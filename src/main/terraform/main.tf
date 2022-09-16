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

# Only needed once, to create backend stuff
module "bootstrap" {
  source                      = "./modules/bootstrap"
  name_of_s3_bucket           = "streaming-fs2-learning"
  dynamo_db_table_name        = "aws-locks"
}

resource "aws_ecr_repository" "repo" {
  name = "repo"
}

module "vpc" {
  source             = "./modules/vpc"
  name               = var.name
  cidr               = var.cidr
  private_subnets    = var.private_subnets
  public_subnets     = var.public_subnets
  availability_zones = var.availability_zones
  environment        = var.environment
}

module "security_groups" {
  source         = "./modules/security-groups"
  name           = var.name
  vpc_id         = module.vpc.id
  environment    = var.environment
  container_port = var.container_port
}

module "alb" {
  source              = "./modules/alb"
  name                = var.name
  vpc_id              = module.vpc.id
  subnets             = module.vpc.public_subnets
  environment         = var.environment
  alb_security_groups = [module.security_groups.alb]
  //  alb_tls_cert_arn    = var.tsl_certificate_arn
  health_check_path   = var.health_check_path
}
