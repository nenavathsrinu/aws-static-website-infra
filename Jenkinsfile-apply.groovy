pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment')
        choice(name: 'AWS_REGION', choices: [
            'us-east-1', 'us-west-1', 'us-west-2',
            'eu-west-1', 'eu-central-1',
            'ap-south-1', 'ap-northeast-1'
        ], description: 'Select AWS region')
        booleanParam(name: 'DESTROY_INFRA', defaultValue: false, description: 'Destroy infrastructure instead of applying')
    }

    environment {
        TF_ENV       = "${params.ENVIRONMENT}"
        TF_REGION    = "${params.AWS_REGION}"
        TFVARS_FILE  = "envs\\${params.ENVIRONMENT}\\terraform.tfvars"
        BACKEND_FILE = "envs\\${params.ENVIRONMENT}\\backend.tfvars"
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
                    bat "terraform init -backend-config=\"${BACKEND_FILE}\""
                }
            }
        }

        stage('Terraform Validate') {
            steps {
                bat "terraform validate"
            }
        }

        stage('Terraform Plan') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    bat """
                        terraform plan ^
                        -var=\"environment=${TF_ENV}\" ^
                        -var=\"region=${TF_REGION}\" ^
                        -var-file=\"${TFVARS_FILE}\"
                    """
                }
            }
        }

        stage('Terraform Apply or Destroy') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    script {
                        def tfCommand = params.DESTROY_INFRA ? 'destroy' : 'apply'
                        bat """
                            terraform ${tfCommand} ^
                            -auto-approve ^
                            -var=\"environment=${TF_ENV}\" ^
                            -var=\"region=${TF_REGION}\" ^
                            -var-file=\"${TFVARS_FILE}\"
                        """
                    }
                }
            }
        }
    }
}
