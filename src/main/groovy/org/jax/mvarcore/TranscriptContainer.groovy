package org.jax.mvarcore

/**
 * Class used to collect new transcripts and then save them to the DB
 */
class TranscriptContainer {
    String primaryIdentifier
    int length
    String chromosome
    Long locationStart
    Long locationEnd
    String mgiGeneIdentifier
    String ensGeneIdentifier
    Variant variant
    Gene gene
    Boolean mostPathogenic
}
