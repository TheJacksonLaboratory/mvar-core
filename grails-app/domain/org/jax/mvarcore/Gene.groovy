package org.jax.mvarcore

class Gene {

    String name
    String chromosome

    static hasMany = [identifiers: Identifier, variants: Variant]

    static constraints = {
    }
}
