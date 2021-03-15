#!/usr/bin/env groovy

def call(config) {

	pipeline {
		agent any
		
	    stages {
            stage('Setup') {
                steps {
                    script {	
                        configF = readYaml (file: config)
                        giturl = configF.setup.setup_url
                        println "URL GIT: " + giturl
                        gitbranch = configF.setup.setup_branch						  
                        println "RAMA GIT: " + gitbranch
                            
                        git url: giturl, branch: gitbranch
                        //git url:'https://github.com/oscarmestre16/spring-data-examples.git', branch: 'weblib'
                    }
                }
            } 
            // Compilamos el proyecto y almacenamos los test unitarios y de integracion
            stage('Build') {		
                steps {
                    script {			
                        configF = readYaml (file: config)
                        arrayWeb = configF.setup.arrayWebProject
                        println "Array Proyectos: " + arrayWeb
                        //List arrayWebProject = ["web/example/pom.xml", "web/projection/pom.xml", "web/querydsl/pom.xml"]					
                        withMaven (maven: 'Maven 3.6.3') {
                            for (proyecto in arrayWeb) {
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
                    script {	
                        configF = readYaml (file: config)
                        pom_file = configF.setup.archivoPom
                        println "Archivo pom: " + pom_file

                        withMaven (maven: 'Maven 3.6.3') {
                            bat 'mvn org.pitest:pitest-maven:mutationCoverage -f ' + pom_file
                        }
                    }
                }
                
            }
            // Analizamos con SonarQube el proyecto y pasamos los informes generados (test, cobertura, mutation)
            stage('SonarQube analysis') {
                steps {
                    script {	
                        withSonarQubeEnv('Sonarqube') {
                            withMaven (maven: 'Maven 3.6.3') {
                                bat 'mvn sonar:sonar -f ' + pom_file + ' \
                                -Dsonar.sourceEncoding=UTF-8 \
                                -Dsonar.junit.reportPaths=target/surefire-reports'
                            }
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
                        configF = readYaml (file: config)
                        println "Datos de Nexus: " 
                        NEXUS_VERSION = configF.setup.NEXUS_VERSION
                        println "Version Nexus: " + NEXUS_VERSION
                        NEXUS_PROTOCOL = configF.setup.NEXUS_PROTOCOL
                       // println "Protocolo: " + NEXUS_PROTOCOL
                        NEXUS_REPOSITORY = configF.setup.NEXUS_REPOSITORY
                       // println "Repositorio: " + NEXUS_REPOSITORY
                        NEXUS_ID = configF.setup.NEXUS_ID
                       // println "Credenciales Nexus - ID: " + NEXUS_ID
                        arrayNexus = configF.setup.arrayNexusProject
                        //println "Array Proyectos: " + arrayNexus
                        
                        //List arrayNexusProject = ["example", "projection", "querydsl"]
                        /*println "----------------------------------" 
						configF = readYaml (file: config)
						println "Configuraciones para utilizar Nexus"
						proyectsArray2 = configF.nexus.proyectsArray2
						println "Lista de arrays de los subproyectos: " + proyectsArray2
						NEXUS_VERSION = configF.nexus.NEXUS_VERSION
						println "Version Nexus: " + NEXUS_VERSION
						NEXUS_PROTOCOL = configF.nexus.NEXUS_PROTOCOL
						println "Protocolo Nexus: " + NEXUS_PROTOCOL
						NEXUS_URL = configF.nexus.NEXUS_URL
						println "URL Nexus: " + NEXUS_URL
						NEXUS_REPOSITORY = configF.nexus.NEXUS_REPOSITORY
						println "Reporitorio Nexus: " + NEXUS_REPOSITORY
						NEXUS_CREDENTIAL_ID = configF.nexus.NEXUS_CREDENTIAL_ID
						println "Credenciales Nexus: " + NEXUS_CREDENTIAL_ID
						println "----------------------------------"  */

                        for (proyect2 in arrayNexus) {						
                            
                            pom = readMavenPom file: "web/" + proyect2 + "/pom.xml";
                            filesByGlob = findFiles(glob: "web/" + proyect2 + "/target/*.${pom.packaging}");
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
}   