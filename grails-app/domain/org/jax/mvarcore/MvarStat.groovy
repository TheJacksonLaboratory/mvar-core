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
    int sourceCount
    int assemblyCount

    static mapping = {
        version false
    }
    static constraints = {
    }
}
