pipeline {
    agent any

    tools {
        nodejs 'node22'
    }

    environment {

        docker_image = "dockshresthahub/mapview:v1"
        cluster = "mapview"
        service = "mapview-svc"
    }

    stages {

        stage('git checkout') {
            steps {
                git branch: 'main', credentialsId: 'git-cred', url: 'https://github.com/rajitShrestha/camp-view'
            }
        }
    
    
        stage ('build a docker image') {
            steps {
                script {

                    withDockerRegistry(credentialsId: 'docker-hub') {

                        sh 'docker build -t ${docker_image} .'

                    }
                }
            }
        }    

         stage ('push docker image') {
            steps {
                script {

                    withDockerRegistry(credentialsId: 'docker-hub') {

                        sh 'docker push ${docker_image}'

                    }
                }
            }
        }     

        stage('Deploy to ecs') {
            steps {
                withAWS(credentials: 'aws-cred', region: 'us-east-1') {
                    sh 'aws ecs update-service --cluster ${cluster} --service ${service} --force-new-deployment'
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
