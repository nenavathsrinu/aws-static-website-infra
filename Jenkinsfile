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
        echo Selected Environment: %ENV%
        echo Selected Region: %AWS_REGION%
        '''
      }
    }

    stage('Terraform Init') {
      steps {
        bat '''
        terraform init -backend-config="region=%AWS_REGION%"
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
        terraform plan -var="env=%ENV%" -var="region=%AWS_REGION%"
        '''
      }
    }

    stage('Terraform Apply') {
      steps {
        input message: "Do you want to apply Terraform changes?"
        bat '''
        terraform apply -auto-approve -var="env=%ENV%" -var="region=%AWS_REGION%"
        '''
      }
    }

    stage('Terraform Destroy') {
      when {
        expression { params.ENVIRONMENT != 'prod' }  // Avoid accidental prod deletion
      }
      steps {
        input message: "Do you want to destroy the infrastructure?"
        bat '''
        terraform destroy -auto-approve -var="env=%ENV%" -var="region=%AWS_REGION%"
        '''
      }
    }
  }
}