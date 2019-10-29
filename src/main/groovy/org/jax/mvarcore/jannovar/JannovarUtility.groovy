package org.jax.mvarcore.jannovar;

import com.google.common.collect.ImmutableMap
import de.charite.compbio.jannovar.JannovarException
import de.charite.compbio.jannovar.annotation.VariantAnnotations
import de.charite.compbio.jannovar.annotation.VariantAnnotator
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions
import de.charite.compbio.jannovar.data.Chromosome
import de.charite.compbio.jannovar.data.JannovarData
import de.charite.compbio.jannovar.data.JannovarDataSerializer
import de.charite.compbio.jannovar.data.ReferenceDictionary
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator
import de.charite.compbio.jannovar.reference.GenomePosition
import de.charite.compbio.jannovar.reference.GenomeVariant
import de.charite.compbio.jannovar.reference.PositionType
import de.charite.compbio.jannovar.reference.Strand

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 */
class JannovarUtility {


    /** {@link de.charite.compbio.jannovar.data.ReferenceDictionary} with genome information. */
    private static ReferenceDictionary refDict

    /** Map of Chromosomes, used in the annotation. */
    private static ImmutableMap<Integer, Chromosome> chromosomeMap
    /** {@link de.charite.compbio.jannovar.data.JannovarData} with the information */

    private static JannovarData jannovarData
    /** Configuration */


    private static Map<String, List<String>> chromoseReferencesMap = [:]

    private static VariantContextAnnotator annotator


    static void init() {
        if (jannovarData == null) {
            loadReference()
        }
    }

    static final String referenceFile = "src/main/resources/mm10_ucsc.ser"

    static void loadReference() {

        deserializeTranscriptDefinitionFile()
        loadChromosomeReferenceMap()

    }

    private static void deserializeTranscriptDefinitionFile()
            throws JannovarException {
        jannovarData = new JannovarDataSerializer(referenceFile).load()
        refDict = jannovarData.getRefDict()
        chromosomeMap = jannovarData.getChromosomes()
        VariantContextAnnotator.Options opts = new VariantContextAnnotator.Options()
        annotator = new VariantContextAnnotator(refDict, chromosomeMap, opts)

    }

    private static void loadChromosomeReferenceMap() {
        //println("chromosome contig = " + this.chromosomeMap.getAt(1).getRefDict().getContigNameToID().findAll { val, name -> name == 1 }.toString())

        Integer chrId = 1
        List<String> referenceList = []
        chromosomeMap.getAt(1).getRefDict().getContigNameToID().each { val, name ->

            if (chrId != name) {
                chromoseReferencesMap.put(chrId, referenceList.findAll())
                chrId = name
                referenceList.clear()
            }
            referenceList.add(val)
        }

        chromoseReferencesMap.each { name, refMap ->
            println("chr = " + name + " map= " + refMap.toString())

        }
    }

    private static GenomeVariant parseGenomeChange(String changeStr) throws JannovarException {
        Pattern pat = Pattern.compile("(chr[0-9MXY]+):([0-9]+)([ACGTN]*)>([ACGTN]*)")
        Matcher match = pat.matcher(changeStr)

        if (!match.matches()) {
            System.err.println("[ERROR] Input string for the chromosomal change " + changeStr
                    + " does not fit the regular expression ... :(")
            System.exit(3)
        }

        int chr = refDict.getContigNameToID().get(match.group(1))
        int pos = Integer.parseInt(match.group(2))
        String ref = match.group(3)
        String alt = match.group(4)

        return new GenomeVariant(new GenomePosition(refDict, Strand.FWD, chr, pos, PositionType.ONE_BASED), ref, alt)
    }

    static List<Object> annotateMutation(String chromosomalChange) throws JannovarException {
        final VariantAnnotator annotator = new VariantAnnotator(refDict, chromosomeMap, new AnnotationBuilderOptions())
        String transcriptSequence = ""
        System.out.println("#change\teffect\thgvs_annotation\tmessages")
        // Parse the chromosomal change string into a GenomeChange object.
        final GenomeVariant genomeVariant = parseGenomeChange(chromosomalChange)

        // Construct VariantAnnotator for building the variant annotations.
        VariantAnnotations annoList = null
        try {
            annoList = annotator.buildAnnotations(genomeVariant)
        } catch (Exception e) {
            System.err.println(String.format("[ERROR] Could not annotate variant %s!", chromosomalChange))
            e.printStackTrace(System.err)
            return Collections.emptyList()
        }

        return [genomeVariant, annoList]
    }

    public static String getRefSeqAccessionId(Integer chr) {
        chromoseReferencesMap.get(chr).get(2)
    }

    public static String getGenBankAccessionId(Integer chr) {
        chromoseReferencesMap.get(chr).get(3)
    }


}
