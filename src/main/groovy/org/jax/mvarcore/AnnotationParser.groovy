package org.jax.mvarcore

/**
 * Class used to parse the INFO column of a VCF variant row entry with the ANN id
 */
class AnnotationParser extends InfoParser {

    // ##INFO=<ID=ANN,Number=.,Type=String,
    // Description="Functional annotations:
    // Allele   |Annotation                         |Annotation_Impact  |Gene_Name      |Gene_ID|Feature_Type   |Feature_ID             |Transcript_BioType |Rank   |HGVS.c         |HGVS.p |cDNA.pos / cDNA.length |CDS.pos / CDS.length   |AA.pos / AA.length |Distance   |ERRORS / WARNINGS / INFO'">
    // G        |intergenic_variant                 |MODIFIER           |4933401J01Rik  |.      |transcript     |ENSMUST00000193812.1   |Noncoding          |       |               |       |                       |                       |                   |72811      |;CSQ=G                     ||||intergenic_variant||||||||;DP=32;DP4=0,0,19,13
    // C        |intergenic_variant                 |MODIFIER           |Mid1-ps1       |.      |transcript     |ENSMUST00000167967.3   |Noncoding          |       |               |       |                       |                       |                   |5523       |;CSQ=C                     ||||intergenic_variant||||||||;DP=275;DP4=107,44,67,57
    // C        |downstream_gene_variant            |MODIFIER           |Mid1-ps1       |.      |transcript     |ENSMUST00000167967.3   |Noncoding          |       |               |       |                       |                       |                   |4279       |;CSQ=C                     |ENSMUSG00000095134|ENSMUST00000167967|Transcript|downstream_gene_variant|||||||4280|1;DP=229;DP4=44,89,88,8
    // C        |coding_transcript_intron_variant   |LOW                |Xkr4           |497097 |transcript     |ENSMUST00000070533.4   |Coding             |2/2    |c.998-17145A>G |p.(%3D)|1148/457017            |998/1944               |333/648            |           |;CSQ=C                     |ENSMUSG00000051951|ENSMUST00000070533|Transcript|intron_variant||||||||-1;DP=66;DP4=18,22,7,19
    // T        |5_prime_UTR_exon_variant           |LOW                |Rrs1           |59014  |transcript     |ENSMUST00000072079.8   |Coding             |1/1    |c.-8C>T        |p.(%3D)|109/2048               |1/1098                 |1/366              |           |;CSQ=T                     |ENSMUSG00000025911|ENSMUST00000186467|Transcript|upstream_gene_variant|||||||2591|1,T|ENSMUSG00000061024|ENSMUST00000072079|Transcript|5_prime_UTR_variant|109|||||||1,T|ENSMUSG00000025911|ENSMUST00000190654|Transcript|upstream_gene_variant|||||||2603|1,T|ENSMUSG00000025911|ENSMUST00000130927|Transcript|upstream_gene_variant|||||||2576|1,T|ENSMUSG00000025911|ENSMUST00000027044|Transcript|upstream_gene_variant|||||||2539|1,T|ENSMUSG00000025911|ENSMUST00000144177|Transcript|upstream_gene_variant|||||||2432|1;DP=38;DP4=9,17,10,2
    private String allele
    private String annotation
    private String annotationImpact
    private String geneName
    private int geneId
    private String featureType
    private String featureId
    private String transcriptBiotype
    private String rank
    private String hgvsC
    private String hgvsP
    private String cDNApos
    private String cDNAlength
    private String cdsPos
    private String cdsLength
    private String aaPos
    private String aaLength
    private String distance
    private String info

    AnnotationParser(String infoString) {
        super(infoString)
    }

    @Override
    String getInfoId() {
        return "ANN"
    }

    @Override
    int getInfoLength() {
        return 19
    }


    String getAllele() {
        allele = infoArray[0]
        return allele
    }

    String getAnnotation() {
        annotation = infoArray[1]
        return annotation
    }

    String getAnnotationImpact() {
        return annotationImpact = infoArray[2]
    }

    String getGeneName() {
        return geneName = infoArray[3]
    }

    String getGeneId() {
        return geneId = infoArray[4]
    }

    String getFeatureType() {
        return featureType = infoArray[5]
    }

    String getFeatureId() {
        return featureId = infoArray[6]
    }

    String getTranscriptBiotype() {
        return transcriptBiotype = infoArray[7]
    }

    String getRank() {
        return rank = infoArray[8]
    }

    String getHGVSC() {
        return hgvsC = infoArray[9]
    }

    String getHGVSP() {
        return hgvsP = infoArray[10]
    }

    String getCDNAPos() {
        return cDNApos = infoArray[11]
    }

    String getCDNALength() {
        return cDNAlength = infoArray[12]
    }

    String getCdsPos() {
        return cdsPos = infoArray[13]
    }

    String getCdsLength() {
        return cdsLength = infoArray[14]
    }

    String getAaPos() {
        return aaPos = infoArray[15]
    }

    String getAaLength() {
        return aaLength = infoArray[16]
    }

    String getDistance() {
        return distance = infoArray[17]
    }

    String getInfo() {
        return info = infoArray[18]
    }
}


