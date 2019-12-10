package org.jax.mvarcore

import gngs.VCF
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.apache.commons.lang.time.StopWatch
import org.hibernate.internal.SessionImpl

import org.springframework.dao.InvalidDataAccessApiUsageException

import org.jax.mvarcore.jannovar.JannovarUtility

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types


@Transactional
class VcfFileUploadService {

    def sessionFactory
    LoadService loadService
    Map<String, List<String>> newTranscriptsMap

    void loadVCF(File vcfFile) {

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()


        String vcfFileName = vcfFile.getName()
        String assembly = vcfFileName.substring(0, vcfFileName.indexOf("_"))

        if (!isAcceptedAssembly(assembly.toLowerCase())) {
            //Invalid file name. Expecting assembly as the first part of the file name
            return
        }

        println("vcf File: " + vcfFileName)
        try {
            persistData(vcfFile)
        } catch (Exception e) {
            e.printStackTrace()
        }

        println("vcf File: " + vcfFileName)
        println("Vcf file complete parsing and persistance: ${stopWatch} time: ${new Date()}")
    }

    /**
     *
     * 1. parse the vcf -- by chromosome ,
     * 2. Persist canonicals
     * 3. persist variants
     * 4. persist variant associations
     *   - canonical
     *   - strain
     *   - gene
     *   - jannovar data
     *   - external ids
     * 5. construct search doc -- TODO: possible search docs for speed querying of data in site
     * @param vcfFile
     * @return
     */
    private persistData(File vcfFile) {

        String vcfFileName = vcfFile.getName()
        String assembly = vcfFileName.substring(0, vcfFileName.indexOf("_"))

        //persist data by chromosome -- TODO: check potential for multi-threaded process
        //TODO: add mouse chr to config
        List<String> mouseChromosomes = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', 'X', 'Y', 'MT']

        // Map used to collect non-existing transcripts in DB
        newTranscriptsMap = new HashMap<String, List<String>>()
        mouseChromosomes.each { chr ->

            StopWatch stopWatch = new StopWatch()
            stopWatch.start()

            List<Map> vcfVariants = parseVcf(chr, vcfFile, assembly)
            println("CHR = " + chr + ', variant size= ' + vcfVariants.size())

            //insert canonicals
            insertCanonVariantsBatch(vcfVariants)
            //insert variants, transcript, hgvs and relationships, and collect new transcripts not in DB
            insertVariantsBatch(vcfVariants)

            println("Chr= " + chr + " : persistance load = ${stopWatch} time: ${new Date()}")
            stopWatch.reset()
            stopWatch.start()

        }
        // add new Transcripts to DB
        loadService.loadNewTranscripts(newTranscriptsMap)

    }

    // TODO: leaving this commented out for now. some concepts here are still useful
//    private void insertVariants(Map varIn){
//
//
//        String SELECT_LAST_INSERT_ID = 'SELECT LAST_INSERT_ID() as ID'
//        String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) VALUES (0, ?)'
//        String INSERT_INTO_DB_VARIANT = 'insert into variant (version, chr, position, alt, ref, type, assembly, parent_ref_ind, parent_variant_ref_txt, canon_var_identifier_id) ' +
//                'VALUES (0, ?,?,?,?,?,?,?,?,?)' //  + SELECT_CANONICAL_ID + ')'
//        String SELECT_EXTERNAL_SOURCE = '(select id from source where source_name = ?)'
//        String INSERT_INTO_DB_IDENTIFIER = 'insert into identifier (version, external_source_id, external_id) VALUES (0, ' + SELECT_EXTERNAL_SOURCE + ', ?)'
//        String INSERT_INTO_DB_VARIANT_IDENTIFIER = 'insert into variant_identifier (variant_identifier_id, identifier_id) VALUES (?, ?)'
//
//        final Sql sql = getSql()
//        //sql.withBatch(500, INSERT_INTO_DB_TERM_DB_GENES) { BatchingPreparedStatementWrapper ps ->
//
//        //canonical id
//        sql.execute(INSERT_INTO_DB_VARIANT_CANONICAL_ID, varIn.parentVariantRef)
//        String lastCanonVariantID = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID
//
//        //variant
//        sql.execute(INSERT_INTO_DB_VARIANT, [varIn.chr, varIn.pos, varIn.alt,
//                                             varIn.ref, varIn.type,  varIn.assembly, varIn.isParentVariant,
//                                             varIn.parentVariantRef, lastCanonVariantID]) //, varIn.parentVariantRef])
//        String lastVariantID = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID
//
//        if (varIn.ID && varIn.ID.size() > 1) {
//
//            //identifier
//            sql.execute(INSERT_INTO_DB_IDENTIFIER, ['dbSNP', varIn.ID])
//            String lastIdentifierId = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID
//
//            //variant_identifier relation
//            sql.execute(INSERT_INTO_DB_VARIANT_IDENTIFIER, [lastVariantID, lastIdentifierId])
//        }
//        //sql.commit()
//        //sql.close()
//
//    }

