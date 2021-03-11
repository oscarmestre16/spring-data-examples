#!/usr/bin/env groovy
pipeline {
    agent any
    stages {           
        stage('Setup') {
            steps {
                    println "----------------------------------"  
                    script {
                        def props = readProperties file: 'cursofile.properties'
                        env.github_url = props.git_url
                        env.github_branch = props.git_branch
                        
                        git url: github_url, branch: github_branch
                    }                    
                    println "----------------------------------"     
                    
            }
        }
    }
}

    /*@Library('spring-data-examples')_

def configfile = 'cursofile'

setup configfile*/