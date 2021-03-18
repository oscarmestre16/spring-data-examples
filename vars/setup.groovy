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
                            
                        git url: git_url, branch: git_branch
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
                        bat 'docker build -t ' + NEXUS_IMAGE + ' .' 
                        //sh 'docker build -t ' + NEXUS_IMAGE + ' .' 
                    }
                }	
            }
           stage("Nexus") {
                steps {
                    script {		
                        println "---------------------------------"
                        configF = readYaml (file: config)
                        NEXUS_IMAGE = configF.nexus.NEXUS_IMAGE                        
                        NEXUS_URL = configF.nexus.NEXUS_URL
                        NEXUS_REPOSITORY = configF.nexus.NEXUS_REPOSITORY
                        NEXUS_ID = configF.nexus.NEXUS_ID

                        println "Datos Nexus: " + " \nImagen de nexus: "+ NEXUS_IMAGE +" \nURL: " + NEXUS_URL + " \nRepositorio: " + NEXUS_REPOSITORY + " \nCredenciales Nexus - ID: " + NEXUS_ID 

                        bat 'docker image ls' 
                        withDockerRegistry(credentialsId: 'nexus-credentials', url: '192.168.43.172:8081') {
                           bat 'docker tag' + NEXUS_IMAGE +' '+ NEXUS_URL + NEXUS_REPOSITORY + NEXUS_IMAGE
                           bat 'docker push' + NEXUS_URL + NEXUS_REPOSITORY + NEXUS_IMAGE
                        }                                               
                             
                    }
                }
            }
        }
    }
}   