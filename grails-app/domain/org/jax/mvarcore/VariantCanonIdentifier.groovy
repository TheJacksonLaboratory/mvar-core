package org.jax.mvarcore

class VariantCanonIdentifier {

    String caID
    Long position
    String chr
    String ref
    String alt
    String variantRefTxt

    static constraints = {
        caID nullable: true
        variantRefTxt unique: true
        chr unique: ['position', 'ref', 'alt']
    }

    static mapping = {
        variantRefTxt index: true
        chr column: 'chr', sqlType: "char(3)"
        ref column: 'ref', sqlType: "char(100)"
        alt column: 'alt', sqlType: "char(100)"
    }

    def afterInsert() {
        withNewSession {
            //set canonical Id
            this.caID = 'MCA' + this.id.toString().padLeft(13, '0')
        }
    }

}