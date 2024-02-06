package org.jax.mvarcore

class MvarGene {

    String symbol

    Gene gene

    static constraints = {
        symbol nullable: false
    }
    static mapping = {
        sort symbol:'asc'
        version false
    }
}
