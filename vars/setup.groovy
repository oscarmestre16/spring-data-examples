#!/usr/bin/env groovy

def call(config) {

	pipeline {
		agent any
		
	    stages {
            stage('Setup') {
                steps {
                    script {	
                        configF = readYaml (file: config)
                        git_url = configF.setup.setup_url
                        println "URL GIT: " + git_url
                        git_branch = configF.setup.setup_branch						  
                        println "RAMA GIT: " + git_branch
                            
                        git url: giturl, branch: git_branch
                        //git url:'https://github.com/oscarmestre16/spring-data-examples.git', branch: 'weblib'
                    }
                }
            } 
            // Compilamos el proyecto y almacenamos los test unitarios y de integracion
            stage('Dockerfile-Build') {		
                steps {
                    script {		
                        println "---------------------------------"
                        configF = readYaml (file: config)
                        NEXUS_IMAGE = configF.nexus.NEXUS_IMAGE
                        println "Imagen de nexus: " + NEXUS_IMAGE
                        sh 'docker build -t' +  NEXUS_IMAGE + ' .'      
                    }
                }	
            }
           /* // Compilamos el proyecto y almacenamos los test unitarios y de integracion
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
                        NEXUS_VERSION = configF.nexus.NEXUS_VERSION
                        NEXUS_PROTOCOL = configF.nexus.NEXUS_PROTOCOL
                        NEXUS_URL = configF.nexus.NEXUS_URL
                        NEXUS_REPOSITORY = configF.nexus.NEXUS_REPOSITORY
                        NEXUS_ID = configF.nexus.NEXUS_ID
                        arrayNexus = configF.nexus.arrayNexusProject                
						
						println "Datos Nexus: " + " \nVersion Nexus: "+ NEXUS_VERSION +" \nProtocolo: "+NEXUS_PROTOCOL+" \nURL: " + NEXUS_URL + " \nRepositorio: " + NEXUS_REPOSITORY + " \nCredenciales Nexus - ID: " + NEXUS_ID + " \nArray Proyectos de Nexus: " + arrayNexus
                                                
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
            }*/
        }
    }
}   