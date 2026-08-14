pipeline {
    agent any

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['new-ministry', 'test-hemis'],
            description: 'Deploy target: new-ministry (asosiy) yoki test-hemis (2-domen test: api-test.hemis.uz)'
        )
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE_NAME    = 'harbor.e-edu.uz/ministry-back/hemis-back'
        RELEASE_NAME  = 'hemis-back'
        CHART_DIR     = 'helm/hemis-back'
        KUBECONFIG    = '/home/jenkins/.kube/config'
        // NAMESPACE va VALUES_ARGS — DEPLOY_ENV parametridan Checkout'da o'rnatiladi
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
                    env.NAMESPACE = params.DEPLOY_ENV
                    // Base values.yaml har doim; test-hemis uchun qo'shimcha overlay (ingress host + existingSecret)
                    if (params.DEPLOY_ENV == 'new-ministry') {
                        env.VALUES_ARGS = "-f ${CHART_DIR}/values.yaml"
                    } else {
                        env.VALUES_ARGS = "-f ${CHART_DIR}/values.yaml -f ${CHART_DIR}/values/${params.DEPLOY_ENV}.yaml"
                    }
                    echo "Deploy target: namespace=${env.NAMESPACE}, tag=${env.IMAGE_TAG}"
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
                    credentialsId: 'harbor-ministry-back',
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
                        --namespace ${NAMESPACE} --create-namespace \
                        ${VALUES_ARGS} \
                        --set image.repository=${IMAGE_NAME} \
                        --set image.tag=${IMAGE_TAG} \
                        --wait \
                        --timeout 8m
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
