package org.jax.mvarcore


class Transcript {

    String primaryIdentifier
    int length
    String chromosome
    Long locationStart
    Long locationEnd
    String geneIdentifier

    static mapping = {
        primaryIdentifier index: true
        geneIdentifier index: true
        version false
    }

    static constraints = {
        primaryIdentifier nullable: false
        length nullable: false
        chromosome nullable: false
        locationStart nullable: false
        locationEnd nullable: false
        geneIdentifier nullable: true
    }
}
