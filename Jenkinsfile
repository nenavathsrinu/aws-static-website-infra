properties([
  parameters([
    choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment'),
    [$class: 'CascadeChoiceParameter',
      choiceType: 'PT_SINGLE_SELECT',
      description: 'Select AWS Region',
      filterLength: 1,
      filterable: true,
      name: 'AWS_REGION',
      referencedParameters: '',
      script: [
        $class: 'GroovyScript',
        fallbackScript: [classpath: [], sandbox: false, script: 'return ["us-east-1"]'],
        script: [classpath: [], sandbox: false, script: '''
          return [
            "us-east-1",
            "us-west-1",
            "us-west-2",
            "eu-west-1",
            "ap-south-1",
            "ap-northeast-1"
          ]
        ''']
      ]
    ]
  ])
])

pipeline {
  agent any

  environment {
    TF_VAR_env    = "${params.ENVIRONMENT}"
    TF_VAR_region = "${params.AWS_REGION}"
    AWS_REGION    = "${params.AWS_REGION}"
  }

  stages {
    stage('Check Region Input') {
      steps {
        script {
          if (!params.AWS_REGION?.trim()) {
            error("❌ AWS_REGION is required. Please select a region.")
          }
        }
      }
    }

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
        input message: "🟡 Confirm apply for ${params.ENVIRONMENT} in ${params.AWS_REGION}?"
        bat "terraform apply -auto-approve -var-file=env\\%TF_VAR_env%\\terraform.tfvars"
      }
    }
  }

  post {
    always {
      echo "✅ Pipeline complete for ${params.ENVIRONMENT} in ${params.AWS_REGION}"
    }
  }
}