### anticipation is that this is run by a user with the required permissions
### root would have this
DROP DATABASE IF EXISTS `mvar_core`;
CREATE DATABASE `mvar_core`
CHARACTER SET = 'utf8'
COLLATE = 'utf8_unicode_ci';
DROP USER IF EXISTS 'mvar_core'@'%';
CREATE USER 'mvar_core'@'mvar_core' IDENTIFIED BY 'mvar_core_p@55';
GRANT REFERENCES , SELECT, INSERT, DELETE, UPDATE, CREATE, DROP, INDEX, ALTER, CREATE VIEW, SHOW VIEW, TRIGGER, LOCK TABLES , CREATE ROUTINE, ALTER ROUTINE ON `mvar_core`.* TO 'mvar_core'@'mvar_core';
SHOW GRANTS FOR 'mvar_core'@'mvar_core';

commit
