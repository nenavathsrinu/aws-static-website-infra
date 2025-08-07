output "cf_logs_bucket_domain_name" {
  description = "CloudFront logs bucket domain name"
  value       = aws_s3_bucket.cf_logs.bucket_domain_name
}

output "cf_logs_bucket" {
  description = "CloudFront logs bucket name"
  value       = aws_s3_bucket.cf_logs.bucket
}
