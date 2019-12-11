### anticipation is that this is run by a user with the required permissions
### root would have this
LOAD DATA INFILE '~/mvar-core/src/main/resources/transcripts_seed.csv'
INTO TABLE transcript
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

commit
