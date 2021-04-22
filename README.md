# Database set up and insertion of Variants


## Run the grails application the first time to populate the data model in the DB


java -Dgrails.env=development -XX:-UseGCOverheadLimit -jar mvar-core-0.1.war 2>> log.txt



## Reorder the transcript and allele tables

alter table transcript modify m_rna_id varchar(255) after primary_identifier;
alter table transcript modify gene_symbol varchar(255) after m_rna_id;
alter table transcript modify description text after gene_symbol;


alter table allele modify type varchar(255) after id;
alter table allele modify name varchar(255) after type;
alter table allele modify primary_identifier varchar(255) after name;
alter table allele modify symbol text after primary_identifier;


## Load the transcript and allele seed csv files to the DB

Make sure that the files are located in /var/lib/mysql

### Transcripts

load data local infile 'transcripts_ref_noversion.csv' into table transcript  fields terminated by ',' optionally enclosed by '"' lines terminated by '\n' ignore 1 lines (primary_identifier, m_rna_id, gene_symbol, description);


### Alleles

load data local infile 'alleles_seed.csv' into table allele  fields terminated by ',' optionally enclosed by '"' lines terminated by '\n' ignore 1 lines (type, name, primary_identifier, symbol);


## Re run the grails application to populate strains, genes and corresponding relationships

## Re run the grails application to insert the variation files into DB

java -Dgrails.env=development -XX:-UseGCOverheadLimit -jar mvar-core-0.1.war /path/to/variation/files/ 2>> log.txt


