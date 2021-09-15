### anticipation is that this is run by a user with the required permissions
### root would have this

## sort table columns
ALTER TABLE sequence_ontology MODIFY label varchar(255) AFTER so_id;
ALTER TABLE sequence_ontology MODIFY sub_class_of varchar(255) AFTER label;
ALTER TABLE sequence_ontology MODIFY definition text AFTER suc_class_of;

# this Sequence Ontology file has been generated with robot, an ontology tool
# and was converted to TSV from an OWL file (found on https://github.com/The-Sequence-Ontology/SO-Ontologies/blob/master/Ontology_Files/so.owl)
LOAD DATA LOCAL INFILE '/local/svc-mvr/mvar-db/mysql/so.csv'
INTO TABLE sequence_ontology
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES (so_id, label, sub_class_of, definition);

commit
