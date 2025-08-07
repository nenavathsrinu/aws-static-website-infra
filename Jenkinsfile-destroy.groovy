pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment')
        choice(name: 'AWS_REGION', choices: [
            'us-east-1', 'us-west-1', 'us-west-2',
            'eu-west-1', 'eu-central-1',
            'ap-south-1', 'ap-northeast-1'
        ], description: 'Select AWS region')
    }

    environment {
        TF_VAR_environment = "${params.ENVIRONMENT}"
        TF_VAR_region      = "${params.AWS_REGION}"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/nenavathsrinu/aws-static-website-infra.git', branch: 'main'
            }
        }

        stage('Terraform Init') {
            steps {
                withAWS(credentials: 'aws-credentials') {
                    dir("${WORKSPACE}") {
                        bat "terraform init -backend-config=envs\\\\${params.ENVIRONMENT}\\\\backend.tfvars"
                    }
                }
            }
        }

        stage('Terraform Validate') {
            steps {
                bat 'terraform validate'
            }
        }

        stage('Terraform Destroy') {
            steps {
                withAWS(credentials: 'aws-credentials') {
                    dir("${WORKSPACE}") {
                        bat 'terraform destroy -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs\\\\%TF_VAR_environment%\\\\terraform.tfvars"'
                    }
                }
            }
        }
    }

    post {
        always {
            echo "⚠️ Terraform destroy completed for environment: '${params.ENVIRONMENT}' in region: '${params.AWS_REGION}'"
        }
    }
}