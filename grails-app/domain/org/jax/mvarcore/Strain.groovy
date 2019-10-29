package org.jax.mvarcore

class Strain {

    String name
    String description

    static hasMany = [indentifiers : Identifier]

    static constraints = {
        name nullable: false
    }
}
