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
    String parentVariantRefTxt //holds full variation change for the parent reference <chr_pos_ref_alt>  -- ref and alt is empty will have '.' as value
    boolean parentRefInd // should be set to true for GRCm38 assembly
    String dnaHgvsNotation
    String proteinHgvsNotation
    String externalId
    String externalSource


    static mapping = {
        parentVariantRefTxt index: true
        chr sqlType: "char(3)"
        ref sqlType: "char(100)"
        alt sqlType: "char(100)"
        version false
    }

    /**
     * before insert check that there is a parent reference if this variant is not the parent reference
     * @return
     */
 //   def beforeInsert(){

//        //find parent ref
//        if (! parentRefInd){
//
//            canonVarIdentifier = VariantCanonIdentifier.findByVariantRefTxt (this.parentVariantRefTxt)
//            if (! canonVarIdentifier){
//                return false
//            }
//        } else{
//
//          canonVarIdentifier = new VariantCanonIdentifier(variantRefTxt: parentVariantRefTxt)
//          canonVarIdentifier.save()
//        }

        //return true

 //   }

//    def afterInsert() {
//        withNewSession {
//
//            //set canonical Id
//            //this.caID = 'MCA' + this.id.toString().padLeft(13,'0')
//
//            //set varTxt
//            String refIn = this.ref ?: '.'
//            String altIn = this.alt ?: '.'
//            this.parentVariantRef = this.chr + '_' + this.position + '_' + refIn + "_" + altIn
//
//            this.save(failOnError: true)
//
//        }
//    }

    static hasMany = [strains: Strain, transcripts: Transcript]
    static hasOne = [gene: Gene]

    //static mappedBy = [hgvs: 'genomicRefSeqHgvs']


    static constraints = {

//        position unique: ['assembly', 'ref', 'alt']
        gene nullable: true
        functionalClassCode nullable: true, sqlType: 'text'
        accession nullable: true
        canonVarIdentifier nullable: false
        parentVariantRefTxt nullable: false
        parentRefInd nullable: false
        externalId nullable: true
        externalSource nullable: true
        dnaHgvsNotation nullable: true, sqlType: 'text'
        proteinHgvsNotation nullable: true, sqlType: 'text'
    }
}
