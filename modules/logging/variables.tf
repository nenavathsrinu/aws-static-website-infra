variable "project" {
  description = "Project name"
  type        = string
}

variable "env" {
  description = "Environment name (prod, stg, dev)"
  type        = string
}

variable "tags" {
  description = "Tags to apply to the log bucket"
  type        = map(string)
}