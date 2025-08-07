pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment')
        choice(name: 'AWS_REGION', choices: ['us-east-1', 'us-west-1', 'us-west-2', 'eu-west-1', 'eu-central-1', 'ap-south-1', 'ap-northeast-1'], description: 'Select AWS region')
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

        stage('Terraform Destroy') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    withEnv([
                        "TF_VAR_environment=${params.ENVIRONMENT}",
                        "TF_VAR_region=${params.AWS_REGION}"
                    ]) {
                        bat """
                        terraform init ^
                          -backend-config="bucket=teerafor-state-files-by-project" ^
                          -backend-config="key=state/${params.ENVIRONMENT}/${params.AWS_REGION}/terraform.tfstate" ^
                          -backend-config="region=${params.AWS_REGION}" ^
                          -backend-config="dynamodb_table=terraform-locks" ^
                          -backend-config="encrypt=true"

                        terraform validate

                        terraform destroy -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            slackSend(
                channel: '#devops-alerts',
                color: 'good',
                message: "🧨 Terraform *destroy* succeeded for *${params.ENVIRONMENT}* in *${params.AWS_REGION}*.",
                tokenCredentialId: 'slack-token-id'
            )
        }
        failure {
            slackSend(
                channel: '#devops-alerts',
                color: 'danger',
                message: "🔥 Terraform *destroy* failed for *${params.ENVIRONMENT}* in *${params.AWS_REGION}*.",
                tokenCredentialId: 'slack-token-id'
            )
        }
    }
}