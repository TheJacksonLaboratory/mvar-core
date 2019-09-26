### anticipation is that this is run by a user with the required permissions
### root would have this
DROP DATABASE IF EXISTS `mvar_core`;
CREATE DATABASE `mvar_core`;
DROP USER IF EXISTS 'mvar_core'@'%';
CREATE USER 'mvar_core'@'localhost' IDENTIFIED BY 'mvar_core';
GRANT REFERENCES , SELECT, INSERT, DELETE, UPDATE, CREATE, DROP, INDEX, ALTER, CREATE VIEW, SHOW VIEW, TRIGGER, LOCK TABLES , CREATE ROUTINE, ALTER ROUTINE ON `mvar_core`.* TO 'mvar_core'@'localhost';
ALTER USER 'mvar_core'@'localhost' IDENTIFIED WITH mysql_native_password BY 'mvar_core_p@55';
SHOW GRANTS FOR 'mvar_core'@'localhost';

commit
