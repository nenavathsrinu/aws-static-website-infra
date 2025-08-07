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

        stage('Terraform Apply') {
            steps {
                dir("envs/${params.ENVIRONMENT}") {
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                        bat """
                        terraform init -reconfigure
                        terraform validate
                        terraform plan -var-file="terraform.tfvars" -var="region=${params.AWS_REGION}"
                        terraform apply -auto-approve -var-file="terraform.tfvars" -var="region=${params.AWS_REGION}"
                        """
                    }
                }
            }
        }
    }
}