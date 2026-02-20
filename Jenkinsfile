pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE_NAME    = 'harbor.e-edu.uz/ministry-back/hemis-back'
        IMAGE_TAG     = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
        NAMESPACE     = 'new-ministry'
        RELEASE_NAME  = 'hemis-back'
        CHART_DIR     = 'helm/hemis-back'
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
                script {
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}", '--no-cache .')
                }
            }
        }

        stage('Push to Harbor') {
            steps {
                script {
                    docker.withRegistry('https://harbor.e-edu.uz', 'harbor-credentials') {
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push()
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                script {
                    sh """
                        helm upgrade --install ${RELEASE_NAME} ${CHART_DIR} \
                            --namespace ${NAMESPACE} \
                            --set image.repository=${IMAGE_NAME} \
                            --set image.tag=${IMAGE_TAG} \
                            --wait \
                            --timeout 5m
                    """
                }
            }
        }

        stage('Verify') {
            steps {
                script {
                    sh """
                        kubectl rollout status deployment/${RELEASE_NAME} \
                            --namespace ${NAMESPACE} \
                            --timeout=3m
                    """
                }
            }
        }
    }

    post {
        failure {
            script {
                sh """
                    helm rollback ${RELEASE_NAME} 0 \
                        --namespace ${NAMESPACE} \
                        --wait || true
                """
            }
        }
        always {
            cleanWs()
        }
    }
}
