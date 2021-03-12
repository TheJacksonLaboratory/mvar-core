package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse

import org.grails.datastore.gorm.GormEntity
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.internal.SessionImpl

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement


@Transactional
class LoadService {

    def sessionFactory //inject session factory

    // TODO move to config file
    private final String MM_URL = 'http://www.mousemine.org/mousemine/service/query/results'

    // Unused fields
    private final static USE_FILE = false
//    File geneFeedFile = new ClassPathResource('results_gene.json').file
//    File strainFeedFile = new ClassPathResource('results_strain.json').file
//    File transcriptFeedFile = new ClassPathResource('results_transcripts.json').file

    def serviceMethod() {

    }

    /**
     * Load Gene, Strains and Transcript/gene relationships
     */
    void loadData() {
        // We need to have alleles inserted first before Strains and genes
        if (Gene.count() <= 0 && Allele.count() > 0) {
            loadMouseGenes()
        }
        if (Strain.count() <= 0 && Allele.count() > 0) {
            loadMouseStrains()
        }
        // we need to have transcripts inserted before as well as genes
        if (Transcript.count() > 0 && Gene.count() > 0 && !tableHasValues('gene_transcript')) {
            saveGeneTranscriptsRelationships()
        }
        updateVariantTranscriptTable()
        updateVariantStrainTable()
    }

    /**
     * public interface to start the strain data load
     * The Allele Table needs to be populated first. (We retrieved them using a Mysql csv upload as
     * it would take too long to upload them using the Query API
     */
    void loadMouseStrains() {
        println("*** STRAIN LOAD **")
        log.info("*** STRAIN LOAD **")
        // this query requires the allele table to be already full of data
        String strainQuery = '<query name="" model="genomic" view="Strain.primaryIdentifier Strain.name Strain.attributeString Strain.carries.symbol Strain.carries.name Strain.carries.primaryIdentifier Strain.carries.alleleType" longDescription="Returns the strains that carry the specified allele(s)." sortOrder="Strain.primaryIdentifier asc"><constraint path="Strain.carries.organism.taxonId" op="=" value="10090"/></query>'
        String url = "${MM_URL}"
        String fullQuery = url + '?query=' + strainQuery + '&format=jsonobjects'
        def strainList = loadData(fullQuery, 'strain')
        saveObjects(strainList, 1000)
    }

    /**
     * public interface to start the gene data load
     */
    void loadMouseGenes() {
        println("*** GENE LOAD **")
        log.info("*** GENE LOAD **")
        //query all mgd mouse genes where there is an entrez id or ensembl id present
        String geneQuery = '<query name="" model="genomic" view="Gene.primaryIdentifier Gene.symbol Gene.name Gene.description Gene.mgiType Gene.chromosome.symbol Gene.synonyms.value Gene.crossReferences.identifier" longDescription="" sortOrder="Gene.symbol asc" constraintLogic="A and (B or C)"><constraint path="Gene.organism.species" code="A" op="=" value="musculus"/><constraint path="Gene.crossReferences.source.name" code="B" op="=" value="Ensembl Gene Model"/><constraint path="Gene.crossReferences.source.name" code="C" op="=" value="Entrez Gene"/></query>'
        String url = "${MM_URL}"
        String fullQuery = url + '?query=' + geneQuery + '&format=jsonobjects'
        def geneList = loadData(fullQuery, 'gene')
        saveObjects(geneList, 1000)
    }

    /**
     * Add a boolean column to the VariantTranscript relationship table
     * to record most pathogenic variant
     * @return
     */
    private updateVariantTranscriptTable() {
        // if the column does not exist we create it
        def columnName = 'most_pathogenic'
        def tableName = 'variant_transcript'
        if (!columnExists(columnName, tableName)) {
            Statement updateTableStmt = connection.createStatement()
            updateTableStmt.executeUpdate("ALTER TABLE ${tableName} ADD COLUMN ${columnName} BOOL")
            log.debug('Table "variant_transcript" altered with new column created')
        }
    }

    /**
     * Add a genotype column to record genotype information for given strain
     */
    private updateVariantStrainTable() {
        def tableName = 'variant_strain'
        // This column is used to store the GT id of the genotype format column in a VCF file
        def genoColumnName = 'genotype'
        if (!columnExists(genoColumnName, tableName)) {
            Statement updateTableStmt = connection.createStatement()
            updateTableStmt.executeUpdate("ALTER TABLE ${tableName} ADD COLUMN ${genoColumnName} CHAR(4)")
            log.debug('New column ' + genoColumnName + ' created in table variant_strain')
        }
        // This column is used to store all the other info of the genotype in the format column of the VCF file
        def genoDataColumnName = 'genotype_data'
        if (!columnExists(genoDataColumnName, tableName)) {
            Statement updateTableStmt = connection.createStatement()
            updateTableStmt.executeUpdate("ALTER TABLE ${tableName} ADD COLUMN ${genoDataColumnName} VARCHAR(100)")
            log.debug('New column ' + genoDataColumnName + ' created in table variant_strain')
        }

    }

