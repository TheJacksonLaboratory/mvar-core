### anticipation is that this is run by a user with the required permissions
### root would have this
LOAD DATA INFILE '~/mvar-core/src/main/resources/alleles_seed.csv'
INTO TABLE allele
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

commit
