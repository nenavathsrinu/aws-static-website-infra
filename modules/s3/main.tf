# ✅ Create S3 Bucket (Private)
resource "aws_s3_bucket" "this" {
  bucket = "${var.bucket_name}-${var.env}"
  tags   = merge(var.tags, { Environment = var.env })
}

# ✅ Block All Public Access
resource "aws_s3_bucket_public_access_block" "this" {
  bucket                  = aws_s3_bucket.this.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ✅ Allow ONLY CloudFront OAI
data "aws_iam_policy_document" "allow_cf" {
  statement {
    sid    = "AllowCloudFrontAccessOnly"
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = [var.cloudfront_oai_iam_arn] # <-- Pass from CloudFront module output
    }

    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.this.arn}/*"]
  }
}

resource "aws_s3_bucket_policy" "allow_cf" {
  bucket = aws_s3_bucket.this.id
  policy = data.aws_iam_policy_document.allow_cf.json
}

# ✅ (Optional) Static Website Configuration
resource "aws_s3_bucket_website_configuration" "this" {
  bucket = aws_s3_bucket.this.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "error.html"
  }
}

# ✅ Upload Website Files with Correct Content-Type
resource "aws_s3_object" "website_files" {
  for_each = fileset(var.website_files_path, "**/*")

  bucket = aws_s3_bucket.this.id
  key    = each.value
  source = "${var.website_files_path}/${each.value}"
  etag   = filemd5("${var.website_files_path}/${each.value}")

  # ✅ Automatically set correct Content-Type based on file extension
  content_type = lookup(
    {
      html = "text/html",
      css  = "text/css",
      js   = "application/javascript",
      json = "application/json",
      xml  = "application/xml",
      png  = "image/png",
      jpg  = "image/jpeg",
      jpeg = "image/jpeg",
      gif  = "image/gif",
      svg  = "image/svg+xml",
      ico  = "image/x-icon",
      txt  = "text/plain",
      pdf  = "application/pdf"
    },
    lower(element(split(".", each.value), length(split(".", each.value)) - 1)),
    "binary/octet-stream"
  )

  # ✅ Optional: set caching headers
  cache_control = "public, max-age=31536000"
}

