package org.jax.mvarcore

class Strain {

    String name

    static hasMany = [identifiers : Identifier]

    static constraints = {
        name nullable: false
    }
}
