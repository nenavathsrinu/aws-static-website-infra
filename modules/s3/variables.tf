variable "bucket_name" {
  description = "Name of the S3 bucket (without env suffix)"
  type        = string
}

variable "env" {
  description = "Deployment environment (prod, stg, uat)"
  type        = string
}

variable "tags" {
  description = "Common tags to apply to S3 bucket"
  type        = map(string)
}

variable "cloudfront_oai_iam_arn" {
  description = "IAM ARN of the CloudFront Origin Access Identity"
  type        = string
  default     = ""
}

variable "website_files_path" {
  description = "Local path to website files to upload"
  type        = string
}

variable "origin_secret" {
  description = "Secret header value for CloudFront to access S3"
  type        = string
  default     = "super-secret-key-12345"
}