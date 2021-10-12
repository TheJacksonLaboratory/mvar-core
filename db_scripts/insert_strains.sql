### anticipation is that this is run by a user with the required permissions
### root would have this

## sort table columns
ALTER TABLE strain MODIFY attributes varchar(255) AFTER primary_identifier;
ALTER TABLE strain MODIFY name varchar(255) AFTER attributes;
ALTER TABLE strain MODIFY other_ids varchar(255) AFTER name;
ALTER TABLE strain MODIFY synonyms varchar(255) AFTER other_ids;

LOAD DATA LOCAL INFILE '/local/svc-mvr/mvar-db/mysql/mvar-strains-mgi-mpd.csv'
INTO TABLE strain
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES (primary_identifier, attributes, name, other_ids, synonyms);

commit
