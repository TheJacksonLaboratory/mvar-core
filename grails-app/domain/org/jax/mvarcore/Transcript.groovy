package org.jax.mvarcore


class Transcript {

    String name
    String referenceAccession
    String proteinChange

    static hasMany = [hgvs: Hgvs, proteinEffectHgvs: Hgvs]
    //static mappedBy = [hgvs: "codingDnaRefSeqHgvs", proteinEffect: 'proteinRefSeqHgvs']
    static belongsTo = [variant: Variant]
    static constraints = {
    }
}