    /**
     * Insert Canonicals in batch
     * @param varList
     * @return
     */
    private insertCanonVariantsBatch(List<Map> varList) {
        String UPDATE_CANONICAL_ID = 'update variant_canon_identifier set caid = concat(\'MCA_\', lpad(id, 14, 0)) where caid is NULL'

        List<Map> batchOfVars = []
        List<String> batchOfParentVariantRef = []
        int batchSize = 500

        varList.eachWithIndex { var, idx ->
            batchOfVars.add(var)
            batchOfParentVariantRef.add((String)var.parentVariantRef)
            if (idx > 1 && idx % batchSize == 0) {
                batchInsertCannonVariantsJDBC(batchOfVars, batchOfParentVariantRef)
                //clear batch lists
                batchOfVars.clear()
                batchOfParentVariantRef.clear()
                cleanUpGorm()
            }
        }

        //last batch
        if (batchOfVars.size() > 0) {
            batchInsertCannonVariantsJDBC(batchOfVars, batchOfParentVariantRef)
            batchOfVars.clear()
            batchOfParentVariantRef.clear()
            cleanUpGorm()
        }

        // update canonical id
        final Sql sql = getSql()
        sql.execute(UPDATE_CANONICAL_ID)
        //sql.commit()
    }

    /**
     * Insert Canonicals using JDBC
     * @param batchOfVars
     * @param batchOfParentVariantRef
     * @return
     */
    private batchInsertCannonVariantsJDBC(List<Map> batchOfVars, List<String> batchOfParentVariantRef) {
        // insert canon variant
        PreparedStatement insertCanonVariants = connection.prepareStatement("insert into variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) VALUES (0,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)

        def foundRecs = VariantCanonIdentifier.findAllByVariantRefTxtInList(batchOfParentVariantRef)
        List<String> found = foundRecs.collect {
            it.variantRefTxt
        }

        batchOfVars.eachWithIndex { variant, idx2 ->
            if (found.find { it == variant.parentVariantRef }) {
                println(idx2 + " Existing record ID = " + variant.parentVariantRef)
            } else {
                insertCanonVariants.setString(1, (String) variant.chr)
                insertCanonVariants.setInt(2, (Integer) variant.pos)
                insertCanonVariants.setString(3, (String) variant.ref)
                insertCanonVariants.setString(4, (String) variant.alt)
                insertCanonVariants.setString(5, (String) variant.parentVariantRef)
                insertCanonVariants.addBatch()
            }
        }
        insertCanonVariants.executeBatch()

    }

    /**
     * Insert Variants, variants relationship (transcripts, strain) in batch
     * @param varList
     */
    private void insertVariantsBatch(List<Map> varList) {
        List<Map> batchOfVars = []
        List<String> batchOfParentVariantRefTxt = []
        List<String> batchOfVariantRefTxt = []
        List<String> batchOfGenes = []
        List<String> batchOfTranscripts = []
        def batchSize = 1000
        List<Strain> strainList = varList.size() > 0 ? Strain.createCriteria().list {
            like 'name', (varList.get(0).strain) + '%'
        } : new ArrayList<Strain>()

        varList.eachWithIndex { var, idx ->
            batchOfVars.add(var)
            batchOfParentVariantRefTxt.add((String) var.parentVariantRef)
            batchOfVariantRefTxt.add((String) var.variantRefTxt)

            batchOfGenes.add((String) var.info_gene[0])
            batchOfTranscripts.add(((String) var.info_transcript_name[0]).split('\\.')[0])

            if (idx > 1 && idx % batchSize == 0) {
                batchInsertVariantsJDBC(batchOfVars, batchOfVariantRefTxt, batchOfParentVariantRefTxt, batchOfGenes, batchOfTranscripts, strainList)
                //clear batch lists
                batchOfVars.clear()
                batchOfParentVariantRefTxt.clear()
                batchOfVariantRefTxt.clear()
                batchOfGenes.clear()
                batchOfTranscripts.clear()
                cleanUpGorm()
            }
        }
        //last batch
        if (batchOfVars.size() > 0) {
            batchInsertVariantsJDBC(batchOfVars, batchOfVariantRefTxt, batchOfParentVariantRefTxt, batchOfGenes, batchOfTranscripts, strainList)
            batchOfVars.clear()
            batchOfParentVariantRefTxt.clear()
            batchOfVariantRefTxt.clear()
            batchOfGenes.clear()
            batchOfTranscripts.clear()
            cleanUpGorm()
        }
    }

    /**
     * Insert variants, and relationships using JDBC
     * @param batchOfVars
     * @param batchOfVariantRefTxt
     * @param batchOfParentVariantRefTxt
     * @param batchOfGenes
     * @param batchOfTranscripts
     * @param strainList
     */
    private void batchInsertVariantsJDBC(List<Map> batchOfVars, List<String> batchOfVariantRefTxt, List<String> batchOfParentVariantRefTxt, List<String> batchOfGenes, List<String> batchOfTranscripts, List<Strain> strainList) {

        def foundRecs = Variant.findAllByVariantRefTxtInList(batchOfVariantRefTxt)
        List<String> found = foundRecs.collect {
            it.variantRefTxt
        }

        // records of all unique canon ids
        def cannonRecs = VariantCanonIdentifier.findAllByVariantRefTxtInList(batchOfParentVariantRefTxt)
        // records of all unique gene symbols
        def geneSymbolRecs = Gene.findAllBySymbolInList(batchOfGenes)
        def geneSynonymRecs = Synonym.findAllByNameInList(batchOfGenes)

        // directly use java PreparedStatement to get ResultSet with keys
        PreparedStatement insertVariants = connection.prepareStatement("insert into variant (chr, position, alt, ref, type, assembly, parent_ref_ind, parent_variant_ref_txt, variant_ref_txt, dna_hgvs_notation, protein_hgvs_notation, canon_var_identifier_id, gene_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
        batchOfVars.eachWithIndex { variant, idx2 ->
            if (found.find { it == variant.variantRefTxt }) {
                println(idx2 + " Existing record ID = " + variant.variantRefTxt)
            } else {
                VariantCanonIdentifier canonIdentifier = cannonRecs.find {
                    it.variantRefTxt == variant.parentVariantRef
                }
                // Do we want that? to link only the most pathogenic gene info to this variant? or do we have a one to many relationship?
                Gene gene = geneSymbolRecs.find { it.symbol == variant.info_gene[0] }
                // we get the first gene info in the jannovar info string
                if (gene == null) {
                    // We check in the list of synonyms to get the corresponding gene
                    gene = getGeneBySynonyms(geneSynonymRecs, (String) variant.info_gene[0])
                }
                def geneId = gene != null ? gene.id : null

                insertVariants.setString(1, (String) variant.chr)
                insertVariants.setInt(2, (Integer) variant.pos)
                insertVariants.setString(3, (String) variant.alt)
                insertVariants.setString(4, (String) variant.ref)
                insertVariants.setString(5, (String) variant.type)
                insertVariants.setString(6, (String) variant.assembly)
                insertVariants.setBoolean(7, (Boolean) variant.isParentVariant)
                insertVariants.setString(8, (String) variant.parentVariantRef)
                insertVariants.setString(9, (String) variant.variantRefTxt)
                insertVariants.setString(10, (String) variant.info_hgvs_dna[0])
                insertVariants.setString(11, (String) variant.info_hgvs_protein[0])
                insertVariants.setLong(12, canonIdentifier.id)
                if (geneId == null)
                    insertVariants.setNull(13, Types.BIGINT)
                else
                    insertVariants.setLong(13, geneId)
                insertVariants.addBatch()

            }
        }
        insertVariants.executeBatch()
        ResultSet variantKeys = insertVariants.getGeneratedKeys()

        def transcriptsRecs = Transcript.findAllByPrimaryIdentifierInList(batchOfTranscripts)

        // insert Transcript/ Also the transcript/variant relationship table needs to be manually updated to contain the following column boolean : most_pathogenic
        PreparedStatement insertTranscripts = connection.prepareStatement("insert into transcript (primary_identifier, length, chromosome, location_start, location_end, gene_identifier) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
        PreparedStatement insertVariantsByStrain = connection.prepareStatement("insert into variant_strain (variant_strains_id, strain_id) VALUES (?, ?)")
        PreparedStatement insertVariantsByTranscript = connection.prepareStatement("insert into variant_transcript (variant_transcripts_id, transcript_id, most_pathogenic) VALUES (?, ?, ?)")

        batchOfVars.eachWithIndex { variant, idx2 ->

            if (found.find { it == variant.variantRefTxt }) {
                println(idx2 + " Existing record ID = " + variant.variantRefTxt)
            } else {
                // add transcripts
                variantKeys.next()
                Long variantKey = variantKeys.getLong(1)
                for (int i = 0; i < variant.info_transcript_name.size(); i++) {
                    String transcriptId = ((String) variant.info_transcript_name[i]).split('\\.')[0]
                    // check if transcript already exists, if not we create it
                    Transcript transcript
                    if (i == 0) {
                        transcript = transcriptsRecs.find { it.primaryIdentifier == transcriptId }
                    } else {
                        transcript = Transcript.createCriteria().get {
                            eq('primaryIdentifier', transcriptId)
                        } as Transcript
                    }

                    if (transcript == null) {
                        Gene gene = geneSymbolRecs.find { it.symbol == variant.info_gene[i] }
                        // we get the first gene info in the jannovar info string
                        if (gene == null) {
                            // We check in the list of synonyms to get the corresponding gene
                            gene = getGeneBySynonyms(geneSynonymRecs, (String) variant.info_gene[i])
                        }
                        if (!newTranscriptsMap.containsKey(transcriptId))
                            newTranscriptsMap.put(transcriptId, [gene != null ? gene.mgiId : '', String.valueOf(variantKey)] )
                    } else {
                        // we add the transcript / variant relationship
                        insertVariantsByTranscript.setLong(1, variantKey)
                        insertVariantsByTranscript.setLong(2, transcript.id)
                        insertVariantsByTranscript.setBoolean(3, i == 0)
                        insertVariantsByTranscript.addBatch()
                    }

                }
                // add variants by strain
                for (strain in strainList) {
                    insertVariantsByStrain.setLong(1, variantKey)
                    insertVariantsByStrain.setLong(2, strain.id)
                    insertVariantsByStrain.addBatch()
                }
            }
        }
        insertTranscripts.executeBatch()
        insertVariantsByStrain.executeBatch()

    }

    private getGeneBySynonyms(List<Synonym> geneSynonymRecs, String geneName) {
        Gene gene = null
        Synonym syn = geneSynonymRecs.find { it.name == geneName }
        if (syn != null) {
            gene = Gene.createCriteria().get {
                synonyms {
                    eq('id', syn.id)
                }
            } as Gene
        }
        return gene
    }

    protected Sql getSql() {
        new Sql(getConnection())
    }

    /**
     * @return a Connection with the underlying connection for the active session
     */
    protected Connection getConnection() {
        SessionImpl sessionImpl = sessionFactory.currentSession as SessionImpl
        sessionImpl.connection()
    }


    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }


    private boolean isAcceptedAssembly(String inAssembly) {

        //TODO define configuration for accepted assemblies
        List<String> assemblies = ['grcm38', 'ncbi37', 'ncbi36']
        if (assemblies.contains(inAssembly))
            return true
        else
            return false
    }

    private boolean isRefAssembly(String inAssembly) {

        //TODO define configuration for reference assembly
        String refAssembly = 'grcm38'
        if (inAssembly == refAssembly)
            return true
        else
            return false
    }

    private List<Map> parseVcf(String chromosome, File vcfFile, String assembly) {

        // load jannovar reference info
        JannovarUtility.loadReference()

        //Parsing of liftover files should be ONLY for liftover to GRCm38
        //GRCm38 vcf files should have start with GRCm38

        boolean refAssembly = isRefAssembly(assembly) ?: false

        List<Map> variantList = []
        try {

            //vcfFileInputStream.line
            def vcf = VCF.parse(vcfFile.getPath()) { v ->

                v.chr == 'chr' + chromosome || v.chr == chromosome
            }

            println("parsed variants = " + vcf.getVariants().size())
            // get strain name from last header column
            def strain = ''
            if (vcf.getVariants()[0] != null) {
                def header = vcf.getVariants()[0].getHeader().lastHeaderLine
                if (header.size() > 9) {
                    strain = header[9]
                }
            }

            vcf.getVariants().each { vcfVariant ->

                // Get Info column (jannovar data)
                String annotStr = vcfVariant.getInfo().get("ANN")
                InfoParser infoParser = new AnnotationParser("ANN=" + annotStr)

                //compile the vcfVariant and vcfVariant info to build a JSON object
                //def jsp = new JsonSlurper()
                Map<String, Object> variantOut = [:]

                String chromosomeRead = vcfVariant.getChr().replace('ch', '').replace('r', '')

                Map<String, Object> variant = [:]
                variant.put("ID", vcfVariant.getId())
                variant.put("pos", vcfVariant.getPos())
                variant.put("chr", chromosomeRead)
                variant.put("ref", vcfVariant.getRef())
                variant.put("alt", vcfVariant.getAlt())
                variant.put("type", vcfVariant.getType())
                variant.assembly = assembly
                variant.put("info_gene", infoParser.geneName)
                variant.put("info_transcript_name", infoParser.featureId)
                variant.put("info_transcript_biotype", infoParser.transcriptBiotype)
                variant.put("info_hgvs_dna", infoParser.hgvsC)
                variant.put("info_hgvs_protein", infoParser.hgvsP)
                variant.put("strain", strain)

                if (chromosomeRead == 'X') {
                    chromosomeRead = '20'
                }
                if (chromosomeRead == 'Y') {
                    chromosomeRead = '21'
                }
                String refSeqAccession = JannovarUtility.getRefSeqAccessionId(Integer.parseInt(chromosomeRead))

                variant.put("transcript_ref_access", refSeqAccession)

                ///holds full variation change for the parent reference <chr_pos_ref_alt>  -- ref and alt is empty will have '.' as value
                String refIn = variant.ref ?: '.'
                String altIn = variant.alt ?: '.'
                String parentRefVariant = variant.chr + '_' + variant.pos + '_' + variant.ref + '_' + variant.alt
                variant.put("parentVariantRef", parentRefVariant)


                String orgPos = vcfVariant.getInfo().OriginalStart
                if (orgPos) {

                    variant.pos = vcfVariant.getInfo().OriginalStart
                    variant.isParentVariant = false
                } else if (refAssembly) {

                    variant.isParentVariant = true
                } else {
                    //something is wrong. Either
                    //-liftover variants should have original position  OR
                    //-refAssembly should be GRCm38
                    throw new InvalidDataAccessApiUsageException("Expects GRCm38 file or liftover to GRCm38")
                }

                if (!vcfVariant.getInfo().OriginalAlleles) {
                    // if original alleles properties are missed in liftover then they are the same
                    //TODO check and assign values when original alleles are different in liftover variant
                    variant.put("ref", vcfVariant.getRef())
                    variant.put("alt", vcfVariant.getAlt())
                }

                String varRefTxt = variant.chr + '_' + variant.pos + '_' + variant.ref + '_' + variant.alt
                variant.variantRefTxt = varRefTxt

                //variantOut.put('variant', variant)

                variantList.add(variant)

            }
        } catch (Exception e) {

            String error = "Error reading the VCF file " + e.getMessage()
            println(error)
            throw e
        }


        return variantList
    }
}
