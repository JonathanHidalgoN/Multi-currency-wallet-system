variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment (dev, staging, prod)"
  type        = string
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "wallet_db"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "wallet_admin"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for DB"
  type        = list(string)
}

variable "rds_security_group_id" {
  description = "Security group ID for RDS"
  type        = string
}
