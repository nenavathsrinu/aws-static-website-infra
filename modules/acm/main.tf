provider "aws" {
  alias  = "us-east-1"
  region = "us-east-1"
}

# ✅ Request ACM Certificate (DNS Validation)
resource "aws_acm_certificate" "cert" {
  provider          = aws.us-east-1
  domain_name       = var.domain_name
  validation_method = "DNS"

  tags = var.tags
}

# ✅ Create DNS Validation Records (if using Route53)
resource "aws_route53_record" "validation" {
  for_each = {
    for dvo in aws_acm_certificate.cert.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  zone_id = data.aws_route53_zone.selected.zone_id
  name    = each.value.name
  type    = each.value.type
  records = [each.value.record]
  ttl     = 60
}

# ✅ Route53 Zone Lookup (required for DNS validation)
data "aws_route53_zone" "selected" {
  name         = var.domain_name
  private_zone = false
}

# ✅ Validate Certificate
resource "aws_acm_certificate_validation" "cert_validation" {
  provider                = aws.us-east-1
  certificate_arn         = aws_acm_certificate.cert.arn
  validation_record_fqdns = [for record in aws_route53_record.validation : record.fqdn]
}