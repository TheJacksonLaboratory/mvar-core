package org.jax.mvarcore

class Source {

    String name
    String sourceVersion

    static mapping = {
        name sqlType: "char(50)"
        sourceVersion sqlType: "char(50)"
        version false
    }
    static constraints = {
        name nullable: false
        sourceVersion nullable: false
    }
}
