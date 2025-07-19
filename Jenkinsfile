pipeline {
    agent any

    environment {
        PROJECT_ID = 'gurula-465111'
        REGION = 'asia-east1'
        REPO_NAME = 'gurula'
        IMAGE_NAME = 'stock-api'
        GOOGLE_APPLICATION_CREDENTIALS = 'gcp-key.json'
        GCP_CREDENTIALS = credentials('gcp-sa-key')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('GCP Auth') {
            steps {
                sh '''
                echo "$GCP_CREDENTIALS" > $GOOGLE_APPLICATION_CREDENTIALS
                gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                gcloud config set project $PROJECT_ID
                gcloud auth configure-docker $REGION-docker.pkg.dev
                '''
            }
        }

        stage('Push to Artifact Registry') {
            steps {
                sh '''
                docker tag $IMAGE_NAME $REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/$IMAGE_NAME
                docker push $REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/$IMAGE_NAME
                '''
            }
        }
    }
}