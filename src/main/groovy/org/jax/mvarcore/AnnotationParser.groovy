package org.jax.mvarcore

/**
 * Class used to parse the INFO column of a VCF variant row entry with the ANN id
 * See http://snpeff.sourceforge.net/VCFannotationformat_v1.0.pdf for more infos on the full ANN standard
 */
class AnnotationParser extends InfoParser {

    // ##INFO=<ID=ANN,Number=.,Type=String,
    // Description="Functional annotations:
    // 1        |2                                  |3                  |4              |4      |5              |6                      |7                  |8      |9              |10
    // Allele   |Annotation                         |Annotation_Impact  |Gene_Name      |Gene_ID|Feature_Type   |Feature_ID             |Transcript_BioType |Rank   |HGVS.c         |HGVS.p |cDNA.pos / cDNA.length |CDS.pos / CDS.length   |AA.pos / AA.length |Distance   |ERRORS / WARNINGS / INFO'">
    // G        |intergenic_variant                 |MODIFIER           |4933401J01Rik  |.      |transcript     |ENSMUST00000193812.1   |Noncoding          |       |               |       |                       |                       |                   |72811      |;CSQ=G                     ||||intergenic_variant||||||||;DP=32;DP4=0,0,19,13
    // C        |intergenic_variant                 |MODIFIER           |Mid1-ps1       |.      |transcript     |ENSMUST00000167967.3   |Noncoding          |       |               |       |                       |                       |                   |5523       |;CSQ=C                     ||||intergenic_variant||||||||;DP=275;DP4=107,44,67,57
    // C        |downstream_gene_variant            |MODIFIER           |Mid1-ps1       |.      |transcript     |ENSMUST00000167967.3   |Noncoding          |       |               |       |                       |                       |                   |4279       |;CSQ=C                     |ENSMUSG00000095134|ENSMUST00000167967|Transcript|downstream_gene_variant|||||||4280|1;DP=229;DP4=44,89,88,8
    // C        |coding_transcript_intron_variant   |LOW                |Xkr4           |497097 |transcript     |ENSMUST00000070533.4   |Coding             |2/2    |c.998-17145A>G |p.(%3D)|1148/457017            |998/1944               |333/648            |           |;CSQ=C                     |ENSMUSG00000051951|ENSMUST00000070533|Transcript|intron_variant||||||||-1;DP=66;DP4=18,22,7,19
    // T        |5_prime_UTR_exon_variant           |LOW                |Rrs1           |59014  |transcript     |ENSMUST00000072079.8   |Coding             |1/1    |c.-8C>T        |p.(%3D)|109/2048               |1/1098                 |1/366              |           |;CSQ=T                     |ENSMUSG00000025911|ENSMUST00000186467|Transcript|upstream_gene_variant|||||||2591|1,T|ENSMUSG00000061024|ENSMUST00000072079|Transcript|5_prime_UTR_variant|109|||||||1,T|ENSMUSG00000025911|ENSMUST00000190654|Transcript|upstream_gene_variant|||||||2603|1,T|ENSMUSG00000025911|ENSMUST00000130927|Transcript|upstream_gene_variant|||||||2576|1,T|ENSMUSG00000025911|ENSMUST00000027044|Transcript|upstream_gene_variant|||||||2539|1,T|ENSMUSG00000025911|ENSMUST00000144177|Transcript|upstream_gene_variant|||||||2432|1;DP=38;DP4=9,17,10,2
    String[] allele
    String[] annotation
    String[] annotationImpact
    String[] geneName
    String[] geneId
    String[] featureType
    String[] featureId
    String[] transcriptBiotype
    String[] rank
    String[] hgvsC
    String[] hgvsP
    String[] cDNApos
    String[] cDNAlength
    String[] cdsPos
    String[] cdsLength
    String[] aaPos
    String[] aaLength
    String[] distance
    String[] info

    AnnotationParser(String infoString) {
        super(infoString)
        for(int i = 0; i < listOfInfoArray.size(); i++) {
            allele[i] = listOfInfoArray.get(i)[0]
            annotation[i] = listOfInfoArray.get(i)[1]
            annotationImpact[i] = listOfInfoArray.get(i)[2]
            geneName[i] = listOfInfoArray.get(i)[3]
            geneId[i] = listOfInfoArray.get(i)[4]
            featureType[i] = listOfInfoArray.get(i)[5]
            featureId[i] = listOfInfoArray.get(i)[6]
            transcriptBiotype[i] = listOfInfoArray.get(i)[7]
            rank[i] = listOfInfoArray.get(i)[8]
            hgvsC[i] = listOfInfoArray.get(i)[9]
            hgvsP[i] = listOfInfoArray.get(i)[10]
            cDNApos[i] = listOfInfoArray.get(i).length > 11 ? listOfInfoArray.get(i)[11] : ""
            cDNAlength[i] = listOfInfoArray.get(i).length > 12 ? listOfInfoArray.get(i)[12] : ""
            cdsPos[i] = listOfInfoArray.get(i).length > 13 ? listOfInfoArray.get(i)[13] : ""
            cdsLength[i] = listOfInfoArray.get(i).length > 14 ? listOfInfoArray.get(i)[14] : ""
            aaPos[i] = listOfInfoArray.get(i).length > 15 ? listOfInfoArray.get(i)[15] : ""
            aaLength[i] = listOfInfoArray.get(i).length > 16 ? listOfInfoArray.get(i)[16] : ""
            distance[i] = listOfInfoArray.get(i).length > 17 ? listOfInfoArray.get(i)[17] : ""
            info[i] = listOfInfoArray.get(i).length > 18 ? listOfInfoArray.get(i)[18] : ""
        }
    }

    @Override
    String getInfoId() {
        return "ANN"
    }

    @Override
    int getInfoLength() {
        return 19
    }

}
