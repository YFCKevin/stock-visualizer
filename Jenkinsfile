pipeline {
    agent any

    environment {
        GOOGLE_APPLICATION_CREDENTIALS = 'gcp-key.json'
        GCP_CREDENTIALS = credentials('gcp-sa-key') // Jenkins 裡 GCP 金鑰憑證 ID
        PROJECT_ID = 'gurula-465111'
        REGION = 'asia-east1'
        REPO_NAME = 'gurula'
        IMAGE_NAME = 'stock-api'
        SERVICE_NAME = 'stock-api'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('GCP Auth') {
            steps {
                withCredentials([file(credentialsId: 'gcp-sa-key', variable: 'GCP_KEY_FILE')]) {
                    sh '''
                    gcloud auth activate-service-account --key-file=$GCP_KEY_FILE
                    gcloud config set project $PROJECT_ID
                    gcloud auth configure-docker $REGION-docker.pkg.dev
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                docker build -t $IMAGE_NAME .
                docker tag $IMAGE_NAME $REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/$IMAGE_NAME
                '''
            }
        }

        stage('Push to Artifact Registry') {
            steps {
                sh '''
                docker push $REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/$IMAGE_NAME
                '''
            }
        }
    }

    post {
        cleanup {
            sh 'rm -f $GOOGLE_APPLICATION_CREDENTIALS'
        }
    }
}