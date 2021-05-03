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
//        chr unique: ['position', 'ref', 'alt']
    }

    static mapping = {
        variantRefTxt index: true
        caID index: true
        chr column: 'chr', sqlType: "char(3)"
        ref column: 'ref', sqlType: "char(100)"
        variantRefTxt sqlType: "varchar(350)"

    }

    def afterInsert() {
        withNewSession {
            //set canonical Id
            this.caID = 'MCA' + this.id.toString().padLeft(13, '0')
        }
    }

}
