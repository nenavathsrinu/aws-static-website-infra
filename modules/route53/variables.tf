variable "zone_name" {
  description = "The DNS zone name (e.g., example.com)"
  type        = string
}

variable "record_name" {
  description = "The DNS record name (e.g., www, leave empty for root)"
  type        = string
  default     = ""
}

variable "type" {
  description = "DNS record type (e.g., A, CNAME)"
  type        = string
}

variable "ttl" {
  description = "TTL for non-alias records (ignored when alias_name is set)"
  type        = number
  default     = 300
}

variable "record_value" {
  description = "Value for normal DNS records (ignored when alias_name is set)"
  type        = string
  default     = ""
}

variable "alias_name" {
  description = "Alias target DNS name (leave empty for normal records)"
  type        = string
  default     = ""
}

variable "alias_zone_id" {
  description = "Alias target hosted zone ID (required if alias_name is set)"
  type        = string
  default     = ""
}

variable "tags" {
  description = "Tags for the Route53 record"
  type        = map(string)
  default     = {}
}
