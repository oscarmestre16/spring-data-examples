#!/usr/bin/env groovy
pipeline {
    agent any
	environment {       
        	NEXUS_ID = "Nexus_Token"
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
		withMaven (maven: 'Maven 3.6.3') {
			bat 'mvn clean install -f web/pom.xml'
			bat 'mvn clean install -f web/example/pom.xml'
			bat 'mvn clean install -f web/projection/pom.xml'
			bat 'mvn clean install -f web/querydsl/pom.xml'
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
	//stage("Quality Gate") {
          // steps {
            //    timeout(time: 5, unit: 'MINUTES') {
                     //Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                     //true = set pipeline to UNSTABLE, false = don't
                     //Requires SonarQube Scanner for Jenkins 2.7+
              //      waitForQualityGate abortPipeline: true
                //}
            //}
        //}
	 stage('Nexus - Example') {
		steps {			
			nexusArtifactUploader( 
				artifacts: [
					[artifactId: 'pom.artifactId',
					classifier: '',
					file: 'web/example/target/*.${pom.packaging}',
					type: 'pom.packaging'],
					[artifactId: 'pom.artifactId', 
					classifier: '', 
					file: 'pom.xml',
					type: 'pom']
					],
				credentialsId: 'Nexus_Token',
				groupId: 'pom.groupId',
				nexusUrl: '192.168.43.172:8081',
				nexusVersion: 'nexus3',
				protocol: 'http',
				repository: 'springs-data-examples-web/',
				version: 'pom.parent.version'	
				);
        }
    }
}
