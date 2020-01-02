### anticipation is that this is run by a user with the required permissions
### root would have this

## sort table columns
ALTER TABLE transcript MODIFY length int(11) AFTER primary_identifier;
ALTER TABLE transcript MODIFY location_start bigint(20) AFTER length;
ALTER TABLE transcript MODIFY location_end bigint(20) AFTER location_start;
ALTER TABLE transcript MODIFY mgi_gene_identifier varchar(255) AFTER location_end;
ALTER TABLE transcript MODIFY chromosome varchar(255) AFTER mgi_gene_identifier;
ALTER TABLE transcript MODIFY ens_gene_identifier varchar(255) AFTER chromosome;

LOAD DATA LOCAL INFILE 'transcripts_short_seed.csv'
INTO TABLE transcript
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES (primary_identifier, length, location_start, location_end, mgi_gene_identifier, chromosome);

commit
