pipeline {
  agent any

  parameters {
    string(name: 'TF_VAR_environment', description: 'Enter environment (e.g., dev, stg, prod)')
    string(name: 'TF_VAR_region', description: 'Enter AWS region (e.g., us-east-1)')
    choice(name: 'CONFIRM_DESTROY', choices: ['no', 'yes'], description: 'Type "yes" to confirm destroy')
  }

  environment {
    TF_VAR_environment = "${params.TF_VAR_environment}"
    TF_VAR_region      = "${params.TF_VAR_region}"
  }

  stages {
    stage('Terraform Init') {
      steps {
        bat """
          terraform init ^
            -backend-config=envs/%TF_VAR_environment%/backend.tfvars
        """
      }
    }

    stage('Terraform Destroy') {
      when {
        expression { params.CONFIRM_DESTROY == 'yes' }
      }
      steps {
        bat """
          terraform destroy -auto-approve ^
            -var="environment=%TF_VAR_environment%" ^
            -var="region=%TF_VAR_region%" ^
            -var-file="envs/%TF_VAR_environment%/terraform.tfvars"
        """
      }
    }
  }
}