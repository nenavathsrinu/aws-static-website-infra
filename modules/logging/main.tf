# Create S3 bucket for CloudFront logs
resource "aws_s3_bucket" "cf_logs" {
  bucket        = "${var.project}-${var.env}-cloudfront-logs"
  tags          = merge(var.tags, { Environment = var.env })
  force_destroy = true
}

# Ensure CloudFront can write logs
resource "aws_s3_bucket_ownership_controls" "cf_logs" {
  bucket = aws_s3_bucket.cf_logs.id
  rule {
    object_ownership = "ObjectWriter"
  }
}

# Block all public access to logs
resource "aws_s3_bucket_public_access_block" "cf_logs" {
  bucket                  = aws_s3_bucket.cf_logs.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Allow CloudFront service to write logs
resource "aws_s3_bucket_policy" "cf_logs" {
  bucket = aws_s3_bucket.cf_logs.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "AllowCloudFrontToWriteLogs",
        Effect    = "Allow",
        Principal = { Service = "delivery.logs.amazonaws.com" },
        Action    = ["s3:PutObject"],
        Resource  = "${aws_s3_bucket.cf_logs.arn}/*",
        Condition = {
          StringEquals = {
            "s3:x-amz-acl" = "bucket-owner-full-control"
          }
        }
      },
      {
        Sid       = "AllowCloudFrontToGetBucketAcl",
        Effect    = "Allow",
        Principal = { Service = "delivery.logs.amazonaws.com" },
        Action    = ["s3:GetBucketAcl"],
        Resource  = aws_s3_bucket.cf_logs.arn
      }
    ]
  })
}
