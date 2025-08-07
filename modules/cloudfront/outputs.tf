output "cloudfront_oai_iam_arn" {
  description = "IAM ARN of the CloudFront Origin Access Identity"
  value       = aws_cloudfront_origin_access_identity.oai.iam_arn
}

output "cloudfront_domain_name" {
  description = "CloudFront Distribution Domain Name"
  value       = aws_cloudfront_distribution.this.domain_name
}

output "cloudfront_zone_id" {
  description = "CloudFront Hosted Zone ID"
  value       = aws_cloudfront_distribution.this.hosted_zone_id
}
