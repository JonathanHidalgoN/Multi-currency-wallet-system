terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Remote state configuration (uses bootstrap S3 bucket)
  backend "s3" {
    bucket         = "payflow-terraform-state-payflow"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "payflow-terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "PayFlow"
      Environment = "prod"
      ManagedBy   = "Terraform"
    }
  }
}

# Data sources for secrets
data "aws_secretsmanager_secret" "jwt_secret" {
  name = "/payflow/prod/jwt-secret"
}

data "aws_secretsmanager_secret" "db_password" {
  name = "/payflow/prod/db-password"
}

data "aws_secretsmanager_secret" "exchange_rate_api_key" {
  name = "/payflow/prod/exchange-rate-api-key"
}

data "aws_secretsmanager_secret_version" "db_password" {
  secret_id = data.aws_secretsmanager_secret.db_password.id
}

# Networking
module "networking" {
  source = "../../modules/networking"

  project_name = var.project_name
  aws_region   = var.aws_region
  vpc_cidr     = var.vpc_cidr
}

# ECR
module "ecr" {
  source = "../../modules/ecr"

  project_name = var.project_name
}

# Database
module "database" {
  source = "../../modules/database"

  project_name          = var.project_name
  environment           = "prod"
  db_name               = var.db_name
  db_username           = var.db_username
  db_password           = data.aws_secretsmanager_secret_version.db_password.secret_string
  private_subnet_ids    = module.networking.private_subnet_ids
  rds_security_group_id = module.networking.rds_security_group_id
}

# ECS
module "ecs" {
  source = "../../modules/ecs"

  project_name                = var.project_name
  environment                 = "prod"
  aws_region                  = var.aws_region
  vpc_id                      = module.networking.vpc_id
  public_subnet_ids           = module.networking.public_subnet_ids
  private_subnet_ids          = module.networking.private_subnet_ids
  ecs_tasks_security_group_id = module.networking.ecs_tasks_security_group_id
  alb_security_group_id       = module.networking.alb_security_group_id

  ecr_repository_url = module.ecr.repository_url
  image_tag          = var.image_tag

  task_cpu      = var.task_cpu
  task_memory   = var.task_memory
  desired_count = var.desired_count

  db_address  = module.database.db_address
  db_port     = module.database.db_port
  db_name     = module.database.db_name
  db_username = var.db_username

  jwt_secret_arn            = data.aws_secretsmanager_secret.jwt_secret.arn
  db_password_secret_arn    = data.aws_secretsmanager_secret.db_password.arn
  exchange_rate_api_key_arn = data.aws_secretsmanager_secret.exchange_rate_api_key.arn

  secrets_arns = [
    data.aws_secretsmanager_secret.jwt_secret.arn,
    data.aws_secretsmanager_secret.db_password.arn,
    data.aws_secretsmanager_secret.exchange_rate_api_key.arn
  ]
}

# Monitoring
module "monitoring" {
  source = "../../modules/monitoring"

  project_name     = var.project_name
  aws_region       = var.aws_region
  alarm_email      = var.alarm_email

  ecs_cluster_name         = module.ecs.ecs_cluster_name
  ecs_service_name         = module.ecs.ecs_service_name
  db_instance_id           = module.database.db_instance_id
  target_group_arn_suffix  = module.ecs.target_group_arn_suffix
  load_balancer_arn_suffix = module.ecs.alb_arn_suffix
}
