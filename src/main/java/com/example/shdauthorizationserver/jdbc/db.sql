-- Create databases
CREATE DATABASE shd_dev;
CREATE DATABASE shd_prod;

-- Create database service accounts
CREATE USER 'shd_dev_user'@'%' IDENTIFIED BY 'shd_dev66';
CREATE USER 'shd_prod_user'@'%' IDENTIFIED BY 'shd_prod99';

-- Database grants (only DML)
GRANT SELECT ON shd_dev.* TO 'shd_dev_user'@'%';
GRANT INSERT ON shd_dev.* TO 'shd_dev_user'@'%';
GRANT UPDATE ON shd_dev.* TO 'shd_dev_user'@'%';
GRANT DELETE ON shd_dev.* TO 'shd_dev_user'@'%';

GRANT SELECT ON shd_prod.* TO 'shd_prod_user'@'%';
GRANT INSERT ON shd_prod.* TO 'shd_prod_user'@'%';
GRANT UPDATE ON shd_prod.* TO 'shd_prod_user'@'%';
GRANT DELETE ON shd_prod.* TO 'shd_prod_user'@'%';
