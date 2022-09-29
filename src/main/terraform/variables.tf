locals {
  image       = "${var.aws_account_id}.dkr.ecr.us-east-1.amazonaws.com/${var.name}"
}

variable "name" {
  description = "the name of your stack, e.g. \"demo\""
  default     = "streams-fs2-learning"
}

variable "environment" {
  description = "the name of your environment, e.g. \"prod\""
  default     = "prod"
}

variable "domain" {
  description = "The domain by which services will be reached"
  default = "bjhartin.com"
}

variable "region" {
  description = "the AWS region in which resources are created, you must set the availability_zones variable as well if you define this value to something other than the default"
}

variable "aws-region" {
  type        = string
  description = "AWS region to launch servers."
}

variable "aws-access-key" {
  type = string
}

variable "aws-secret-key" {
  type = string
}

variable "aws_account_id" {
  type = string
}

variable "availability_zones" {
  default     = ["us-east-1a", "us-east-1b"]
}

variable "cidr" {
  description = "The CIDR block for the VPC."
  default     = "10.0.0.0/16"
}

variable "private_subnets" {
  description = "a list of CIDRs for private subnets in your VPC, must be set if the cidr variable is defined, needs to have as many elements as there are availability zones"
  default     = ["10.0.0.0/20", "10.0.32.0/20"]
}

variable "public_subnets" {
  description = "a list of CIDRs for public subnets in your VPC, must be set if the cidr variable is defined, needs to have as many elements as there are availability zones"
  default     = ["10.0.16.0/20", "10.0.48.0/20"]
}

variable "service_desired_count" {
  description = "Number of tasks running in parallel"
  default     = 1
}

variable "container_port" {
  description = "The port where the Docker is exposed"
  default     = 8000
}

variable "container_cpu" {
  description = "The number of cpu units used by the task"
  default     = 256
}

variable "container_memory" {
  description = "The amount (in MiB) of memory used by the task"
  default     = 512
}

variable "health_check_path" {
  description = "Http path for task health check"
  default     = "/health"
}
