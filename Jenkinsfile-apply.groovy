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
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/nenavathsrinu/aws-static-website-infra.git', branch: 'main'
            }
        }

        stage('Terraform Apply') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    dir("envs/${params.ENVIRONMENT}") {
                        bat """
                        terraform init
                        terraform validate
                        terraform plan -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="terraform.tfvars"
                        terraform apply -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="terraform.tfvars"
                        """
                    }
                }
            }
        }
    }
}