package org.jax.mvarcore

class Gene {

    String mgiId
    String symbol
    String name
    String description
    String chr
    String type
    String entrezGeneId
    String ensemblGeneId

    static constraints = {

        entrezGeneId nullable: true
        ensemblGeneId nullable:  true
        description nullable: true, sqlType: 'text'
        type nullable: true
    }

    static mapping = {
        symbol index:'symbol_idx'

    }

    static hasMany = [identifiers: Identifier, variants: Variant]

}
