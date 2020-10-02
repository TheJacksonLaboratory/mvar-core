package org.jax.mvarcore

class Genotype {

    String format
    String data

    static hasOne = [strain: Strain, variant: Variant]

    static mapping = {
        format sqlType: "char(36)"
        data sqlType: "char(100)"
        version false
    }
    static constraints = {
        variant nullable: false
        strain nullable: false
        format nullable: false
        data nullable: false
    }
}
