package org.jax.mvarcore

class VariantCanonIdentifier {

    String caID
    String variantRefTxt

    static constraints = {
        caID nullable: true
    }

    static mapping = {
        variantRefTxt index: true
        caID index: true
        variantRefTxt sqlType: "varchar(350)"
        version false
    }

    def afterInsert() {
        withNewSession {
            //set canonical Id
            this.caID = 'MCA' + this.id.toString().padLeft(13, '0')
        }
    }

}
