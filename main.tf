# ✅ Step 1: Create S3 Bucket (with CloudFront OAI Permission handled inside module)
module "s3" {
  source                 = "./modules/s3"
  bucket_name            = var.bucket_name
  env                    = var.env
  tags                   = local.common_tags
  website_files_path     = var.website_files_path
  cloudfront_oai_iam_arn = module.cloudfront.cloudfront_oai_iam_arn # passed to module
}

# ✅ Step 2: Create ACM Certificate
module "acm" {
  source      = "./modules/acm"
  domain_name = var.domain_name
  tags        = local.common_tags
}

# ✅ Step 3: Create WAF
module "waf" {
  source = "./modules/waf"
  env    = var.env
  tags   = local.common_tags
}

# ✅ Step 4: Create CloudFront (references S3 bucket & ACM cert)
module "cloudfront" {
  source                 = "./modules/cloudfront"
  s3_domain_name         = module.s3.bucket_regional_domain_name
  acm_cert_arn           = module.acm.cert_arn
  env                    = var.env
  tags                   = local.common_tags
  log_bucket_domain_name = module.logging.cf_logs_bucket_domain_name # ✅ Pass here
  web_acl_arn            = module.waf.waf_arn
}

# ✅ Step 5: Create Route53 Records
module "route53_root" {
  source        = "./modules/route53"
  zone_name     = "srinuawsdevops.in"
  record_name   = "" # root domain
  type          = "A"
  alias_name    = module.cloudfront.cloudfront_domain_name
  alias_zone_id = module.cloudfront.cloudfront_zone_id
  tags          = local.common_tags
}

module "route53_www" {
  source        = "./modules/route53"
  zone_name     = "srinuawsdevops.in"
  record_name   = "www"
  type          = "A"
  alias_name    = module.cloudfront.cloudfront_domain_name
  alias_zone_id = module.cloudfront.cloudfront_zone_id
  tags          = local.common_tags
}

module "logging" {
  source  = "./modules/logging"
  project = var.project
  env     = var.env
  tags    = local.common_tags
}