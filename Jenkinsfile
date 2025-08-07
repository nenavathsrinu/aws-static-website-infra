pipeline {
  agent any

  parameters {
    choice(name: 'ENVIRONMENT', choices: ['prod', 'stg', 'dev'], description: 'Choose environment')
    choice(name: 'AWS_REGION', choices: ['us-east-1', 'ap-south-1', 'eu-west-1'], description: 'AWS region to deploy in')
    choice(name: 'TF_ACTION', choices: ['plan', 'apply', 'destroy'], description: 'Terraform action to perform')
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
        sh """
          terraform init \
            -backend-config="region=${AWS_REGION}" \
            -backend-config="key=state/${ENVIRONMENT}/${AWS_REGION}/terraform.tfstate"
        """
      }
    }

    stage('Terraform Validate') {
      steps {
        sh "terraform validate"
      }
    }

    stage('Terraform Plan/Apply/Destroy') {
      steps {
        script {
          if (params.TF_ACTION == 'plan') {
            sh "terraform plan -var-file=env/${ENVIRONMENT}/terraform.tfvars"
          } else if (params.TF_ACTION == 'apply') {
            sh "terraform apply -auto-approve -var-file=env/${ENVIRONMENT}/terraform.tfvars"
          } else if (params.TF_ACTION == 'destroy') {
            sh "terraform destroy -auto-approve -var-file=env/${ENVIRONMENT}/terraform.tfvars"
          }
        }
      }
    }
  }

  post {
    failure {
      echo '❌ Build failed.'
    }
    success {
      echo '✅ Terraform operation completed.'
    }
  }
}