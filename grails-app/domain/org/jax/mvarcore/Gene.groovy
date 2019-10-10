package org.jax.mvarcore

class Gene {

    String name
    String chromosome

    static hasMany = [indentifiers: Identifier, variants: Variant]

    static constraints = {
    }
}
