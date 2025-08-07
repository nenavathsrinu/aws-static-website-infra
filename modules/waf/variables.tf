variable "env" {
  description = "Environment (e.g., prod, stg, uat)"
  type        = string
}

variable "tags" {
  description = "Common tags to apply to WAF resources"
  type        = map(string)
}

variable "waf_name" {
  description = "Name prefix for the WAF Web ACL"
  type        = string
  default     = "cloudfront-waf"
}