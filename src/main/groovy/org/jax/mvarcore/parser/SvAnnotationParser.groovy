package org.jax.mvarcore.parser

class SvAnnotationParser extends InfoParser {

    def listOfSvann

    /**
     * ##INFO=<ID=SVANN,Number=1,Type=String,Description="Functional SV Annotation:'Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|ERRORS / WARNINGS / INFO'">
     * @param infoString
     */
    SvAnnotationParser(String infoString) {
        super(infoString)
    }

    @Override
    String getInfoId() {
        return "SVANN"
    }

    @Override
    Object parse() {
        // ##INFO=<ID=SVANN,Number=1,Type=String,Description="Functional SV Annotation:'Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|ERRORS / WARNINGS / INFO'">
        listOfSvann = super.parse()
    }

    @Override
    List getAnnotationKeys() {
        return ["Allele", "Gene", "Feature", "Feature_type",
                "Consequence", "cDNA_position", "CDS_position", "Protein_position"]
    }
}
