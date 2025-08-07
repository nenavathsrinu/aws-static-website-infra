output "record_fqdn" {
  description = "Fully qualified domain name of the created record"
  value       = aws_route53_record.this.fqdn
}