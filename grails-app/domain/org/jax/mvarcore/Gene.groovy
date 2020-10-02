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
        mgiId unique: true
        entrezGeneId nullable: true
        ensemblGeneId nullable:  true
        description nullable: true, sqlType: 'text'
        name nullable: true, sqlType: 'text'
        type nullable: true
    }

    static mapping = {
        mgiId index: 'mgi_id_idx'
        symbol index:'symbol_idx'
    }

    static hasMany = [synonyms: Synonym, transcripts: Transcript]

}
