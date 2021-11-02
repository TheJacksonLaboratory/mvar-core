CREATE DEFINER=`root`@`localhost` PROCEDURE `mvar_gene_update`()
BEGIN
    # drop and recreate table
    DROP TABLE IF EXISTS `mvar_core`.`mvar_gene`;

    CREATE TABLE `mvar_gene` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `gene_id` bigint NOT NULL,
                                 `symbol` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FK8l0evg1o9ng0fppauxilomsbm` (`gene_id`),
                                 CONSTRAINT `FK8l0evg1o9ng0fppauxilomsbm` FOREIGN KEY (`gene_id`) REFERENCES `gene` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    # insert into table distinct results from DB
    INSERT INTO mvar_core.mvar_gene (symbol, gene_id)
        SELECT symbol, id FROM mvar_core.gene where id in (select distinct gene_id from variant) ORDER BY symbol;


END