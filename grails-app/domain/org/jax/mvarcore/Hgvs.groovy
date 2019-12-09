package org.jax.mvarcore

class Hgvs {

    //    Variant genomicRefSeqHgvs
    //    Transcript codingDnaRefSeqHgvs
    //    Transcript proteinRefSeqHgvs
//    String refAccession
//    String description
    String dnaHgvsNotation
    String proteinHgvsNotation

    static constraints = {
        dnaHgvsNotation nullable: true
        proteinHgvsNotation nullable: true
    }

//    static belongsTo = [variant: Variant]

}
