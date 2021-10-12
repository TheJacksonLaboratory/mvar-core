CREATE DEFINER=`root`@`localhost` PROCEDURE `mvar_stat_update`()
BEGIN
    #find number of rows
    SET @variant_count := (SELECT count(id) FROM variant);
    SET @variant_canon_identifier_count := (SELECT count(id) FROM variant_canon_identifier);
    SET @variant_strain_count := (SELECT count(id) FROM variant_strain);
    SET @gene_count := (SELECT count(*) FROM gene);
    SET @transcript_count := (SELECT count(*) FROM transcript);
    SET @strain_count := (SELECT count(*) FROM strain);
    SET @variant_transcript_count := (SELECT count(*) FROM variant_transcript);
    SET @strain_analysis_count := (SELECT count(*) FROM mvar_strain);
    SET @transcript_analysis_count := (SELECT count(distinct transcript_id) as transcript_num FROM mvar_core.variant_transcript);
    SET @gene_analysis_count := (SELECT count(distinct gene_id) as gene_num FROM mvar_core.variant);

#update mvar_stats
    UPDATE mvar_stat SET
                         variant_count = @variant_count,
                         variant_canon_identifier_count = @variant_canon_identifier_count,
                         variant_strain_count = @variant_strain_count,
                         variant_transcript_count = @variant_transcript_count,
                         strain_analysis_count = @strain_analysis_count,
                         transcript_analysis_count = @transcript_analysis_count,
                         gene_count = @gene_count,
                         transcript_count = @transcript_count,
                         strain_count = @strain_count,
                         gene_analysis_count = @gene_analysis_count
    WHERE id = 1;
END