pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment')
        choice(name: 'AWS_REGION', choices: ['us-east-1', 'us-west-1', 'us-west-2', 'eu-west-1', 'eu-central-1', 'ap-south-1', 'ap-northeast-1'], description: 'Select AWS region')
        booleanParam(name: 'DESTROY_INFRA', defaultValue: false, description: 'Destroy infrastructure instead of applying')
    }

    environment {
        TF_VAR_environment = "${params.ENVIRONMENT}"
        TF_VAR_region      = "${params.AWS_REGION}"
        TF_VARS_FILE       = "envs/${params.ENVIRONMENT}/terraform.tfvars"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/nenavathsrinu/aws-static-website-infra.git', branch: 'main'
            }
        }

        stage('Terraform Init') {
            steps {
                bat 'terraform init'
            }
        }

        stage('Terraform Validate') {
            steps {
                bat 'terraform validate'
            }
        }

        stage('Terraform Plan') {
            steps {
                bat """
                    terraform plan ^
                    -var="environment=%TF_VAR_environment%" ^
                    -var="region=%TF_VAR_region%" ^
                    -var-file="%TF_VARS_FILE%"
                """
            }
        }

        stage('Terraform Apply or Destroy') {
            steps {
                script {
                    if (params.DESTROY_INFRA) {
                        bat """
                            terraform destroy -auto-approve ^
                            -var="environment=%TF_VAR_environment%" ^
                            -var="region=%TF_VAR_region%" ^
                            -var-file="%TF_VARS_FILE%"
                        """
                    } else {
                        bat """
                            terraform apply -auto-approve ^
                            -var="environment=%TF_VAR_environment%" ^
                            -var="region=%TF_VAR_region%" ^
                            -var-file="%TF_VARS_FILE%"
                        """
                    }
                }
            }
        }
    }
}