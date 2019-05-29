package org.jax.mvarcore

class Allele {

    String name
    String chromosome

    static hasMany = [indentifiers: Identifier, variants: Variant]

    static constraints = {
    }
}
