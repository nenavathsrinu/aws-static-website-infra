pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment')
        choice(name: 'AWS_REGION', choices: ['us-east-1', 'us-west-1', 'us-west-2', 'eu-west-1', 'eu-central-1', 'ap-south-1', 'ap-northeast-1'], description: 'Select AWS region')
        booleanParam(name: 'DESTROY_INFRA', defaultValue: false, description: 'Destroy infrastructure instead of apply')
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
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    bat '''
                    set AWS_ACCESS_KEY_ID=%AWS_ACCESS_KEY_ID%
                    set AWS_SECRET_ACCESS_KEY=%AWS_SECRET_ACCESS_KEY%
                    terraform init ^
                      -backend-config="bucket=teerafor-state-files-by-project" ^
                      -backend-config="key=state/%TF_VAR_environment%/%TF_VAR_region%/terraform.tfstate" ^
                      -backend-config="region=%TF_VAR_region%" ^
                      -backend-config="dynamodb_table=terraform-locks" ^
                      -backend-config="encrypt=true"
                    '''
                }
            }
        }

        stage('Terraform Validate') {
            steps {
                bat '''
                set AWS_ACCESS_KEY_ID=%AWS_ACCESS_KEY_ID%
                set AWS_SECRET_ACCESS_KEY=%AWS_SECRET_ACCESS_KEY%
                terraform validate
                '''
            }
        }

        stage('Terraform Plan') {
            steps {
                bat '''
                set AWS_ACCESS_KEY_ID=%AWS_ACCESS_KEY_ID%
                set AWS_SECRET_ACCESS_KEY=%AWS_SECRET_ACCESS_KEY%
                terraform plan -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"
                '''
            }
        }

        stage('Confirm Destroy') {
            when {
                expression { return params.DESTROY_INFRA }
            }
            steps {
                input message: "⚠️ Confirm DESTROY for *${params.ENVIRONMENT}* in *${params.AWS_REGION}*", ok: "Yes, Destroy"
            }
        }

        stage('Terraform Apply/Destroy') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    script {
                        def action = params.DESTROY_INFRA ? 'destroy' : 'apply'
                        bat """
                        set AWS_ACCESS_KEY_ID=%AWS_ACCESS_KEY_ID%
                        set AWS_SECRET_ACCESS_KEY=%AWS_SECRET_ACCESS_KEY%
                        terraform ${action} -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"
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
                message: "✅ Terraform *${params.DESTROY_INFRA ? 'destroy' : 'apply'}* succeeded for *${params.ENVIRONMENT}* in *${params.AWS_REGION}*.",
                tokenCredentialId: 'slack-token-id'
            )
        }
        failure {
            slackSend(
                channel: '#devops-alerts',
                color: 'danger',
                message: "❌ Terraform *${params.DESTROY_INFRA ? 'destroy' : 'apply'}* failed for *${params.ENVIRONMENT}* in *${params.AWS_REGION}*.",
                tokenCredentialId: 'slack-token-id'
            )
        }
    }
}