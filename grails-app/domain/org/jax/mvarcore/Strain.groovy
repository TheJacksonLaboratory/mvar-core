package org.jax.mvarcore

class Strain {

    String name

    static hasMany = [indentifiers : Identifier]

    static constraints = {
        name nullable: false
    }
}
