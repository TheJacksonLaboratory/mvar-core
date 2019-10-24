CREATE DEFINER=`root`@`localhost` PROCEDURE `Variant_Insertion`(
	IN var_chr CHAR(1),
    IN var_position BIGINT(20),
    IN var_alt CHAR(1),
    IN var_ref CHAR(100),
    IN var_type VARCHAR(255),
    IN var_assembly VARCHAR(255),
    IN var_parent_ref_ind BIT(1),
    IN var_parent_variant_ref_txt VARCHAR(255),
    IN var_variant_ref_txt VARCHAR(255),
    OUT var_canon_var_identifier_id BIGINT(20),
    IN gene_name VARCHAR(255),
    IN transcript_reference_accession VARCHAR(255),
    IN transcript_protein_change VARCHAR(255),
    IN transcript_name VARCHAR(255),
    IN identifier_external_src_id BIGINT(20),
    IN identifier_external_id VARCHAR(255),
    IN hgvs_ref_accession VARCHAR(255),
    IN hgvs_description VARCHAR(255),
    IN strain_name VARCHAR(255),
    IN src_base_url VARCHAR(255),
    IN src_name VARCHAR(255),
    IN src_type VARCHAR(255)
    
)
BEGIN
	# DECLARE var_canon_var_identifier_id BIGINT(20) DEFAULT 0;
    DECLARE existing_canon_var_id BIGINT(20);
    DECLARE parent_variant_ref_txt VARCHAR(255);
	# We check if the variant has already been canonicalized
    SELECT id, variant_ref_txt INTO existing_canon_var_id, parent_variant_ref_txt FROM variant_canon_identifier WHERE variant_ref_txt = var_variant_ref_txt;
    IF parent_variant_ref_txt is NULL THEN
		# 1. insert into variant_canon_identifier
		INSERT INTO variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) 
			VALUES (0, var_chr, var_position, var_ref, var_alt, var_variant_ref_txt);
		SELECT last_insert_id() as inserted_canon_var_id;
        SET var_canon_var_identifier_id = inserted_canon_var_id;
	ELSE
		SET var_canon_var_identifier_id = existing_canon_var_id;
    END IF;
	
	# 2. insert into variant
	INSERT INTO variant (version, chr, position, alt, ref, type, assembly, parent_ref_ind, parent_variant_ref_txt, variant_ref_txt, canon_var_identifier_id)
        VALUES (0, var_chr, var_position, var_alt, var_ref, var_type, var_assembly, var_parent_ref_ind, var_parent_variant_ref_txt, var_variant_ref_txt, var_canon_var_identifier_id);
	# we get the id of the latest insertion (ie the variant id)
    SELECT last_insert_id() AS inserted_var_id;
	# 3. insert into gene
	INSERT INTO gene (version, chromosome, name) VALUES (0, var_chr, gene_name);
    SELECT last_insert_id() AS inserted_gene_id;
    
    # 4. insert into transcript
    INSERT INTO transcript (version, reference_accession, protein_change, name, variant_id) VALUES (0, transcript_reference_accession, transcript_protein_change, transcript_name, inserted_var_id);
	SELECT last_insert_id() as inserted_transcript_id;
    
	# 5. insert into identifier
	INSERT INTO identifier (version, external_source_id, external_id) VALUES (0, identifier_external_src_id, identifier_external_id);
	SELECT last_insert_id() AS inserted_identifier_id; 
    
    # 6. insert into hgvs
    INSERT INTO hgvs (version, ref_accession, description) VALUES (0, hgvs_ref_accession, hgvs_description);
    SELECT last_insert_id() AS inserted_hgvs_id;
    
    # 7. insert into strain
	INSERT INTO strain (version, name) VALUES (0, strain_name);
    SELECT last_insert_id() AS inserted_strain_id;
    
    # 8. insert into source
	INSERT INTO source (version, base_url, source_name, type) VALUES (0, src_base_url, src_name, src_type);

	# 9. insert into correspondance tables
    INSERT INTO strain_identifier (strain_identifiers_id, identifier_id) VALUES (inserted_strain_id, inserted_identifier_id);
	INSERT INTO variant_identifier (variant_identifier_id, identifier_id) VALUES (inserted_var_id, inserted_identifier_id);
	INSERT INTO gene_identifier (gene_identifiers_id, identifier_id) VALUES (inserted_gene_id, inserted_identifier_id);
	INSERT INTO variant_strain (variant_strains_id, strain_id) VALUES (inserted_var_id, inserted_strain_id);
	INSERT INTO variant_hgvs (variant_hgvs_id, hgvs_id) VALUES (inserted_var_id, inserted_hgvs_id);
    # where do we get transcript_protein_effect_hgvs_id?
	#INSERT INTO transcript_hgvs (transcript_hgvs_id, hgvs_id, transcript_protein_effect_hgvs_id) VALUES (inserted_transcript_id, inserted_hgvs_id, ?);

END