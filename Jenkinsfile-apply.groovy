pipeline {
    agent any

    parameters {
        string(name: 'ENV', defaultValue: '', description: 'Environment (e.g. prod, stg, dev)')
        string(name: 'REGION', defaultValue: '', description: 'AWS region')
        choice(name: 'ACTION', choices: ['plan', 'apply'], description: 'Terraform action')
    }

    stages {
        stage('Terraform Init') {
            steps {
                dir("${env.WORKSPACE}") {
                    bat """
                    terraform init -backend-config="envs/%ENV%/backend.tfvars"
                    """
                }
            }
        }

        stage('Terraform Validate') {
            steps {
                dir("${env.WORKSPACE}") {
                    bat "terraform validate"
                }
            }
        }

        stage('Terraform Plan') {
            when {
                expression { return params.ACTION == 'plan' || params.ACTION == 'apply' }
            }
            steps {
                dir("${env.WORKSPACE}") {
                    bat """
                    terraform plan -var="environment=%ENV%" -var="region=%REGION%" -var-file="envs/%ENV%/terraform.tfvars"
                    """
                }
            }
        }

        stage('Terraform Apply') {
            when {
                expression { return params.ACTION == 'apply' }
            }
            steps {
                dir("${env.WORKSPACE}") {
                    bat """
                    terraform apply -auto-approve -var="environment=%ENV%" -var="region=%REGION%" -var-file="envs/%ENV%/terraform.tfvars"
                    """
                }
            }
        }
    }
}
