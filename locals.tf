locals {
  # Core Tag Values
  env         = var.env
  name        = var.name
  project     = var.project
  managed_by  = "Terraform"
  aws_account = var.aws_account

  # Additional user-defined tags
  othertags = var.othertags

  # Final merged tags
  common_tags = merge(
    {
      Environment = local.env
      Name        = local.name
      Project     = local.project
      ManagedBy   = local.managed_by
      AWSAccount  = local.aws_account
    },
    local.othertags
  )
}