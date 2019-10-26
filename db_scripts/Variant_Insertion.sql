CREATE DEFINER=`mvar_core`@`localhost` PROCEDURE `Variant_Insertion`(
	IN var_chr CHAR(1),
    IN var_position BIGINT(20),
    IN var_alt CHAR(1),
    IN var_ref CHAR(100),
    IN var_type VARCHAR(255),
    IN var_assembly VARCHAR(255),
    IN var_parent_ref_ind BIT(1),
    IN var_parent_variant_ref_txt VARCHAR(255),
    IN var_variant_ref_txt VARCHAR(255),
    IN gene_name VARCHAR(255),
    IN transcript_reference_accession VARCHAR(255),
    IN transcript_protein_change VARCHAR(255),
    IN transcript_name VARCHAR(255),
    IN identifier_external_id VARCHAR(255),
    IN hgvs_ref_accession VARCHAR(255),
    IN hgvs_description VARCHAR(255),
    IN strain_name VARCHAR(255),
    IN src_base_url VARCHAR(255),
    IN src_name VARCHAR(255),
    IN src_type VARCHAR(255)
)
    COMMENT '\n Description\n \n Inserts the variation data (variant, strain, gene, hgvs, transcript, source, identifier) into the DB, \n The variant insertion is preceded by the creation of a canonicalized unique variant entry for the particular variant being inserted. \n If the canonicalized id for a variant already exists, it links the existing canonicalized id to duplicated variant being inserted.\n'
BEGIN
	DECLARE var_canon_var_identifier_id BIGINT(20);
    DECLARE existing_canon_var_id BIGINT(20);
    DECLARE parent_variant_ref_txt VARCHAR(255);
    DECLARE inserted_canon_var_id BIGINT(20);
	DECLARE inserted_var_id BIGINT(20);
	DECLARE inserted_gene_id BIGINT(20);
    DECLARE inserted_transcript_id BIGINT(20);
    DECLARE inserted_source_id BIGINT(20);
    DECLARE inserted_identifier_id BIGINT(20);
    DECLARE inserted_hgvs_id BIGINT(20);
    DECLARE strain_id BIGINT(20);
	# We check if the variant has already been canonicalized
START TRANSACTION;
    SELECT id, variant_ref_txt INTO existing_canon_var_id, parent_variant_ref_txt FROM variant_canon_identifier WHERE variant_ref_txt = var_variant_ref_txt;
    IF parent_variant_ref_txt is NULL THEN
		# 1. insert into variant_canon_identifier
		INSERT INTO variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) 
			VALUES (0, var_chr, var_position, var_ref, var_alt, var_variant_ref_txt);
		SET inserted_canon_var_id = last_insert_id();
        SELECT concat('variant canonicalized inserted with id: ', inserted_canon_var_id) ;
        SET var_canon_var_identifier_id = inserted_canon_var_id;
	ELSE
		SELECT concat('variant canonicalized already exist with id: ', existing_canon_var_id) ;
		SET var_canon_var_identifier_id = existing_canon_var_id;
    END IF;
    
    # 2. insert into gene
    INSERT INTO gene (version, chromosome, name) VALUES (0, var_chr, gene_name);
    SET inserted_gene_id = last_insert_id();
    SELECT concat('gene inserted with id:', inserted_gene_id);
    
	# 3. insert into variant
	INSERT INTO variant (version, chr, position, alt, ref, type, assembly, parent_ref_ind, parent_variant_ref_txt, variant_ref_txt, canon_var_identifier_id, gene_id)
        VALUES (0, var_chr, var_position, var_alt, var_ref, var_type, var_assembly, var_parent_ref_ind, var_parent_variant_ref_txt, var_variant_ref_txt, var_canon_var_identifier_id, inserted_gene_id);
    SET inserted_var_id = last_insert_id();
    SELECT concat('variant inserted with id:', inserted_var_id);
    
    # 4. insert into transcript
    INSERT INTO transcript (version, reference_accession, protein_change, name, variant_id) VALUES (0, transcript_reference_accession, transcript_protein_change, transcript_name, inserted_var_id);
	SET inserted_transcript_id = last_insert_id();
    SELECT concat('transcript inserted with id:', inserted_transcript_id);

    # 5. insert into source
	INSERT INTO source (version, base_url, source_name, type) VALUES (0, src_base_url, src_name, src_type);
    SET inserted_source_id = last_insert_id();
    SELECT concat('source inserted with id:', inserted_source_id);
    
	# 6. insert into identifier
	INSERT INTO identifier (version, external_source_id, external_id) VALUES (0, inserted_source_id, identifier_external_id);
	SET inserted_identifier_id = last_insert_id();
	SELECT concat('identifier inserted with id:', inserted_identifier_id);
    
    # 7. insert into hgvs
    INSERT INTO hgvs (version, ref_accession, description) VALUES (0, hgvs_ref_accession, hgvs_description);
    SET inserted_hgvs_id = last_insert_id();
    SELECT concat('hgvs inserted with id:', inserted_hgvs_id);
    
    # 8. insert into strain
    IF (SELECT EXISTS(SELECT id, name FROM strain WHERE name like CONCAT('%', strain_name, '%'))) THEN
		SET strain_id = id;
		SELECT concat('existing strain with id:', strain_id);
	ELSE
		INSERT INTO strain (version, name) VALUES (0, strain_name);
		SET strain_id = last_insert_id();
		SELECT concat('new strain inserted with id:', strain_id);
	END IF;

	# 9. insert into correspondance tables
    INSERT INTO strain_identifier (strain_identifiers_id, identifier_id) VALUES (strain_id, inserted_identifier_id);
	INSERT INTO variant_identifier (variant_identifier_id, identifier_id) VALUES (inserted_var_id, inserted_identifier_id);
	INSERT INTO gene_identifier (gene_identifiers_id, identifier_id) VALUES (inserted_gene_id, inserted_identifier_id);
	INSERT INTO variant_strain (variant_strains_id, strain_id) VALUES (inserted_var_id, strain_id);
	INSERT INTO variant_hgvs (variant_hgvs_id, hgvs_id) VALUES (inserted_var_id, inserted_hgvs_id);
    SELECT 'Correspondence tables data inserted';
    
COMMIT;
    # where do we get transcript_protein_effect_hgvs_id?
	#INSERT INTO transcript_hgvs (transcript_hgvs_id, hgvs_id, transcript_protein_effect_hgvs_id) VALUES (inserted_transcript_id, inserted_hgvs_id, ?);

END