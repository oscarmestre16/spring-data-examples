#!/usr/bin/env groovy
pipeline {
    agent any
    stages {           
        stage('Setup') {
            steps {
                    println "----------------------------------"  
                    script {
                        def props = readProperties file: 'cursofile.properties'
                        env.git_url = props.git_url
                        env.git_branch = props.git_branch
                        
                        git url: git_url, branch: git_branch
                    }                    
                    println "----------------------------------"     
                    
            }
        }
    }
}

    /*@Library('spring-data-examples')_

def configfile = 'cursofile'

setup configfile*/