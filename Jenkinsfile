pipeline {
    agent any
    
    environment {
       IMAGE_NAME         = "consumer-notification"
       LOCAL_REGISTRY     = "localhost:5000"
       CLUSTER_REGISTRY   = "registry:5000"
       GIT_CREDENTIALS_ID = "github-consumer-notification-deploy-key"
    }
    
    stages {
        stage ('checkout') {
            steps { 
                chekcout scm 
	    }
        }
        stage('Build & Test') {
	    steps {
		sh 'chmod +x gradlew'
		sh './gradlew clean build --no-deamon'
	    }
	}
	stage('Version & Tag') {
	    steps {
	        script {
		    env.VERSION = "0.1.${BUILD_NUMBER}"
		}
		withCredentials([sshUserPrivateKey(credentialsId: "${GIT_CREDENTIALS_ID}", keyFileVariable: 'SSH_KEY')]) {
		    sh """ 
		        git config user.email "jenkins@local"
			git config user.name "Jenkins"
			git tag -a v${VERSION} -m "Release v${VERSION}"
			GIT_SSH_COMMAND="ssh -i \$SSH_KEY -o StrictHostKeyChecking=no" git push origin v${VERSION}
		    """
		}
	    }
	}
	stage('Docker Build') {
	    steps {
	        sh "docker build -t ${LOCAL_REGISTRY}/${IMAGE_NAME}:${VERSION} ."
	    }
	}
	stage('Docker Push') {
	    steps {
                sh "docker push ${LOCAL_REGISTRY}/${IMAGE_NAME}:${VERSION}"
            }
	}
	stage('Deploy QA') {
	    steps {
                sh """
                    kubectl config use-context cluster-qa
                    kubectl set image deployment/consumer-notification consumer-notification=${CLUSTER_REGISTRY}/${IMAGE_NAME}:${VERSION}
                    kubectl rollout status deployment/consumer-notification --timeout=90s
                """
            }
	}
    }
}
