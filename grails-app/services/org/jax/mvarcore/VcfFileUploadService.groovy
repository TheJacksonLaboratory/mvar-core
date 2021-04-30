package org.jax.mvarcore

import gngs.VCF
import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.sql.Sql
import org.apache.commons.lang.time.StopWatch
import org.grails.web.json.JSONObject
import org.hibernate.internal.SessionImpl
import org.jax.mvarcore.parser.AnnotationParser
import org.jax.mvarcore.parser.InfoParser

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Types


@Transactional
class VcfFileUploadService {

    def sessionFactory
    private def newTranscriptsMap
    private static final String ENSEMBL_URL = 'http://rest.ensembl.org/'

    private static int batchSize = 1000

    boolean isRefAssembly
    private String assembly

    private static final String VARIANT_CANON_INSERT = "insert into variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) VALUES (0,?,?,?,?,?)"
    private static final String VARIANT_INSERT = "insert into variant (chr, position, alt, ref, type, functional_class_code, assembly, parent_ref_ind, parent_variant_ref_txt, variant_ref_txt, String, protein_hgvs_notation, canon_var_identifier_id, gene_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    private static final String VARIANT_STRAIN_INSERT = "insert into variant_strain (variant_strains_id, strain_id) VALUES (?, ?)"
    private static final String VARIANT_TRANSCRIPT_INSERT = "insert into variant_transcript (variant_transcripts_id, transcript_id, most_pathogenic) VALUES (?, ?, ?)"

    void loadVCF(File vcfFile, int batchSize) {
        if (batchSize != -1) {
            this.batchSize = batchSize
            println("Batch size is " + batchSize)
        }
        loadVCF(vcfFile)
    }

    void loadVCF(File vcfFile) {

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        String vcfFileName = vcfFile.getName()
        String[] strTmpArray = vcfFileName.split('\\.')
        // the assembly should be the beginning of the filename followed by '.'
        assembly = strTmpArray[0]
        isRefAssembly = isRefAssembly(assembly) ?: false
        // the strain name should be after the assembly name after the '.' character
        String strainName = strTmpArray[1]

        if (!isAcceptedAssembly(assembly.toLowerCase())) {
            //Invalid file name. Expecting assembly as the first part of the file name
            return
        }

        println("vcf File: " + vcfFileName)
        try {
            // get strain id
            Long strainId = getStrainId(strainName);

            persistData(vcfFile, strainId)
        } catch (Exception e) {
            e.printStackTrace()
        }

        println("vcf File: " + vcfFileName)
        println("Vcf file complete parsing and persistance: ${stopWatch} time: ${new Date()}")
    }

