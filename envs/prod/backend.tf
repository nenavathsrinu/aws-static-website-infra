terraform {
  backend "s3" {
    bucket         = "teerafor-state-files-by-project "
    key            = "state/${var.env}/${var.region}/terraform.tfstate"
    region         = var.region
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}