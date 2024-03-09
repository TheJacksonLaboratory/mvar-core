package org.jax.mvarcore


class Synonym {
    String name

    static constraints = {
        name nullable: true, sqlType: 'text'
    }

    static mapping = {
        name index:'name_idx'
        version false
    }
}
