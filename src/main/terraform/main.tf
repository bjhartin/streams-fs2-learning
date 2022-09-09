# Require TF version to be same as or greater than 0.12.13
terraform {
  required_version = ">=0.12.13"
  required_providers {
    aws = {
      source  = "aws"
      version = "~> 2.36.0"
    }
  }
  backend "s3" {
    bucket         = "streaming-fs2-learning"
    key            = "terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "aws-locks"
    encrypt        = true
  }
}

# Download any stable version in AWS provider of 2.36.0 or higher in 2.36 train
#provider "aws" {
#  region  = "us-east-1"
  # version = "~> 2.36.0" Needs to go in required providers block
#}

# Call the seed_module to build our ADO seed info
module "bootstrap" {
  source                      = "./modules/bootstrap"
  name_of_s3_bucket           = "streaming-fs2-learning"
  dynamo_db_table_name        = "aws-locks"
}