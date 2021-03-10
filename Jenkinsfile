#!/usr/bin/env groovy
pipeline {
    agent any
	environment {
        // Puede ser nexus3 o nexus2
        NEXUS_VERSION = "nexus3"
        // Puede ser http o https
        NEXUS_PROTOCOL = "http"
        // Dónde se ejecuta tu Nexus        
        NEXUS_URL = "192.168.43.172:8081"
        // Repositorio donde subiremos el artefacto
        NEXUS_REPOSITORY = "springs-data-examples-web/"
        // Identificación de credencial de Jenkins para autenticarse en Nexus OSS
        NEXUS_ID = "nexus-credentials"
	}
    stages {
        stage('Setup') {
            steps {
                git url:'https://github.com/oscarmestre16/spring-data-examples.git', branch: 'web'
            }
        } 
		
        // Compilamos el proyecto y almacenamos los test unitarios y de integracion
       	stage('Build') {		
            steps {
                script {			
					List arrayWebProject = ["web/example/pom.xml", "web/projection/pom.xml", "web/querydsl/pom.xml"]					
					withMaven (maven: 'Maven 3.6.3') {
						for (proyecto in arrayWebProject) {
							println proyecto
							bat 'mvn clean install -f ' + proyecto
                            //mvn clean install -f web/example/pom.xml
						}
					}           
    	        }
            }
            post {
                always {
                    junit 'web/example/target/surefire-reports/*.xml, web/projection/target/surefire-reports/*.xml, web/querydsl/target/surefire-reports/*.xml'
                }
            }		
        }
        // Lanzamos en paralelo la comprobacion de dependencias y los mutation test
        stage('Mutation Test') {
			// Lanzamos los mutation test			
            steps {
              withMaven (maven: 'Maven 3.6.3') {
                bat 'mvn org.pitest:pitest-maven:mutationCoverage -f web/pom.xml'
              }
            }
			
        }
        // Analizamos con SonarQube el proyecto y pasamos los informes generados (test, cobertura, mutation)
        stage('SonarQube analysis') {
		  steps {
			    withSonarQubeEnv('Sonarqube') {
				      withMaven (maven: 'Maven 3.6.3') {
						bat 'mvn sonar:sonar -f web/pom.xml \
						-Dsonar.sourceEncoding=UTF-8 \
						-Dsonar.junit.reportPaths=target/surefire-reports'
				      }
			     }
		   }
	    }
		// Esperamos hasta que se genere el QG y fallamos o no el job dependiendo del estado del mismo
        stage("Quality Gate") {
            steps {
                    timeout(time: 5, unit: 'MINUTES') {
                        //Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                        //true = set pipeline to UNSTABLE, false = don't
                        //Requires SonarQube Scanner for Jenkins 2.7+
                    waitForQualityGate abortPipeline: true
                    }
                }
            }
        
        stage("Nexus") {
            steps {
                script {
                    List arrayNexusProject = ["example", "projection", "querydsl"]

                    for (nexusProyecto in arrayNexusProject) {
                        archivo = "web/" + nexusProyecto + "/pom.xml"
                        archivoTarget = "web/example" + nexusProyecto + "/target/*.${pom.packaging}"
                        pom = readMavenPom file: archivo;
                        filesByGlob = findFiles(glob: archivoTarget);
                        echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                        artifactPath = filesByGlob[0].path;
                        artifactExists = fileExists artifactPath;
                        if(artifactExists) {
                            echo "*** File: ${artifactPath}, group: ${pom.parent.groupId}, packaging: ${pom.packaging}, version ${pom.parent.version}";
                            nexusArtifactUploader(
                                nexusVersion: NEXUS_VERSION,
                                protocol: NEXUS_PROTOCOL,
                                nexusUrl: NEXUS_URL,
                                groupId: pom.groupId,
                                version: pom.parent.version,
                                repository: NEXUS_REPOSITORY,
                                credentialsId: NEXUS_ID,
                                artifacts: [
                                    [artifactId: pom.artifactId,
                                    classifier: '',
                                    file: artifactPath,
                                    type: pom.packaging],
                                    [artifactId: pom.artifactId,
                                    classifier: '',
                                    file: "pom.xml",
                                    type: "pom"]
                               ]
                            );
                        } else {
                            error "*** File: ${artifactPath}, could not be found";
                        }
                    }
                }
            }
        }
    }
}