    /**
     * We expect the strain name taken from the file name to be the same number of char as the strain
     * name in the database
     * @param strainName
     * @return
     */
    private Long getStrainId(String strainName) {
//        Statement selectStrainId = getConnection().createStatement();
//        ResultSet result = selectStrainId.executeQuery("SELECT ID WHERE name LIKE " + strainName);
//        result.next();
//        return result.getLong("ID");

        // We expect the strain name taken from the file name to be the same number of char as the strain
        // name in the database
        Strain strain = Strain.createCriteria().get {
            like 'name', strainName
        }
        return strain.id
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
     * @param strainId id of the strain in the Database for this file
     * @return
     */
    private persistData(File vcfFile, Long strainId) {
        //persist data by chromosome -- TODO: check potential for multi-threaded process
        //TODO: add mouse chr to config
        List<String> mouseChromosomes = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', 'X', 'Y', 'MT']
        List<String> varTypes = ['SNP', 'DEL', 'INS']
        // Map used to collect non-existing transcripts in DB
        // key : transcript id
        // value : list of 3 strings with 0 = gene id, 1 = variant id, 2 = most pathogenic
        newTranscriptsMap = [:]

        innoDBSetOptions(false)

        for (String type : varTypes) {
            println("Variant type = " + type)
            for (String chr : mouseChromosomes) {
                StopWatch stopWatch = new StopWatch()
                stopWatch.start()

                List<gngs.Variant> vcfVariants = parseVcf(chr, vcfFile, type)
                println("CHR = " + chr + ', variant size= ' + vcfVariants.size())

                //insert canonicals
                insertCanonVariantsBatch(vcfVariants)
                //insert variants, transcript, hgvs and relationships, and collect new transcripts not in DB
                insertVariantsBatch(vcfVariants, strainId)

                println("Chr= " + chr + " : persistance load = ${stopWatch} time: ${new Date()}")
                stopWatch.reset()
                stopWatch.start()
            }
        }

        // add new Transcripts to DB
        loadNewTranscripts(newTranscriptsMap)

        innoDBSetOptions(true)
    }

    /**
     * Enable/Disable ForeignKey checks, autocommit and unique checks
     * @param isEnabled
     * @return
     */
    private innoDBSetOptions(boolean isEnabled) {
        int val = isEnabled ? 1 : 0
        PreparedStatement foreignKeyCheckStmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS = ?")
        foreignKeyCheckStmt.setInt(1, val)
        PreparedStatement uniqueChecksStmt = connection.prepareStatement("SET UNIQUE_CHECKS = ?")
        uniqueChecksStmt.setInt(1, val)
        PreparedStatement autoCommitStmt = connection.prepareStatement("SET AUTOCOMMIT = ?")
        autoCommitStmt.setInt(1, val)
        autoCommitStmt.execute()
        uniqueChecksStmt.execute()
        foreignKeyCheckStmt.execute()
        connection.commit()
    }

    /**
     * Disable/Enable table key
     * @param tableName
     * @param isEnabled
     * @return
     */
    private setTableKeyOnOff(String tableName, boolean isEnabled) {
        String cmd = isEnabled ? "ALTER TABLE " + tableName + " ENABLE KEYS" : "ALTER TABLE "+ tableName + " DISABLE KEYS"
        PreparedStatement preparedStmt = connection.prepareStatement(cmd)
        preparedStmt.execute()
        connection.commit()
    }

    /**
     * Insert Canonicals in batch
     * @param vcf list
     * @return
     */
    private insertCanonVariantsBatch(List<gngs.Variant> varList) {
        String UPDATE_CANONICAL_ID = 'update variant_canon_identifier set caid = concat(\'MCA_\', lpad(id, 14, 0)) where caid is NULL'

        List<gngs.Variant> batchOfVars = []
        List<String> batchOfParentVariantRef = []

        StringBuilder strBuilder = new StringBuilder(8)
        String parentRefVariant, position, chromosome

        int idx = 0
        for (gngs.Variant var : varList) {
            batchOfVars.add(var)

            position = var.getInfo().OriginalStart ? var.getInfo().OriginalStart : var.getPos()
            chromosome = var.getChr().replace('ch', '').replace('r', '')
            strBuilder.setLength(0)
            parentRefVariant = strBuilder.append(chromosome).append('_').append(position).append('_').append(var.getRef()).append('_').append(var.getAlt()).toString()

            batchOfParentVariantRef.add(parentRefVariant)
            if (idx > 1 && idx % batchSize == 0) {
                batchInsertCannonVariantsJDBC(batchOfVars, batchOfParentVariantRef)
                //clear batch lists
                batchOfVars.clear()
                batchOfParentVariantRef.clear()
                cleanUpGorm()
            }
            idx++
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
    private batchInsertCannonVariantsJDBC(List<gngs.Variant> batchOfVars, List<String> batchOfParentVariantRef) {

        def foundRecs = VariantCanonIdentifier.findAllByVariantRefTxtInList(batchOfParentVariantRef)
        List<String> found = foundRecs.collect {
            it.variantRefTxt
        }

        // disable keys/indices
//        setTableKeyOnOffnOff("variant_canon_identifier", false)
//        setTableKeyOnOffnOff("variant", false)

        // insert canon variant
        PreparedStatement insertCanonVariants = connection.prepareStatement(VARIANT_CANON_INSERT, Statement.RETURN_GENERATED_KEYS)

        StringBuilder strBuilder = new StringBuilder(8)
        String position, chromosome, parentRefVariant

        int idx2 = 0
        for (gngs.Variant variant : batchOfVars) {
            position = variant.getInfo().OriginalStart ? variant.getInfo().OriginalStart : variant.getPos()
            chromosome = variant.getChr().replace('ch', '').replace('r', '')
            strBuilder.setLength(0)
            parentRefVariant = strBuilder.append(chromosome).append('_').append(position).append('_').append(variant.getRef()).append('_').append(variant.getAlt()).toString()

            if (found.find { it == parentRefVariant }) {
                println(idx2 + " Existing record ID = " + parentRefVariant)
            } else {
                // chromosome
                insertCanonVariants.setString(1, chromosome)
                insertCanonVariants.setInt(2, Integer.valueOf(position))
                insertCanonVariants.setString(3, variant.getRef())
                insertCanonVariants.setString(4, variant.getAlt())
                insertCanonVariants.setString(5, parentRefVariant)
                insertCanonVariants.addBatch()
            }
            idx2++
        }
        insertCanonVariants.executeBatch()
        connection.commit()

        // enable keys/indices
//        setStringtTableKeyOnOff("variant_canon_identifier", true)
//        setStringtTableKeyOnOffeyOnOff("variant", true)
    }

    /**
     * Insert Variants, variants relationship (transcripts, strain) in batch
     * @param vcf list
     * @param strainId id of the strain in the Database for this file
     */
    private void insertVariantsBatch(List<gngs.Variant> varList, Long strainId) {
        List<gngs.Variant> batchOfVars = []
        List<String> batchOfParentVariantRefTxt = []
        List<String> batchOfVariantRefTxt = []
        List<String> batchOfGenes = []
        List<String> batchOfTranscripts = []

        String geneName, transcriptName, parentRefVariant, variantRefTxt, annotStr, position, chromosome
        StringBuilder strBuilder = new StringBuilder(8)
        List<Map> annotationParsed
        InfoParser infoParser = new AnnotationParser()
        int idx = 0
        for (gngs.Variant var : varList) {
            batchOfVars.add(var)

            // Retrieve values
            // TODO Take into account the lift over from mm9 to mm10 when inserting original mm9 data
            position = var.getInfo().OriginalStart ? var.getInfo().OriginalStart : var.getPos()
            chromosome = var.getChr().replace('ch', '').replace('r', '')
            strBuilder.setLength(0)
            parentRefVariant = strBuilder.append(chromosome).append('_').append(position).append('_').append(var.getRef()).append('_').append(var.getAlt()).toString()
            batchOfParentVariantRefTxt.add(parentRefVariant)
//            strBuilder.setLength(0)
            variantRefTxt = parentRefVariant
//            variantRefTxt = strBuilder.append(chromosome).append('_').append(position).append('_').append(var.getRef()).append('_').append(var.getAlt()).toString()
            batchOfVariantRefTxt.add(variantRefTxt)

            // get jannovar info
            annotStr = strBuilder.append("ANN=").append(var.getInfo().get("ANN")).toString()
            annotationParsed = infoParser.parse(annotStr)
            geneName  = annotationParsed.get(0)["Gene_Name"]
            batchOfGenes.add(geneName as String)
            transcriptName = ((String)annotationParsed.get(0)["Feature_ID"]).split('\\.')[0]
            batchOfTranscripts.add(transcriptName)

            if (idx > 1 && idx % batchSize == 0) {
                batchInsertVariantsJDBC(batchOfVars, batchOfVariantRefTxt, batchOfParentVariantRefTxt, batchOfGenes, batchOfTranscripts, strainId)
                //clear batch lists
                batchOfVars.clear()
                batchOfParentVariantRefTxt.clear()
                batchOfVariantRefTxt.clear()
                batchOfGenes.clear()
                batchOfTranscripts.clear()
                cleanUpGorm()
            }
            idx++
        }
        //last batch
        if (batchOfVars.size() > 0) {
            batchInsertVariantsJDBC(batchOfVars, batchOfVariantRefTxt, batchOfParentVariantRefTxt, batchOfGenes, batchOfTranscripts, strainId)
            batchOfVars.clear()
            batchOfParentVariantRefTxt.clear()
            batchOfVariantRefTxt.clear()
            batchOfGenes.clear()
            batchOfTranscripts.clear()
            cleanUpGorm()
        }
    }

    private AnnotationParser infoParser = new AnnotationParser()

    /**
     * Insert variants, and relationships using JDBC
     * @param batchOfVars
     * @param batchOfVariantRefTxt
     * @param batchOfParentVariantRefTxt
     * @param batchOfGenes
     * @param batchOfTranscripts
     * @param strainId id of the strain in the Database for this file
     */
    private void batchInsertVariantsJDBC(List<gngs.Variant> batchOfVars, List<String> batchOfVariantRefTxt, List<String> batchOfParentVariantRefTxt, List<String> batchOfGenes, List<String> batchOfTranscripts, Long strainId) {

        def foundRecs = Variant.findAllByVariantRefTxtInList(batchOfVariantRefTxt)
        List<String> found = foundRecs.collect {
            it.variantRefTxt
        }
        // records of all unique canon ids
        def cannonRecs = VariantCanonIdentifier.findAllByVariantRefTxtInList(batchOfParentVariantRefTxt)
        // records of all unique gene symbols
        def geneSymbolRecs = Gene.findAllBySymbolInList(batchOfGenes)
        def geneSynonymRecs = Synonym.findAllByNameInList(batchOfGenes)

        // disable keys/indices
//        setTableKeyOnOff("variant_canon_identifier", false)
//        setTableKeyOnOff("variant", false)
//        setTableKeyOnOffOnOff("gene", false)

        // directly use java PreparedStatement to get ResultSet with keys
        PreparedStatement insertVariants = connection.prepareStatement(VARIANT_INSERT, Statement.RETURN_GENERATED_KEYS)

        StringBuilder strBuilder = new StringBuilder(8)
        List<Map> annotationParsed
        Gene gene
        String position, chromosome, parentRefVariant, variantRefTxt, annotStr, geneName, geneId
        boolean isParentVariant
        VariantCanonIdentifier canonIdentifier

        int idx2 = 0
        for (gngs.Variant variant : batchOfVars) {
            // retrieve values
            // TODO Take into account the lift over from mm9 to mm10 when inserting original mm9 data
//            if (variant.getInfo().OriginalStart) {
//                position = variant.getInfo().OriginalStart
//                isParentVariant = false
//            } else {
            position = variant.getPos()
            isParentVariant = true
//            }
            chromosome = variant.getChr().replace('ch', '').replace('r', '')
            strBuilder.setLength(0)
            parentRefVariant = strBuilder.append(chromosome).append('_').append(position).append('_').append(variant.getRef()).append('_').append(variant.getAlt()).toString()
//            strBuilder.setLength(0)
            variantRefTxt = parentRefVariant
//            variantRefTxt = strBuilder.append(chromosome).append('_').append(position).append('_').append(variant.getRef()).append('_').append(variant.getAlt()).toString()

            if (found.find { it == variantRefTxt }) {
                println(idx2 + " Existing record ID = " + variantRefTxt)
            } else {
                canonIdentifier = cannonRecs.find {
                    it.variantRefTxt == parentRefVariant
                }
                // get jannovar info
                annotStr = strBuilder.append("ANN=").append(variant.getInfo().get("ANN")).toString()
                annotationParsed = infoParser.parse(annotStr)
                // Do we want that? to link only the most pathogenic gene info to this variant? or do we have a one to many relationship?
                geneName = annotationParsed.get(0)["Gene_Name"]
                gene = geneSymbolRecs.find { it.symbol == geneName }
                // we get the first gene info in the jannovar info string
                if (gene == null) {
                    // We check in the list of synonyms to get the corresponding gene
                    gene = getGeneBySynonyms(geneSynonymRecs, (String) geneName)
                }
                geneId = gene != null ? gene.id : null

                insertVariants.setString(1, chromosome)
                insertVariants.setInt(2, Integer.valueOf(position))
                insertVariants.setString(3, variant.getAlt())
                insertVariants.setString(4, variant.getRef())
                insertVariants.setString(5, variant.getType())
                insertVariants.setString(6, concatenate(annotationParsed, "Annotation"))
                insertVariants.setString(7, assembly)
                insertVariants.setBoolean(8, isParentVariant)
                insertVariants.setString(9, parentRefVariant)
                insertVariants.setString(10, variantRefTxt)
                insertVariants.setString(11, concatenate(annotationParsed, "HGVS.c"))
                insertVariants.setString(12, concatenate(annotationParsed, "HGVS.p"))
                insertVariants.setLong(13, canonIdentifier.id)
                if (geneId == null)
                    insertVariants.setNull(14, Types.BIGINT)
                else
                    insertVariants.setLong(14, Long.valueOf(geneId))
                insertVariants.addBatch()
            }
            idx2++
        }
        insertVariants.executeBatch()
        connection.commit()

        ResultSet variantKeys = insertVariants.getGeneratedKeys()

        def transcriptsRecs = Transcript.findAllByPrimaryIdentifierInList(batchOfTranscripts)

        // insert Transcript/ Also the transcript/variant relationship table needs to be manually updated to contain the following column boolean : most_pathogenic
        PreparedStatement insertVariantsByStrain = connection.prepareStatement(VARIANT_STRAIN_INSERT)
        PreparedStatement insertVariantsByTranscript = connection.prepareStatement(VARIANT_TRANSCRIPT_INSERT)

        Long variantKey
        String transcriptId
        Transcript transcript

        idx2 = 0
        for (gngs.Variant variant : batchOfVars) {

            // retrieve values
            // TODO Take into account the lift over from mm9 to mm10 when inserting original mm9 data
//            position = variant.getInfo().OriginalStart ? variant.getInfo().OriginalStart : variant.getPos()
            position = variant.getPos()
            chromosome = variant.getChr().replace('ch', '').replace('r', '')
            strBuilder.setLength(0)
            variantRefTxt = strBuilder.append(chromosome).append('_').append(position).append('_').append(variant.getRef()).append('_').append(variant.getAlt()).toString()

            if (found.find { it == variantRefTxt }) {
                println(idx2 + " Existing record ID = " + variantRefTxt)
            } else {
                // add transcripts
                // get the key of the current variant previously inserted
                variantKeys.next()
                variantKey = variantKeys.getLong(1)

                // get jannovar info
                annotStr = strBuilder.append("ANN=").append(variant.getInfo().get("ANN")).toString()
                annotationParsed = infoParser.parse(annotStr)
                for (int i = 0; i < annotationParsed.size(); i++) {
                    transcriptId = annotationParsed.get(i)["Feature_ID"].split('\\.')[0]
                    // check if transcript already exists, if not we create it
                    if (i == 0) {
                        transcript = transcriptsRecs.find { it.primaryIdentifier == transcriptId }
                    } else {
                        transcript = Transcript.createCriteria().get {
                            eq('primaryIdentifier', transcriptId)
                        } as Transcript
                    }

                    if (transcript == null) {
                        geneName = annotationParsed.get(i)["Gene_Name"]
                        gene = geneSymbolRecs.find { it.symbol == geneName }
                        // we get the first gene info in the jannovar info string
                        if (gene == null) {
                            // We check in the list of synonyms to get the corresponding gene
                            gene = getGeneBySynonyms(geneSynonymRecs, geneName)
                        }
                        if (!newTranscriptsMap.containsKey(transcriptId))
                            newTranscriptsMap.put(transcriptId, [gene != null ? gene.mgiId : '', String.valueOf(variantKey), String.valueOf(i==0)] )
                    } else {
                        // we add the transcript / variant relationship
                        insertVariantsByTranscript.setLong(1, variantKey)
                        insertVariantsByTranscript.setLong(2, transcript.id)
                        insertVariantsByTranscript.setBoolean(3, i == 0)
                        insertVariantsByTranscript.addBatch()
                    }

                }
                // add variants by strain
                insertVariantsByStrain.setLong(1, variantKey)
                insertVariantsByStrain.setLong(2, strainId)
                insertVariantsByStrain.addBatch()

            }
            idx2++
        }
        insertVariantsByStrain.executeBatch()
        insertVariantsByTranscript.executeBatch()
        connection.commit()

        // enable keys/indices
        setTableKeyOnOff("variant_canon_identifier", true)
        setTableKeyOnOff("variant", true)
        setTableKeyOnOff("gene", true)

    }

    private String concatenate(List<Map> annotations, String annotationKey) {
        String concatenationResult = ''
        StringBuilder strBuilder = new StringBuilder(8)
        for (Map annot : annotations) {
            for (Map.Entry<String, Integer> entry : annot.entrySet()) {
                if (entry.getKey() == annotationKey) {
                    if (concatenationResult != '') {
                        strBuilder.setLength(0)
                        concatenationResult = strBuilder.append(concatenationResult).append(',').append(entry.getValue()).toString()
                    } else {
                        concatenationResult = entry.getValue()
                    }
                    return concatenationResult
                }
            }
        }
        return concatenationResult
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

    /**
     * Load new transcripts from the Ensembl Rest API given an ID and a List of Gene and Variant ID
     * The Gene ID and Variant ID associated with the transcript
     * @param ids
     */
    void loadNewTranscripts(Map<String, List<String>> ids) {
        println("*** ADD NEW TRANSCRIPTS **")
        log.info("*** ADD NEW TRANSCRIPTS **")
        String lookupQuery = 'lookup/id/'
        String url = "${ENSEMBL_URL}"
        RestBuilder rest = new RestBuilder()
        def transcriptList = []

        for (Map.Entry<String,List<String>> entry : ids.entrySet()) {
            TranscriptContainer transcript = loadNewTranscript(rest, url + lookupQuery, entry.getKey(), entry.getValue())
            if (transcript)
                transcriptList.add(transcript)
        }
        // as size might be small we set the batch size to the list size (so we have only one batch
        saveNewTranscripts(transcriptList, transcriptList.size())
    }

    /**
     * Load new transcript in DB given the transcript id. Using a RestBuilder, we connect to the
     * Ensembl RESTAPI to retrieve the info if it exists.
     * @param rest
     * @param url
     * @param id
     * @param geneAndVariantIds
     * @return
     */
    private TranscriptContainer loadNewTranscript(RestBuilder rest, String url, String id, List<String> idsAndMostPathogenic) {
        String fullQuery = url + id + '?content-type=application/json;expand=1'
        RestResponse resp = rest.get(fullQuery)
        log.info("Request response = " + resp.statusCode.value())
        JSONObject jsonResult
        String respString = resp.getBody()

        if (resp.statusCode.value() == 200 && respString) {
            int begin = respString.indexOf("{")
            int end = respString.lastIndexOf("}") + 1
            respString = respString.substring(begin, end)
            jsonResult = new JSONObject(respString)
        } else {
            log.error("Response to mouse mine data request: " + resp.statusCode.value() + " restResponse.text= " + resp.text)
            return null
        }
        int start = jsonResult.get('start') as int
        int end = jsonResult.get('end') as int
        // TODO find how to get base pair length
//        int length = (end - start) + 1
        // add variant/transcript relationship
        Variant variant = Variant.findById(Long.parseLong(idsAndMostPathogenic.get(1)))
        // add gene/transcript relationship
        Gene gene
        if (idsAndMostPathogenic.get(0) != '') {
            gene = Gene.createCriteria().get {
                eq('mgiId', idsAndMostPathogenic.get(0))
            }
        } else {
            gene = Gene.createCriteria().get {
                eq('ensemblGeneId', jsonResult.get('Parent'))
            }
        }
        TranscriptContainer transcript = new TranscriptContainer(
                primaryIdentifier: id,
//                length: length,
                chromosome: jsonResult.get('seq_region_name'),
                locationStart: start,
                locationEnd: end,
                ensGeneIdentifier: jsonResult.get('Parent'),
                variant: variant,
                gene: gene,
                mostPathogenic: Boolean.valueOf(idsAndMostPathogenic.get(2))
        )
        return transcript
    }

    private void saveNewTranscripts(List<TranscriptContainer> listOfTranscripts, int batchSize) {
        List<TranscriptContainer> batchOfTranscripts = []

        listOfTranscripts.eachWithIndex{ transcript, idx ->
            batchOfTranscripts.add(transcript)
            if (idx > 1 && idx % batchSize == 0) {
                batchInsertNewTranscriptsJDBC(batchOfTranscripts)
                //clear batch lists
                batchOfTranscripts.clear()
                cleanUpGorm()
            }
        }
        //last batch
        if (listOfTranscripts.size() > 0) {
            batchInsertNewTranscriptsJDBC(batchOfTranscripts)
            batchOfTranscripts.clear()
            cleanUpGorm()
        }
    }

    private final static String TRANSCRIPT_INSERT = "insert into transcript (primary_identifier, length, chromosome, location_start, location_end, mgi_gene_identifier, ens_gene_identifier) VALUES (?,?,?,?,?,?,?)"

    private batchInsertNewTranscriptsJDBC(List<TranscriptContainer> batchOfTranscripts) {
        PreparedStatement insertTranscripts = connection.prepareStatement(TRANSCRIPT_INSERT, Statement.RETURN_GENERATED_KEYS)

        for (TranscriptContainer transcript : batchOfTranscripts) {
            insertTranscripts.setString(1, transcript.primaryIdentifier)
            insertTranscripts.setInt(2, transcript.length)
            insertTranscripts.setString(3, transcript.chromosome)
            insertTranscripts.setLong(4, transcript.locationStart)
            insertTranscripts.setLong(5, transcript.locationEnd)
            if (transcript.mgiGeneIdentifier)
                insertTranscripts.setString(6, transcript.mgiGeneIdentifier)
            else
                insertTranscripts.setNull(6, Types.VARCHAR)
            insertTranscripts.setString(7, transcript.ensGeneIdentifier)
            insertTranscripts.addBatch()
        }
        insertTranscripts.executeBatch()
        ResultSet transcriptsKeys = insertTranscripts.getGeneratedKeys()

        PreparedStatement insertVariantsByTranscript = connection.prepareStatement(VARIANT_TRANSCRIPT_INSERT)

        Long transcriptKey
        for (TranscriptContainer transcript : batchOfTranscripts) {
            // add transcripts/variant relationship
            transcriptsKeys.next()
            transcriptKey = transcriptsKeys.getLong(1)

            // we add the transcript / variant relationship
            insertVariantsByTranscript.setLong(1, transcript.variant.id)
            insertVariantsByTranscript.setLong(2, transcriptKey)
            insertVariantsByTranscript.setBoolean(3, transcript.mostPathogenic)
            insertVariantsByTranscript.addBatch()

        }
        insertVariantsByTranscript.executeBatch()

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

    private List<gngs.Variant> parseVcf(String chromosome, File vcfFile, String type) {
        List<gngs.Variant> varList
        try {
            //vcfFileInputStream.line
            VCF vcf = VCF.parse(vcfFile.getPath()) { v ->
                (v.chr == 'chr' + chromosome || v.chr == chromosome) && v.type == type
            }
            varList = vcf.getVariants()
            println("parsed variants = " + vcf.getVariants().size())
        } catch (Exception e) {
            String error = "Error reading the VCF file " + e.getMessage()
            println(error)
            throw e
        }
        return varList
    }

}
