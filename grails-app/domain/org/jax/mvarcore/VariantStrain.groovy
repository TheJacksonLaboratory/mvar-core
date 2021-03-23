package org.jax.mvarcore

class VariantStrain {

    String genotype
    String genotypeData

    static belongsTo = [strain: Strain, variant: Variant]
    static constraints = {
        genotype nullable: false
        genotypeData nullable: true
    }

    static mapping = {
//        strain index: 'strain_genotype_idx'
//        genotype index: 'strain_genotype_idx', sqlType: "varchar(4)"
        version false
    }
}