    private boolean columnExists(String columnName, String tableName) {
        Statement showColumnStmt = connection.createStatement()
        ResultSet showColumnRs = showColumnStmt.executeQuery("SHOW COLUMNS FROM ${tableName} LIKE '${columnName}'")
        if (!showColumnRs.isBeforeFirst()){
            return false
        }
        return true
    }

    private boolean tableHasValues(String tableName) {
        Statement stmt = connection.createStatement()
        ResultSet rs = stmt.executeQuery("select * FROM ${tableName}")
        return rs.next()
    }

    /**
     * request to mouse mine for data
     * @param fullQuery contains mousemine url and query
     * @param type can be 'gene', 'strain' or 'synonyms'
     * @return
     */
    protected List<GormEntity> loadData(String fullQuery, String type) {
        def jsonResult
        RestBuilder rest = new RestBuilder()
        RestResponse restResponse = rest.get(fullQuery)
        log.info("Request response = " + restResponse.statusCode.value())
        if (restResponse.statusCode.value() == 200 && restResponse.json) {
            jsonResult = restResponse.json.results
        } else {
            log.error("Response to mouse mine data request: " + restResponse.statusCode.value() + " restResponse.text= " + restResponse.text)
        }

        return parseJsonData(jsonResult, type)
    }

    private static String[] getSynonyms(JSONArray jsonArray) {
        String[] synonyms = new String[jsonArray.size()]
        for (int i = 0; i < jsonArray.size(); i++) {
            def obj = ((JSONObject) jsonArray[i]).get('value')
            synonyms[i] = obj
        }
        return synonyms
    }

    /**
     * parse mousemine json data into POJO objects
     * @param json
     * @param type can be 'gene', 'strain' or 'transcripts'
     * @return
     */
    protected List<GormEntity> parseJsonData(def json, String type) {
        List<GormEntity> mgdObj = []

        GormEntity obj

        json.each { it ->
            JSONObject mmProps = it

            if (type == 'gene') {
                Map ids = getEntrezEnsemblIds((JSONArray) mmProps.get('crossReferences'))
                obj = new Gene(
                        mgiId: mmProps.get('primaryIdentifier'),
                        symbol: mmProps.get('symbol'),
                        name: mmProps.get('name'),
                        description: mmProps.get('description'),
                        type: mmProps.get('mgiType'),
                        chr: ((JSONObject) mmProps.get('chromosome')).get('symbol'),
                        entrezGeneId: ids.get('entrez'),
                        ensemblGeneId: ids.get('ensembl')
                )
                JSONObject[] synonyms = mmProps.get('synonyms')
                for (synonym in synonyms) {
                    obj.addToSynonyms new Synonym(name: synonym.get('value'))
                }
            } else if (type == 'strain') {
                obj = new Strain(
                        primaryIdentifier: mmProps.get('primaryIdentifier'),
                        name: mmProps.get('name'),
                        attributes: mmProps.get('attributeString'),
                )
                List<String> allelesId = []
                JSONArray carries = (JSONArray) mmProps.get('carries')
                for (int i = 0; i < carries.size(); i++) {
                    allelesId.add(((JSONObject) carries[i]).get('primaryIdentifier'))
                }
                def foundAlleles = Allele.findAllByPrimaryIdentifierInList(allelesId)
                for (allele in foundAlleles) {
                    obj.addToAlleles(allele)
                }

            }
            log.info("mousemine pojo: " + obj.properties)
            mgdObj.add(obj)
        }

        mgdObj


    }

    private Map<String, String> getEntrezEnsemblIds(JSONArray ids) {
        Map<String, String> mapIds = new HashMap<String, String>()
        // both entrez and ensembl present
        if (ids.size() == 2) {
            mapIds.put('entrez', ((JSONObject) ids[0]).get('identifier'))
            mapIds.put('ensembl', ((JSONObject) ids[1]).get('identifier'))
        }
        if (ids.size() == 1) {
            String id = ((JSONObject) ids[0]).get('identifier')
            if (id.startsWith('ENS')) {
                mapIds.put('entrez', null)
                mapIds.put('ensembl', id)
            } else {
                mapIds.put('entrez', id)
                mapIds.put('ensembl', null)
            }
        }
        if (ids.size() == 0) {
            mapIds.put('entrez', null)
            mapIds.put('ensembl', null)
        }
        return mapIds
    }

