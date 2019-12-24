package org.jax.mvarcore.parser

class FullInfoParser {

    def infosDataMap = [:]

    /**
     * ##INFO=<ID=ANN,Number=1,Type=String,Description="Functional annotations:'Allele|Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|Rank|HGVS.c|HGVS.p|cDNA.pos / cDNA.length|CDS.pos / CDS.length|AA.pos / AA.length|Distance|ERRORS / WARNINGS / INFO'">
     * ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type from Ensembl 78 as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND">
     * ##INFO=<ID=DP,Number=1,Type=Integer,Description="Raw read depth">
     * ##INFO=<ID=DP4,Number=4,Type=Integer,Description="Total Number of high-quality ref-fwd, ref-reverse, alt-fwd and alt-reverse bases">
     * ##INFO=<ID=INDEL,Number=0,Type=Flag,Description="Indicates that the variant is an INDEL.">
     * ##INFO=<ID=SVANN,Number=1,Type=String,Description="Functional SV Annotation:'Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|ERRORS / WARNINGS / INFO'">
     * @param infoString
     */
    FullInfoParser(String infoString) {
        AnnotationParser annParser = new AnnotationParser(infoString)
        infosDataMap['ANN'] = annParser.listOfAnnMap
        ConsequenceParser csqParser = new ConsequenceParser(infoString)
        infosDataMap['CSQ'] = csqParser.listOfCsqMap
        DP4Parser dp4Parser = new DP4Parser(infoString)
        infosDataMap['DP4'] = dp4Parser.dp4Map
        DPParser dpParser = new DPParser(infoString)
        infosDataMap['DP'] = dpParser.dpMap
        SvAnnotationParser svAnnotParser = new SvAnnotationParser(infoString)
        infosDataMap['SVANN'] = svAnnotParser.listOfSvann
    }

}
