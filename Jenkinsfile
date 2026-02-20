pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE_NAME    = 'harbor.e-edu.uz/ministry-back/hemis-back'
        NAMESPACE     = 'new-ministry'
        RELEASE_NAME  = 'hemis-back'
        CHART_DIR     = 'helm/hemis-back'
        KUBECONFIG    = '/home/jenkins/.kube/config'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build --no-cache -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Push to Harbor') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'harbor-credentials',
                    usernameVariable: 'HARBOR_USER',
                    passwordVariable: 'HARBOR_PASS'
                )]) {
                    sh """
                        echo \$HARBOR_PASS | docker login harbor.e-edu.uz -u \$HARBOR_USER --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                        docker push ${IMAGE_NAME}:latest
                        docker logout harbor.e-edu.uz
                    """
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh """
                    helm upgrade --install ${RELEASE_NAME} ${CHART_DIR} \
                        --namespace ${NAMESPACE} \
                        --set image.repository=${IMAGE_NAME} \
                        --set image.tag=${IMAGE_TAG} \
                        --set replicaCount=1 \
                        --wait \
                        --timeout 5m
                """
            }
        }

        stage('Verify') {
            steps {
                sh """
                    kubectl rollout status deployment/${RELEASE_NAME} \
                        --namespace ${NAMESPACE} \
                        --timeout=3m
                """
            }
        }
    }

    post {
        failure {
            sh "helm rollback ${RELEASE_NAME} 0 --namespace ${NAMESPACE} --wait || true"
        }
        always {
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            cleanWs()
        }
    }
}
