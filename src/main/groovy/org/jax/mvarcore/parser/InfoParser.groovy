package org.jax.mvarcore.parser


/**
 * This class is the base class to parse the INFO column from a variant entry.
 */
abstract class InfoParser {

    String infoString
    String[] infos

    /**
     * ##INFO=<ID=ANN,Number=1,Type=String,Description="Functional annotations:'Allele|Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|Rank|HGVS.c|HGVS.p|cDNA.pos / cDNA.length|CDS.pos / CDS.length|AA.pos / AA.length|Distance|ERRORS / WARNINGS / INFO'">
     * ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type from Ensembl 78 as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND">
     * ##INFO=<ID=DP,Number=1,Type=Integer,Description="Raw read depth">
     * ##INFO=<ID=DP4,Number=4,Type=Integer,Description="Total Number of high-quality ref-fwd, ref-reverse, alt-fwd and alt-reverse bases">
     * ##INFO=<ID=INDEL,Number=0,Type=Flag,Description="Indicates that the variant is an INDEL.">
     * ##INFO=<ID=SVANN,Number=1,Type=String,Description="Functional SV Annotation:'Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|ERRORS / WARNINGS / INFO'">
     * @param infoString
     */
    InfoParser(String infoString) {
        this.infoString = infoString
        if (!this.infoString.contains(getInfoId())) {
            throw new IllegalArgumentException("This INFO string does not have the " + getInfoId() + " id.")
        }
        parse()
    }

    /**
     * Returns the ID for the INFO column. To be implemented in child class.
     * @return
     */
    abstract String getInfoId()

    /**
     * Returns the expected length of this functional annotation
     * @return
     */
    int getInfoLength() {
        return getAnnotationKeys().size()
    }

    /**
     * Returns the expected object for the given ID implementation parser.
     * Can be overriden if necessary
     * @return
     */
    Object parse() {
        // split by " 'id'= "
        infos = infoString.split(getInfoId() + "=")
        def listOfAnnMap = []
        if (infos.size() > 1) {
            // split string by commas: a comma in the jannovar string separates multiple transcripts
            String[] functAnnotations = infos[1].split(';')[0].split(',')
            for (int i = 0; i < functAnnotations.size(); i++) {
                String[] infoAnnArray = functAnnotations[i].split("\\|", -1)
                if (infoAnnArray.size() != getInfoLength()) {
                    throw new IllegalArgumentException("Expecting " + getInfoId() + " identifier to have " + getInfoLength() + " blocks. Had " + infoAnnArray.size() + " instead.")
                }
                def annMap = [:]
                getAnnotationKeys().eachWithIndex { annotationKey, index ->
                    annMap[annotationKey] = infoAnnArray[index]
                }
                listOfAnnMap.add(annMap)
            }
        }
        return listOfAnnMap
    }

    /**
     * List of annotation keys for the given implementation
     * @return
     */
    abstract List getAnnotationKeys()

}