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
		withMaven (maven: 'Maven 3.6.3') {
			bat 'mvn clean install -f web/pom.xml'
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
	stage('Nexus Publisher') {
		  steps {
			nexusPublisher nexusInstanceId: 'maven-releases', nexusRepositoryId: 'maven-releases', packages: [[$class:
			'MavenPackage', mavenAssetList: [[classifier: '', extension: '', filePath: ' \\target\\*.jar']], 
			mavenCoordinate: [artifactId: 'spring-data-examples', groupId: 'org.springframework.data.examples', packaging: 'jar', version: '2.0.0.BUILD-SNAPSHOT']]]	 
		  }
	}
	    
		// Esperamos hasta que se genere el QG y fallamos o no el job dependiendo del estado del mismo
	//stage("Quality Gate") {
          // steps {
            //    timeout(time: 1, unit: 'HOURS') {
                    // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    // true = set pipeline to UNSTABLE, false = don't
                    // Requires SonarQube Scanner for Jenkins 2.7+
              //      waitForQualityGate abortPipeline: true
               // }
           // }
        //}
    }
}
