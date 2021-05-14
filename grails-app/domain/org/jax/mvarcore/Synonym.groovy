package org.jax.mvarcore


class Synonym {
    String name
    static mapping = {
        name index:'name_idx'
        version false
    }

    static constraints = {
        name nullable: true, sqlType: 'text'
    }

}
