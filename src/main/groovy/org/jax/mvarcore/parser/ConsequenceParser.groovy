package org.jax.mvarcore.parser

class ConsequenceParser extends InfoParser {

    def listOfCsqMap

    /**
     * ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type from Ensembl 78 as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND">
     * @param infoString
     */
    ConsequenceParser(String infoString) {
        super(infoString)
    }

    @Override
    String getInfoId() {
        return "CSQ"
    }

    @Override
    Object parse() {
        // Consequence type from Ensembl 78 as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND
        listOfCsqMap = super.parse()
    }

    @Override
    List getAnnotationKeys() {
        return ["Allele", "Gene", "Feature", "Feature_type", "Consequence", "cDNA_position",
                "CDS_position", "Protein_position", "Amino_acids", "Codons", "Existing_variation",
                "DISTANCE", "STRAND"]
    }
}
