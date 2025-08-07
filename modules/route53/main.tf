# ✅ Get the Hosted Zone ID
data "aws_route53_zone" "selected" {
  name         = var.zone_name
  private_zone = false
}

# ✅ Create Route53 Record (Alias or Normal)
resource "aws_route53_record" "this" {
  zone_id = data.aws_route53_zone.selected.zone_id
  name    = var.record_name
  type    = var.type

  # ✅ Alias Record Block (Only when alias_name is provided)
  dynamic "alias" {
    for_each = var.alias_name != "" ? [1] : []
    content {
      name                   = var.alias_name
      zone_id                = var.alias_zone_id
      evaluate_target_health = false
    }
  }

  # ✅ Normal Record Block (Only when alias_name is NOT provided)
  ttl     = var.alias_name == "" ? var.ttl : null
  records = var.alias_name == "" ? [var.record_value] : null
}