    private String getMgiId(String id) {
        String[] array = id.split("_")
        String mgiId
        if (array.size() == 3) {
            mgiId = array[0] + ':' + array[2]
        } else {
            return id
        }
        return mgiId
    }

    private List<String> getMgiIds(List<String> ids) {
        List<String> mgiIds = []
        for (id in ids) {
            String[] array = id.split("_")
            String mgiId
            if (array.size() == 3) {
                mgiId = array[0] + ':' + array[2]
            } else {
                mgiId = id
            }
            mgiIds.add(mgiId)
        }
        return mgiIds
    }

    /**
     * persist data in batches
     * @param object List
     * @return
     */
    private saveObjects(List<GormEntity> mmList, int batchSize) {

        List<GormEntity> batch = []
        GormEntity obj
        mmList.eachWithIndex { mmObj, idx ->
            if (mmObj instanceof Gene) {
                obj = mmObj.properties as Gene
            } else if (mmObj instanceof Strain) {
                obj = mmObj.properties as Strain
            } else {
                throw new Exception('this type is not supported')
            }

            if (obj.validate()) {
                batch.add(obj)
            } else {
                log.error("Error with data : " + obj.properties + " This record wont be persisted")
                log.error(obj.errors.toString())
            }

            if (idx > 1 && idx % batchSize == 0) {

                log.info("Batch count" + batch.size())
                if (mmObj instanceof Gene) {
                    Gene.withTransaction {
                        batch.each { gn ->
                            gn.save(failOnError: true)
                        }
                    }
                } else if (mmObj instanceof Strain) {
                    Strain.withTransaction {
                        batch.each { st ->
                            st.save(failOnError: true)
                        }
                    }
                }
                batch.clear()
                cleanUpGorm()
            }
        }
        //save the rest
        if (batch.size() > 0) {
            batch.each { gn ->
                gn.save(failOnError: true)
            }
            batch.clear()
            cleanUpGorm()
        }
    }

    protected Connection getConnection() {
        SessionImpl sessionImpl = sessionFactory.currentSession as SessionImpl
        sessionImpl.connection()
    }

    /**
     *
     */
    private void saveGeneTranscriptsRelationships() {
        println("*** GENE/TRANSCRIPT LOAD **")
        log.info("*** GENE/TRANSCRIPT LOAD **")
        List<String> batchOfGenesIds = []
        List<Transcript> batchOfTranscripts = []

        List<Transcript> listOfTranscripts = Transcript.findAll()
        int batchSize = 1000

        listOfTranscripts.eachWithIndex { transcript, idx ->
            batchOfTranscripts.add(transcript)
            batchOfGenesIds.add(transcript.mgiGeneIdentifier)
            if (idx > 1 && idx % batchSize == 0) {

                batchInsertGeneTranscriptsJDBC(batchOfTranscripts, batchOfGenesIds)
                //clear batch lists
                batchOfGenesIds.clear()
                batchOfTranscripts.clear()
                cleanUpGorm()
            }
        }
        //last batch
        if (listOfTranscripts.size() > 0) {
            batchInsertGeneTranscriptsJDBC(batchOfTranscripts, batchOfGenesIds)
            batchOfGenesIds.clear()
            batchOfTranscripts.clear()
            cleanUpGorm()
        }
    }

    private batchInsertGeneTranscriptsJDBC(List<Transcript> batchOfTranscripts, List<String> batchOfGeneIds) {
        PreparedStatement insertGeneTranscripts = connection.prepareStatement("insert into gene_transcript (gene_transcripts_id, transcript_id) VALUES (?, ?)")
        List<String> geneMgiIds = getMgiIds(batchOfGeneIds)
        def recsGenes = Gene.findAllByMgiIdInList(geneMgiIds)
        batchOfTranscripts.eachWithIndex { transcript, idx2 ->
            // add gene/transcript relationship
            Gene geneFound = recsGenes.find {
                it.mgiId == getMgiId(transcript.mgiGeneIdentifier)
            }
            if (geneFound != null) {
                insertGeneTranscripts.setLong(1, geneFound.id)
                insertGeneTranscripts.setLong(2, transcript.id)
                insertGeneTranscripts.addBatch()
            } else {
                print 'gene not found for id: ' + transcript.mgiGeneIdentifier
            }
        }
        insertGeneTranscripts.executeBatch()
    }

    /**
     * GROM cache clearing
     */
    void cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }
}
