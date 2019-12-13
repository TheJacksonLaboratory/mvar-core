package org.jax.mvarcore

class Strain implements IMouseMineObject {

    String identifier
    String name
    String description
    String carriesAlleleSymbol
    String carriesAlleleName
    String carriesAlleleType
    String carriesAlleleIdentifier

    static hasMany = [identifiers : Identifier]

    static constraints = {
        name nullable: false
        carriesAlleleType nullable:true
        description nullable: true
    }

    static mapping = {
        name index:'name_idx'
        identifier index:'identifier_idx'
    }
}
