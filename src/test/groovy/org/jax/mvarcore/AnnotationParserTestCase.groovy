package org.jax.mvarcore

import org.junit.Before

import javax.naming.directory.InvalidAttributesException

class AnnotationParserTestCase extends GroovyTestCase {

    AnnotationParser parser
    String WRONG_ID = "TEST=C|coding_transcript_intron_variant|LOW|Kcnq5|226922|transcript|ENSMUST00000029667.12|Coding|1/13|c.402-31248T>G|p.(%3D)|402/563179|402/2802|134/934||;CSQ=C|ENSMUSG00000028033|ENSMUST00000174183|Transcript|intron_variant&NMD_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000115300|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000115299|Transcript|intron_variant&non_coding_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000134505|Transcript|intron_variant&non_coding_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000173058|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000173404|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000029667|Transcript|intron_variant||||||||-1;DP=31;DP4=0,0,14,17"
    String SHORT_INFO_STRING = "ANN=C|coding_transcript_intron_variant|LOW|Kcnq5|226922|transcript|ENSMUST00000029667.12|Coding|1/13|c.402-31248T>G|p"
    String INFO_STRING = "ANN=C|coding_transcript_intron_variant|LOW|Kcnq5|226922|transcript|ENSMUST00000029667.12|Coding|1/13|c.402-31248T>G|p.(%3D)|402/563179|402/2802|134/934||;CSQ=C|ENSMUSG00000028033|ENSMUST00000174183|Transcript|intron_variant&NMD_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000115300|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000115299|Transcript|intron_variant&non_coding_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000134505|Transcript|intron_variant&non_coding_transcript_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000173058|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000173404|Transcript|intron_variant||||||||-1,C|ENSMUSG00000028033|ENSMUST00000029667|Transcript|intron_variant||||||||-1;DP=31;DP4=0,0,14,17"

    @Before
    void setUp() {
        parser = new AnnotationParser(INFO_STRING)
    }

    void testTooShortInfoString() {
        boolean exceptionCaught = false
        String exceptionMsg
        AnnotationParser localParser
        try {
            localParser = new AnnotationParser(SHORT_INFO_STRING)
        } catch (InvalidAttributesException exception) {
            exceptionCaught = true
            exceptionMsg = exception.getMessage()
        }
        assert exceptionCaught == true
        assert exceptionMsg == "This INFO string doesn't have the expected number of attributes : is 11; should be 19"

    }

    void testWrongInfoId() {
        boolean exceptionCaught = false
        String exceptionMsg
        AnnotationParser localParser
        try {
            localParser = new AnnotationParser(WRONG_ID)
        } catch (InvalidAttributesException exception) {
            exceptionCaught = true
            exceptionMsg = exception.getMessage()
        }
        assert exceptionCaught == true
        assert exceptionMsg == "This INFO string does not have the ANN id."
    }

    void testAllele() {
        assert parser.getAllele() == "C"
    }

    void testAnnotation() {
        assert parser.getAnnotation() == "coding_transcript_intron_variant"
    }

    void testGeneName() {
        assert parser.getGeneName() == "Kcnq5"
    }

    void testHGVSc() {
        assert parser.getHGVSC() == "c.402-31248T>G"
    }

    void testHGVSp() {
        assert parser.getHGVSP() == "p.(%3D)"
    }
}
