variable "env" { description = "Environment name (prod, stg, uat)" }
variable "name" { description = "Base name for resources" }
variable "project" { description = "Project name" }
variable "aws_account" { description = "AWS Account ID or alias" }

variable "othertags" {
  description = "Additional custom tags"
  type        = map(string)
  default     = {}
}

variable "bucket_name" {
  description = "Base name for the S3 bucket (environment will be appended)"
  type        = string
}

variable "domain_name" {
  description = "The domain name to request an ACM certificate and use for CloudFront"
  type        = string
}

variable "website_files_path" {
  description = "Local path to website files to upload to S3"
  type        = string
  default     = "./website" # default directory where your static files are stored
}
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-east-1" # You can override this in terraform.tfvars
}

variable "region" {
  description = "AWS region to deploy resources"
  type        = string
}

variable "environment" {
  description = "Deployment environment (dev, stg, prod)"
  type        = string
}
