package org.jax.mvarcore

class MvarStrain {

    String name

    Strain strain

    static constraints = {
    }
    static mapping = {
        version false
        sort name:"asc"
    }

    static namedQueries = {
        mvarStrainsWithImputed { anImputed ->
            imputeds {
                eq 'imputed', anImputed.imputed
            }
        }
    }
}
