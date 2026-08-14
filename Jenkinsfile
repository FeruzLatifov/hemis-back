// Professional Continuous Delivery pipeline (hemis-back)
// Oqim: bitta image QURILADI → staging (api-test.hemis.uz)'ga deploy → inson TASDIQLAYDI →
//        AYNI image prod'ga (qayta build YO'Q). "build once, promote the same artifact".
// Trigger: main'ga har merge (GitHub webhook). Feature branch → PR → main.
pipeline {
    agent any

    options {
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        IMAGE_NAME    = 'harbor.e-edu.uz/central_hemis-back/hemis-back'
        RELEASE_NAME  = 'hemis-back'
        CHART_DIR     = 'helm/hemis-back'
        KUBECONFIG    = '/home/jenkins/.kube/config'
        STAGING_NS    = 'test-hemis'      // api-test.hemis.uz
        PROD_NS       = 'new-ministry'    // asosiy domen
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Immutable tag = build raqami + git SHA. AYNI shu tag staging va prod'da ishlatiladi.
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
                    echo "Artifact: ${IMAGE_NAME}:${env.IMAGE_TAG}"
                }
            }
        }

        stage('Build & Push (1 marta)') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'harbor-central-hemis',
                    usernameVariable: 'HARBOR_USER',
                    passwordVariable: 'HARBOR_PASS'
                )]) {
                    sh '''
                        echo "$HARBOR_PASS" | docker login harbor.e-edu.uz -u "$HARBOR_USER" --password-stdin
                        docker build --no-cache -t ${IMAGE_NAME}:${IMAGE_TAG} .
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                        docker logout harbor.e-edu.uz
                    '''
                }
            }
        }

        stage('Deploy -> Staging (api-test.hemis.uz)') {
            steps {
                sh '''
                    helm upgrade --install ${RELEASE_NAME} ${CHART_DIR} \
                        --namespace ${STAGING_NS} --create-namespace \
                        -f ${CHART_DIR}/values.yaml -f ${CHART_DIR}/values/test-hemis.yaml \
                        --set image.repository=${IMAGE_NAME} \
                        --set image.tag=${IMAGE_TAG} \
                        --wait --timeout 10m
                    kubectl rollout status deployment/${RELEASE_NAME} --namespace ${STAGING_NS} --timeout=8m
                '''
            }
        }

        stage('Approve -> Production') {
            steps {
                timeout(time: 1, unit: 'DAYS') {
                    input(
                        message: "Staging (api-test.hemis.uz) tekshirildi. Prod'ga (${PROD_NS}) AYNI image chiqaraymi?",
                        ok: "Prod'ga chiqar"
                    )
                }
            }
        }

        stage('Deploy -> Production (AYNI image)') {
            steps {
                sh '''
                    helm upgrade --install ${RELEASE_NAME} ${CHART_DIR} \
                        --namespace ${PROD_NS} --create-namespace \
                        -f ${CHART_DIR}/values.yaml \
                        --set image.repository=${IMAGE_NAME} \
                        --set image.tag=${IMAGE_TAG} \
                        --wait --timeout 10m
                    kubectl rollout status deployment/${RELEASE_NAME} --namespace ${PROD_NS} --timeout=8m
                '''
            }
        }
    }

    post {
        failure {
            sh "helm rollback ${RELEASE_NAME} 0 --namespace ${STAGING_NS} --wait || true"
        }
        always {
            sh 'docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true'
            cleanWs()
        }
    }
}
