properties([
  parameters([
    choice(name: 'ENVIRONMENT', choices: ['dev', 'stg', 'prod'], description: 'Select environment'),
    [
      $class: 'CascadeChoiceParameter',
      choiceType: 'PT_SINGLE_SELECT',
      description: 'Select AWS Region dynamically',
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
            "eu-central-1",
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
    AWS_REGION = "${params.AWS_REGION}"
    ENV = "${params.ENVIRONMENT}"
  }

  stages {
    stage('Initialize') {
      steps {
        bat '''
        echo =============================
        echo Selected Environment: %ENV%
        echo Selected Region: %AWS_REGION%
        echo =============================
        '''
      }
    }

    stage('Terraform Init') {
      steps {
        bat '''
        terraform init -backend-config=env\\%ENV%\\backend.tfvars
        '''
      }
    }

    stage('Terraform Validate') {
      steps {
        bat 'terraform validate'
      }
    }

    stage('Terraform Plan') {
      steps {
        bat '''
        terraform plan -var-file=env\\%ENV%\\terraform.tfvars -var="region=%AWS_REGION%"
        '''
      }
    }

    stage('Terraform Apply') {
      steps {
        input message: "✅ Proceed with Terraform Apply?"
        bat '''
        terraform apply -auto-approve -var-file=env\\%ENV%\\terraform.tfvars -var="region=%AWS_REGION%"
        '''
      }
    }

    stage('Terraform Destroy') {
      when {
        expression { return params.ENVIRONMENT != 'prod' }
      }
      steps {
        input message: "⚠️ Confirm Terraform Destroy for non-prod?"
        bat '''
        terraform destroy -auto-approve -var-file=env\\%ENV%\\terraform.tfvars -var="region=%AWS_REGION%"
        '''
      }
    }
  }

  post {
    always {
      echo "✅ Pipeline completed for environment: ${params.ENVIRONMENT}, region: ${params.AWS_REGION}"
    }
  }
}