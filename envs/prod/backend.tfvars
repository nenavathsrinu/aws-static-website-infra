terraform {
  backend "s3" {
    bucket         = "teerafor-state-files-by-project"
    key            = "state/prod/us-east-1/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}
