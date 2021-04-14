package org.jax.mvarcore


class Transcript {

    String primaryIdentifier
    String mRnaId
    String geneSymbol
    String description

    static mapping = {
        primaryIdentifier index: true
        mRnaId index: true
        geneSymbol index: true
        version false
    }

    static constraints = {
        primaryIdentifier nullable: false, unique: true
        mRnaId nullable: true
        geneSymbol nullable: false
        description nullable: true
    }
}
