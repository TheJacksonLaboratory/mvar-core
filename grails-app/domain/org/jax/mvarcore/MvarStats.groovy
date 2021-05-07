package org.jax.mvarcore

class MvarStats {

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
    
    static constraints = {
    }
}
