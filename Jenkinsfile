pipeline {
  agent any

  parameters {
    choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select the deployment environment')
    string(name: 'AWS_REGION', defaultValue: 'ap-south-1', description: 'AWS Region to deploy')
  }

  environment {
    TF_VAR_env     = "${params.ENVIRONMENT}"
    TF_VAR_region  = "${params.AWS_REGION}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Terraform Init') {
      steps {
        sh """
          terraform init \
            -backend-config="region=${env.AWS_REGION}" \
            -backend-config="key=state/${env.ENVIRONMENT}/${env.AWS_REGION}/terraform.tfstate"
        """
      }
    }

    stage('Terraform Validate') {
      steps {
        sh "terraform validate"
      }
    }

    stage('Terraform Plan') {
      steps {
        sh "terraform plan -var-file=env/${env.ENVIRONMENT}/terraform.tfvars"
      }
    }

    stage('Terraform Apply') {
      when {
        expression { return params.ENVIRONMENT != 'dev' } // Optional: avoid auto-apply in dev
      }
      steps {
        input message: "Approve apply to ${params.ENVIRONMENT}?"
        sh "terraform apply -auto-approve -var-file=env/${env.ENVIRONMENT}/terraform.tfvars"
      }
    }

    stage('Terraform Destroy') {
      when {
        expression { return false } // Change to true if you want to allow destroy manually
      }
      steps {
        sh "terraform destroy -auto-approve -var-file=env/${env.ENVIRONMENT}/terraform.tfvars"
      }
    }
  }

  post {
    always {
      echo "Pipeline complete for ${params.ENVIRONMENT}"
    }
  }
}