package org.jax.mvarcore

class Variant {

    // canonical ID
    VariantCanonIdentifier canonVarIdentifier
    Long position
    String chr
    String ref
    String alt
    String type
    String functionalClassCode
    String assembly
    String accession
    String variantRefTxt
//    String parentVariantRefTxt //holds full variation change for the parent reference <chr_pos_ref_alt>  -- ref and alt is empty will have '.' as value
    boolean parentRefInd // should be set to true for GRCm38 assembly
    String variantHgvsNotation
    String dnaHgvsNotation
    String proteinHgvsNotation
    String impact
    String externalId
    String externalSource
    String proteinPosition
    String aminoAcidChange
    Gene gene

    static mapping = {
        position index: true
		variantHgvsNotation index: true
        variantRefTxt index: true
        functionalClassCode index: true
        chr sqlType: "char(3)"
        ref sqlType: "char(100)"
        variantRefTxt sqlType: "varchar(350)"
        assembly sqlType: "varchar(12)"
        type sqlType: "varchar(6)"
        accession sqlType: "varchar(100)"
//        parentVariantRefTxt sqlType: "varchar(350)"
        version false
    }

    static hasMany = [variantStrains: VariantStrain, transcripts: Transcript, sources: Source]

    static constraints = {
//        position unique: ['assembly', 'ref', 'alt']
        id size: 1..4500000000
        gene nullable: true
        functionalClassCode nullable: true, sqlType: 'text'
        assembly nullable: false
        accession nullable: true
        canonVarIdentifier nullable: false
//        parentVariantRefTxt nullable: false
        parentRefInd nullable: false
        externalId nullable: true
        externalSource nullable: true
        variantHgvsNotation nullable: true
        dnaHgvsNotation nullable: true, sqlType: 'text'
        proteinHgvsNotation nullable: true, sqlType: 'text'
        impact nullable: true, sqlType: 'text'
    }
}
