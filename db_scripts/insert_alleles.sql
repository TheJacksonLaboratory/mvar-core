### anticipation is that this is run by a user with the required permissions
### root would have this

## change order of columns
ALTER TABLE allele MODIFY type VARCHAR(255) AFTER id;
ALTER TABLE allele MODIFY name VARCHAR(255) AFTER type;
ALTER TABLE allele MODIFY primary_identifier VARCHAR(255) AFTER name;
ALTER TABLE allele MODIFY symbol VARCHAR(255) AFTER primary_identifier;

LOAD DATA LOCAL INFILE 'alleles_seed.csv'
INTO TABLE allele
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES (type, name, primary_identifier, symbol);

commit
