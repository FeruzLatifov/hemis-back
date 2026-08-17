// CD pipeline (hemis-back) — main'ga merge → 1 marta BUILD → STAGING (api-test.hemis.uz)
//   → Approve gate → PROD (api-central.hemis.uz). Build-once: prodga aynan test qilingan
//   IMAGE chiqadi (qayta build YO'Q). Har deploy --atomic (xatoda avto-rollback, per-namespace).
pipeline {
    agent any

    options {
        timeout(time: 90, unit: 'MINUTES')   // Approve gate kutishi (60m) + build/deploy
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        IMAGE_NAME    = 'harbor.e-edu.uz/central_hemis-back/hemis-back'
        RELEASE_NAME  = 'hemis-back'
        CHART_DIR     = 'helm/hemis-back'
        KUBECONFIG    = '/home/jenkins/.kube/config'
        STAGING_NS    = 'test-hemis'       // api-test.hemis.uz
        PROD_NS       = 'central-hemis'    // api-central.hemis.uz
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
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
                    // --network=host: Gradle Maven Central DNS'ni host resolveri orqali (bridge DNS overload'dan qochish)
                    // --provenance=false --sbom=false: BuildKit default provenance attestation'ni O'CHIRADI (Harbor push bug'i).
                    sh '''
                        echo "$HARBOR_PASS" | docker login harbor.e-edu.uz -u "$HARBOR_USER" --password-stdin
                        docker build --network=host --provenance=false --sbom=false -t ${IMAGE_NAME}:${IMAGE_TAG} .
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
                        --atomic --timeout 10m
                    kubectl rollout status deployment/${RELEASE_NAME} --namespace ${STAGING_NS} --timeout=8m
                '''
            }
        }

        stage('Approve -> Production') {
            steps {
                timeout(time: 60, unit: 'MINUTES') {
                    input message: "PRODUCTION (api-central.hemis.uz) ga ${IMAGE_NAME}:${env.IMAGE_TAG} deploy qilinsinmi? (staging test qilingan aynan shu image)", ok: 'Deploy PROD'
                }
            }
        }

        stage('Deploy -> Production (api-central.hemis.uz)') {
            steps {
                // Ayni IMAGE_TAG — qayta build YO'Q. Birinchi prod deploy: migration-job restore qilingan
                // CUBA base ustiga hemis-back migratsiyalarini (h_*, users, seed) qo'llaydi (bir necha daqiqa).
                sh '''
                    helm upgrade --install ${RELEASE_NAME} ${CHART_DIR} \
                        --namespace ${PROD_NS} --create-namespace \
                        -f ${CHART_DIR}/values.yaml -f ${CHART_DIR}/values/central.yaml \
                        --set image.repository=${IMAGE_NAME} \
                        --set image.tag=${IMAGE_TAG} \
                        --atomic --timeout 15m
                    kubectl rollout status deployment/${RELEASE_NAME} --namespace ${PROD_NS} --timeout=8m
                '''
            }
        }
    }

    post {
        always {
            sh 'docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true'
            cleanWs()
        }
    }
}
