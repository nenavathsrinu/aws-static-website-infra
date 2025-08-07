pipeline {
  agent any

  parameters {
    choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Environment to deploy')
    string(name: 'AWS_REGION', defaultValue: 'ap-south-1', description: 'AWS Region')
  }

  environment {
    TF_VAR_env    = "${params.ENVIRONMENT}"
    TF_VAR_region = "${params.AWS_REGION}"
    AWS_REGION    = "${params.AWS_REGION}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Terraform Init') {
      steps {
        bat """
        terraform init ^
          -backend-config="region=%AWS_REGION%" ^
          -backend-config="key=state\\%TF_VAR_env%\\%AWS_REGION%\\terraform.tfstate"
        """
      }
    }

    stage('Terraform Validate') {
      steps {
        bat "terraform validate"
      }
    }

    stage('Terraform Plan') {
      steps {
        bat "terraform plan -var-file=env\\%TF_VAR_env%\\terraform.tfvars"
      }
    }

    stage('Terraform Apply') {
      when {
        expression { return params.ENVIRONMENT != 'dev' }
      }
      steps {
        input message: "Do you want to apply changes to ${params.ENVIRONMENT}?"
        bat "terraform apply -auto-approve -var-file=env\\%TF_VAR_env%\\terraform.tfvars"
      }
    }
  }

  post {
    always {
      echo "✅ Pipeline execution finished for %TF_VAR_env%"
    }
  }
}