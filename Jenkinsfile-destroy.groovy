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
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
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
        }

        stage('Terraform Validate') {
            steps {
                bat 'terraform validate'
            }
        }

        stage('Terraform Plan') {
            steps {
                bat 'terraform plan -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"'
            }
        }

        stage('Confirm Destroy') {
            when {
                expression { return params.DESTROY_INFRA }
            }
            steps {
                input message: "⚠️ Confirm destroy for ${params.ENVIRONMENT} in ${params.AWS_REGION}", ok: "Yes, destroy"
            }
        }

        stage('Terraform Apply/Destroy') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    script {
                        if (params.DESTROY_INFRA) {
                            bat 'terraform destroy -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"'
                        } else {
                            bat 'terraform apply -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Terraform ${params.DESTROY_INFRA ? 'destroy' : 'apply'} succeeded for ${params.ENVIRONMENT} in ${params.AWS_REGION}"
        }
        failure {
            echo "❌ Terraform ${params.DESTROY_INFRA ? 'destroy' : 'apply'} failed for ${params.ENVIRONMENT} in ${params.AWS_REGION}"
        }

        // Optional: uncomment to enable email notifications
        /*
        success {
            mail to: 'team@example.com',
                 subject: "Terraform Apply Succeeded for ${params.ENVIRONMENT} in ${params.AWS_REGION}",
                 body: "The Terraform apply was successful."
        }
        failure {
            mail to: 'team@example.com',
                 subject: "Terraform Apply Failed for ${params.ENVIRONMENT} in ${params.AWS_REGION}",
                 body: "The Terraform apply failed. Please check the Jenkins console output."
        }
        */
    }
}
