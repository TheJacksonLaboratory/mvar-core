package org.jax.mvarcore

class MvarStat {

    int alleleCount
    int geneCount
    int strainCount
    int transcriptCount
    int variantCount
    int variantCanonIdentifierCount

    Long variantStrainCount
    Long variantTranscriptCount

    int strainAnalysisCount
    int geneAnalysisCount
    int transcriptAnalysisCount

    static mapping = {
        version false
    }
    static constraints = {
    }
}
