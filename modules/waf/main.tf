resource "aws_wafv2_web_acl" "this" {
  name  = "${var.waf_name}-${var.env}"
  scope = "CLOUDFRONT" # CloudFront only supports CLOUDFRONT scope

  default_action {
    allow {}
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "WAFMetrics-${var.env}"
    sampled_requests_enabled   = true
  }

  # ✅ AWS Managed Common Rule Set
  rule {
    name     = "AWS-AWSManagedRulesCommonRuleSet"
    priority = 1

    override_action {
      none {}
    }

    statement {
      managed_rule_group_statement {
        vendor_name = "AWS"
        name        = "AWSManagedRulesCommonRuleSet"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "CommonRuleSet-${var.env}"
      sampled_requests_enabled   = true
    }
  }

  # ✅ AWS Managed SQLi Rule Set
  rule {
    name     = "AWS-AWSManagedRulesSQLiRuleSet"
    priority = 2

    override_action {
      none {}
    }

    statement {
      managed_rule_group_statement {
        vendor_name = "AWS"
        name        = "AWSManagedRulesSQLiRuleSet"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "SQLiRuleSet-${var.env}"
      sampled_requests_enabled   = true
    }
  }

  tags = merge(var.tags, { Environment = var.env })
}