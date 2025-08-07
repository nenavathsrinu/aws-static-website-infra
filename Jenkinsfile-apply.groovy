pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment')
        choice(name: 'AWS_REGION', choices: ['us-east-1', 'us-west-1', 'ap-south-1'], description: 'Select AWS region')
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
                bat """
                terraform init ^
                  -backend-config="bucket=teerafor-state-files-by-project" ^
                  -backend-config="key=state/${params.ENVIRONMENT}/${params.AWS_REGION}/terraform.tfstate" ^
                  -backend-config="region=${params.AWS_REGION}" ^
                  -backend-config="dynamodb_table=terraform-locks" ^
                  -backend-config="encrypt=true"
                """
            }
        }

        stage('Validate') {
            steps {
                bat 'terraform validate'
            }
        }

        stage('Plan') {
            steps {
                bat 'terraform plan -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"'
            }
        }

        stage('Apply') {
            steps {
                bat 'terraform apply -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"'
            }
        }
    }

    post {
        success {
            slackSend(channel: '#devops-alerts', message: ":white_check_mark: Apply SUCCESS for `${params.ENVIRONMENT}` in `${params.AWS_REGION}`.")
        }
        failure {
            slackSend(channel: '#devops-alerts', message: ":x: Apply FAILED for `${params.ENVIRONMENT}` in `${params.AWS_REGION}`.")
        }
    }
}