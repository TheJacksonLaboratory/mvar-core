package org.jax.mvarcore

class MvarGene {

    String symbol

    Gene gene

    static constraints = {
    }
    static mapping = {
        version: false
        sort symbol:"asc"
    }
}
