pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['QA', 'PROD'],
            description: 'QA builda a partir de uma branch. PROD promove uma tag já existente (sem rebuild).')
        string(name: 'GIT_REF', defaultValue: 'master',
            description: 'Branch (se ENVIRONMENT=QA) ou tag, ex: v0.1.6 (se ENVIRONMENT=PROD)')
    }

    environment {
        IMAGE_NAME         = "consumer-notification"
        LOCAL_REGISTRY     = "localhost:5000"
        CLUSTER_REGISTRY   = "registry:5000"
        GIT_CREDENTIALS_ID = "github-consumer-notification-deploy-key"
        GIT_REPO           = "git@github.com:Framks/consumer-notification.git"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    def refSpec = params.ENVIRONMENT == 'PROD' ? "refs/tags/${params.GIT_REF}" : "refs/heads/${params.GIT_REF}"
                    checkout([$class: 'GitSCM',
                        branches: [[name: refSpec]],
                        userRemoteConfigs: [[url: "${GIT_REPO}", credentialsId: "${GIT_CREDENTIALS_ID}"]]
                    ])
                }
            }
        }

        stage('Validar tag (PROD)') {
            when { expression { params.ENVIRONMENT == 'PROD' } }
            steps {
                sh """
                    git fetch --tags
                    git rev-parse "refs/tags/${params.GIT_REF}" \
                        || (echo "GIT_REF '${params.GIT_REF}' não é uma tag existente. Em PROD, GIT_REF precisa ser uma tag já publicada." && exit 1)
                """
            }
        }

        stage('Build & Test') {
            when { expression { params.ENVIRONMENT == 'QA' } }
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build --no-daemon'
            }
        }

        stage('Version & Tag') {
            when { expression { params.ENVIRONMENT == 'QA' } }
            steps {
                script {
                    if (params.GIT_REF == 'master') {
                        env.VERSION = "0.1.${BUILD_NUMBER}"
                        withCredentials([sshUserPrivateKey(credentialsId: "${GIT_CREDENTIALS_ID}", keyFileVariable: 'SSH_KEY')]) {
                            sh """
                                git config user.email "jenkins@local"
                                git config user.name "Jenkins"
                                git tag -a v${VERSION} -m "Release v${VERSION}"
                                GIT_SSH_COMMAND="ssh -i \$SSH_KEY -o StrictHostKeyChecking=no" git push origin v${VERSION}
                            """
                        }
                    } else {
                        env.VERSION = "${params.GIT_REF.replaceAll('[^A-Za-z0-9.-]', '-')}-${BUILD_NUMBER}"
                        echo "Branch '${params.GIT_REF}' não é master — build de teste, sem tag oficial (versão descartável: ${env.VERSION})"
                    }
                }
            }
        }

        stage('Docker Build') {
            when { expression { params.ENVIRONMENT == 'QA' } }
            steps {
                sh "docker build -t ${LOCAL_REGISTRY}/${IMAGE_NAME}:${VERSION} ."
            }
        }

        stage('Docker Push') {
            when { expression { params.ENVIRONMENT == 'QA' } }
            steps {
                sh "docker push ${LOCAL_REGISTRY}/${IMAGE_NAME}:${VERSION}"
            }
        }

        stage('Deploy QA') {
            when { expression { params.ENVIRONMENT == 'QA' } }
            steps {
                sh """
                    kubectl config use-context cluster-qa
                    kubectl set image deployment/consumer-notification consumer-notification=${CLUSTER_REGISTRY}/${IMAGE_NAME}:${VERSION} -n notification
                    kubectl rollout status deployment/consumer-notification -n notification --timeout=90s
                """
            }
        }

        stage('Deploy PROD') {
            when { expression { params.ENVIRONMENT == 'PROD' } }
            steps {
                script {
                    env.VERSION = params.GIT_REF.replaceFirst('^v', '')
                }
                sh """
                    kubectl config use-context cluster-prod
                    kubectl set image deployment/consumer-notification consumer-notification=${CLUSTER_REGISTRY}/${IMAGE_NAME}:${VERSION} -n notification
                    kubectl rollout status deployment/consumer-notification -n notification --timeout=90s
                """
            }
        }
    }
}
