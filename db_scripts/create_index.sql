USE mvar_core;

SELECT 'CREATING INDEX FOR VARIANT_STRAIN TABLE';
CREATE INDEX strain_genotype_index ON variant_strain (strain_id, genotype);

COMMIT;