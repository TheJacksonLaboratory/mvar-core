package org.jax.mvarcore.parser

/**
 * ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type from Ensembl 78 as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND">
 */
class ConsequenceParser extends InfoParser {

    @Override
    String getInfoId() {
        return "CSQ"
    }

    @Override
    Object parse(String infoString) {
        // Consequence type from Ensembl 78 as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND
        return super.parse(infoString)
    }

    @Override
    List getAnnotationKeys() {
        return ["Allele", "Gene", "Feature", "Feature_type", "Consequence", "cDNA_position",
                "CDS_position", "Protein_position", "Amino_acids", "Codons", "Existing_variation",
                "DISTANCE", "STRAND"]
    }
}
