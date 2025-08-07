variable "s3_domain_name" {
  description = "S3 bucket regional domain name (from S3 module output)"
  type        = string
}

variable "acm_cert_arn" {
  description = "ARN of the ACM certificate for HTTPS"
  type        = string
}

variable "env" {
  description = "Environment name (e.g., prod, stg, uat)"
  type        = string
}

variable "tags" {
  description = "Common tags to apply to CloudFront resources"
  type        = map(string)
}

variable "default_root_object" {
  description = "Default root object for CloudFront"
  type        = string
  default     = "index.html"
}


variable "origin_id" {
  description = "Origin ID for CloudFront origin"
  type        = string
  default     = "s3-origin"
}

variable "price_class" {
  description = "Price class for CloudFront distribution"
  type        = string
  default     = "PriceClass_100"
}

variable "origin_secret" {
  description = "Secret header value for CloudFront to access S3"
  type        = string
  default     = "super-secret-key-12345"
}

variable "log_bucket_domain_name" {
  description = "Domain name of the S3 bucket for CloudFront logs"
  type        = string
}
variable "web_acl_arn" {
  description = "The ARN of the AWS WAF Web ACL to associate with the CloudFront distribution"
  type        = string
  default     = null
}
