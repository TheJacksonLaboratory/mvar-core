package org.jax.mvarcore

import org.jax.mvarcore.parser.AnnotationParser
import org.junit.Before


class AnnotationParserTestCase extends GroovyTestCase {

    AnnotationParser parser
    String WRONG_ID = "TEST=C|coding_transcript_intron_variant|LOW|Kcnq5|226922|transcript|ENSMUST00000029667.12|Coding|1/13|c.402-31248T>G|p.(%3D)|402/563179|402/2802|134/934||;CSQ=C|ENSMUSG00000028033|ENSMUST00000174183|Transcript|intron_variant&NMD_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000115300|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000115299|Transcript|intron_variant&non_coding_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000134505|Transcript|intron_variant&non_coding_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000173058|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000173404|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000029667|Transcript|intron_variant||||||||-1;DP=31;DP4=0,0,14,17"
    String SHORT_INFO_STRING = "ANN=C|coding_transcript_intron_variant|LOW|Kcnq5|226922|transcript|ENSMUST00000029667.12|Coding|1/13|c.402-31248T>G|p"
    String INFO_STRING = "ANN=G|coding_transcript_intron_variant|LOW|Xkr4|497097|transcript|ENSMUST00000070533.4|Coding|1/2|c.798-84265A>C|p.(%3D)|948/457017|798/1944|266/648||,G|non_coding_transcript_intron_variant|LOW|Gm1992|.|transcript|ENSMUST00000161581.1|Noncoding|1/1|n.102-7239T>G||102/46967||||;CSQ=G|ENSMUSG00000051951|ENSMUST00000070533|Transcript|intron_variant||||||||-1,G|ENSMUSG00000089699|ENSMUST00000161581|Transcript|intron_variant&non_coding_transcript_variant||||||||1;DP=31;DP4=10,17,2,2\tGT:GQ:DP:MQ0F:GP:PL:AN:MQ:DV:DP4:SP:SGB:PV4:FI\t0/1:31:31:0:31,0,266:37,0,255:2:60:4:10,17,2,2:2:-0.556411:.:0"
    String INFO_STRING2 = "ANN=T|coding_transcript_intron_variant|LOW|Rp1|19888|transcript|ENSMUST00000208660.1|Coding|8/29|c.1441+3998T>A|p.(%3D)|1496/409685|1442/4116|481/1372||,T|non_coding_transcript_intron_variant|LOW|Gm6101|.|transcript|ENSMUST00000195384.1|Noncoding|1/1|n.1674-1102T>A||1674/4286||||;CSQ=T|ENSMUSG00000102948|ENSMUST00000195384|Transcript|intron_variant&non_coding_transcript_variant||||||||-1;DP=35;DP4=0,0,14,21\tGT:GQ:DP:MQ0F:GP:PL:AN:MQ:DV:DP4:SP:SGB:PV4:FI\t1/1:117:35:0:285,117,0:255,105,0:2:45:35:0,0,14,21:0:-0.693136:.:1"

    @Before
    void setUp() {
        parser = new AnnotationParser(INFO_STRING)
    }

    void testTooShortInfoString() {
        boolean exceptionCaught = false
        String exceptionMsg = ''
        try {
            new AnnotationParser(SHORT_INFO_STRING)
        } catch (IllegalArgumentException exception) {
            exceptionCaught = true
            exceptionMsg = exception.getMessage()
        }
        assert exceptionCaught
        assert exceptionMsg == "Expecting ANN identifier to have 16 blocks. Had 11 instead."

    }

    void testWrongInfoId() {
        boolean exceptionCaught = false
        String exceptionMsg = ''
        try {
            new AnnotationParser(WRONG_ID)
        } catch (IllegalArgumentException exception) {
            exceptionCaught = true
            exceptionMsg = exception.getMessage()
        }
        assert exceptionCaught
        assert exceptionMsg == "This INFO string does not have the ANN id."
    }

    void testAllele() {
        assert parser.listOfAnnMap.get(0)["Allele"] == "G"
    }

    void testAnnotation() {
        assert parser.listOfAnnMap.get(0)["Annotation"] == "coding_transcript_intron_variant"
    }

    void testGeneName() {
        assert parser.listOfAnnMap.get(0)["Gene_Name"] == "Xkr4"
    }

    void testHGVSc() {
        assert parser.listOfAnnMap.get(0)["HGVS.c"] == "c.798-84265A>C"
    }

    void testHGVSp() {
        assert parser.listOfAnnMap.get(0)["HGVS.p"] == "p.(%3D)"
    }
}
