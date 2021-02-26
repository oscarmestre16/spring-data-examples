#!/usr/bin/env groovy
pipeline {
    agent any
    stages {
        stage('Setup') {
            steps {
                git url:'https://github.com/oscarmestre16/spring-data-examples.git', branch: 'master'
            }
        } 
		
        // Compilamos el proyecto y almacenamos los test unitarios y de integracion
       	stage('Build') {
        	steps {
            withMaven (maven: 'maven-3.6.3') {
              sh 'mvn clean install -f web/pom.xml'
            }
          }
          post {
	   always{
            	junit 'web/example/target/surefire-reports/*.xml, web/projection/target/surefire-reports/*.xml, web/querydsl/target/surefire-reports/*.xml'
	   }
          }
        }
        // Lanzamos en paralelo la comprobacion de dependencias y los mutation test
        stage('Mutation Test') {
			// Lanzamos los mutation test			
            steps {
              withMaven (maven: 'maven-3.6.3') {
                sh 'mvn org.pitest:pitest-maven:mutationCoverage -f web/pom.xml'
              }
            }
			
        }
        // Analizamos con SonarQube el proyecto y pasamos los informes generados (test, cobertura, mutation)
        stage('SonarQube analysis') {
        	steps {
            withSonarQubeEnv(credentialsId: '8360d0699f13cb4748e20aab19aa98f923443bab', installationName: 'local') {
              withMaven (maven: 'maven-3.6.3') {
                sh 'mvn sonar:sonar -f web/pom.xml \
                -Dsonar.sourceEncoding=UTF-8 \
                -Dsonar.login=admin \
                -Dsonar.password=oscarmestre'
              }
		    	}
			}
		}
		// Esperamos hasta que se genere el QG y fallamos o no el job dependiendo del estado del mismo
		stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    // true = set pipeline to UNSTABLE, false = don't
                    // Requires SonarQube Scanner for Jenkins 2.7+
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
