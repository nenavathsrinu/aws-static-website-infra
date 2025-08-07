variable "domain_name" {
  description = "The primary domain name for the ACM certificate"
  type        = string
}

variable "tags" {
  description = "Common tags to apply to ACM resources"
  type        = map(string)
}