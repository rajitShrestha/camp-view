pipeline {
    agent any

    tools {
        nodejs 'node22'
        jdk 'openjdk'
    }

    environment {
        scannerHome = tool 'sonar'
        docker_image = "dockshresthahub/camp-site:latest"
    }

    stages {

        stage('git checkout') {
            steps {
                git branch: 'main', credentialsId: 'git-cred', url: 'https://github.com/rajitShrestha/camp-view'
            }
        }

        stage('Sonar Analysis') {

            steps {
               withSonarQubeEnv('sonar') {
                   sh '''${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=camp-site \
                   -Dsonar.projectName=camp-site \
                   -Dsonar.projectVersion=1.0
                   '''
              }
            }
        }

        stage ('build a docker image') {
            steps {
                script {

                    withDockerRegistry(credentialsId: 'docker-cred') {

                        sh 'docker build -t ${docker_image} .'

                    }
                }
            }
        }    

         stage ('push docker image') {
            steps {
                script {

                    withDockerRegistry(credentialsId: 'docker-cred') {

                        sh 'docker push ${docker_image}'

                    }
                }
            }
        }   
          
        
    }

     post {
        cleanup {
            script {
                // Clean up local Docker image to save space
                sh "docker rmi ${docker_image} || true"
            }
        }
    }
    
}
