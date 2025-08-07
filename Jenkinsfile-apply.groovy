pipeline {
    agent any

    parameters {
        string(name: 'ENV', description: 'Environment (prod/stg/uat)')
    }

    environment {
        TF_VAR_environment = "${params.ENV}"
    }

    stages {
        stage('Terraform Init') {
            steps {
                dir("${env.WORKSPACE}") {
                    bat """
                    terraform init -backend-config="envs/${TF_VAR_environment}/backend.tfvars"
                    """
                }
            }
        }

        stage('Terraform Validate') {
            steps {
                dir("${env.WORKSPACE}") {
                    bat """
                    terraform validate
                    """
                }
            }
        }

        stage('Terraform Plan') {
            steps {
                dir("${env.WORKSPACE}") {
                    bat """
                    terraform plan -var-file="envs/${TF_VAR_environment}/terraform.tfvars"
                    """
                }
            }
        }

        stage('Terraform Apply') {
            steps {
                dir("${env.WORKSPACE}") {
                    bat """
                    terraform apply -auto-approve -var-file="envs/${TF_VAR_environment}/terraform.tfvars"
                    """
                }
            }
        }
    }
}