package org.jax.mvarcore

class VariantStrain {

    String genotype
    /* 0=non-imputed, 1=imputed-snpgrid, 2=imputed-mgi, 3=, 4=  */
    Integer imputed

    static belongsTo = [strain: Strain, variant: Variant]
    static constraints = {
        genotype nullable: false
        imputed nullable: true
    }

    static mapping = {
        genotype sqlType: "char(5)"
        imputed sqlType: "tinyint"
        version false
    }
}
