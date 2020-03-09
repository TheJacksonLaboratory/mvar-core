package org.jax.mvarcore.parser

class SvAnnotationParser extends InfoParser {

    @Override
    String getInfoId() {
        return "SVANN"
    }

    /**
     * Parse string in the following form:
     *  ##INFO=<ID=SVANN,Number=1,Type=String,Description="Functional SV Annotation:'Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|ERRORS / WARNINGS / INFO'">
     * @param infoString
     * @return
     */
    @Override
    List<Map> parse(String infoString) {
        // ##INFO=<ID=SVANN,Number=1,Type=String,Description="Functional SV Annotation:'Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|ERRORS / WARNINGS / INFO'">
        return super.parse(infoString)
    }

    @Override
    List getAnnotationKeys() {
        return ["Allele", "Gene", "Feature", "Feature_type",
                "Consequence", "cDNA_position", "CDS_position", "Protein_position"]
    }
}
