package org.jax.mvarcore

class VariantStrain {

    String genotype
    /* 0=non-imputed, 1=imputed-snpgrid, 2=imputed-mgi, 3=, 4=   See Imputed table */
    byte imputed

    static belongsTo = [strain: Strain, variant: Variant]
    static constraints = {
        genotype nullable: false
        imputed nullable: false
    }

    static mapping = {
        genotype sqlType: "char(5)"
        version false
    }
}
