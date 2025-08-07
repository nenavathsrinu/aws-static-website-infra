pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment')
        choice(name: 'AWS_REGION', choices: ['us-east-1', 'us-west-1', 'ap-south-1'], description: 'Select AWS region')
        booleanParam(name: 'CONFIRM_DESTROY', defaultValue: false, description: 'Confirm destroy operation')
    }

    environment {
        TF_VAR_environment = "${params.ENVIRONMENT}"
        TF_VAR_region      = "${params.AWS_REGION}"
    }

    stages {
        stage('Confirmation') {
            when {
                expression { return params.CONFIRM_DESTROY }
            }
            steps {
                echo "Destroy confirmed. Proceeding..."
            }
        }

        stage('Checkout') {
            when {
                expression { return params.CONFIRM_DESTROY }
            }
            steps {
                git url: 'https://github.com/nenavathsrinu/aws-static-website-infra.git', branch: 'main'
            }
        }

        stage('Terraform Init') {
            when {
                expression { return params.CONFIRM_DESTROY }
            }
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

        stage('Destroy') {
            when {
                expression { return params.CONFIRM_DESTROY }
            }
            steps {
                bat 'terraform destroy -auto-approve -var="environment=%TF_VAR_environment%" -var="region=%TF_VAR_region%" -var-file="envs/%TF_VAR_environment%/terraform.tfvars"'
            }
        }
    }

    post {
        success {
            slackSend(channel: '#devops-alerts', message: ":warning: Destroy SUCCESS for `${params.ENVIRONMENT}` in `${params.AWS_REGION}`.")
        }
        failure {
            slackSend(channel: '#devops-alerts', message: ":x: Destroy FAILED for `${params.ENVIRONMENT}` in `${params.AWS_REGION}`.")
        }
    }
}