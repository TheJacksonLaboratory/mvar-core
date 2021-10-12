package org.jax.mvarcore

class Imputed {

    String source
    /* 0=non-imputed, 1=imputed-snpgrid, 2=imputed-mgi, 3=, 4=  */
    byte imputed

    static constraints = {
        source nullable: false
        imputed nullable: false
    }

    static mapping = {
        version false
    }
}
