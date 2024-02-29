package org.jax.mvarcore

class VariantTranscriptTemp {

    String variantRefTxt
    String transcriptIds
    String transcriptFeatureIds

    static mapping = {
        variantRefTxt sqlType: "varchar(700)"
        version false
    }
    static constraints = {
        variantRefTxt nullable: false
        transcriptIds nullable: false, sqlType: 'text'
        transcriptFeatureIds nullable: false, sqlType: 'text'
    }
}
