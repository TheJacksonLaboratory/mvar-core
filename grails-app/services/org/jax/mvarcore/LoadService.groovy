package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import org.grails.datastore.gorm.GormEntity
import org.grails.io.support.ClassPathResource
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.internal.SessionImpl

import java.sql.Connection
import java.sql.PreparedStatement


@Transactional
class LoadService {

    def sessionFactory //inject session factory

    // TODO move to config file
    private final String MM_URL = 'http://www.mousemine.org/mousemine/service/query/results'
    private final String ENSEMBL_URL = 'http://rest.ensembl.org/'

    private final static USE_FILE = false
    File geneFeedFile = new ClassPathResource('results_gene.json').file
    File strainFeedFile = new ClassPathResource('results_strain.json').file
    File transcriptFeedFile = new ClassPathResource('results_transcripts.json').file

    def serviceMethod() {

    }

    void loadData() {
        if (Gene.count() <= 0) {
            loadMouseGenes()
        }

        if (Strain.count() <= 0) {
            loadMouseStrains()
        }

        //saveGeneTranscriptsRelationships()
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
        List<Strain> strainList = loadData(fullQuery, 'strain')
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
        List<Gene> geneList = loadData(fullQuery, 'gene')
        saveObjects(geneList, 1000)
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
        List<Transcript> transcriptList = []
        ids.each { key, val ->
            Transcript transcript = loadNewTranscript(rest, url + lookupQuery, key, val)
            if (transcript)
                transcriptList.add(transcript)
        }
        // as size might be small we set the batch size to the list size (so we have only one batch
        saveObjects(transcriptList, transcriptList.size())
    }

    private Transcript loadNewTranscript(RestBuilder rest, String url, String id, List<String> geneAndVariantIds) {
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
        int start = jsonResult.get('start')
        int end = jsonResult.get('end')
        int length = (end - start) + 1
        Transcript transcript = new Transcript(
                primaryIdentifier: id,
                length: length,
                chromosome: jsonResult.get('seq_region_name'),
                locationStart: start,
                locationEnd: end,
                geneIdentifier: jsonResult.get('Parent')
        )
        // add variant/transcript relationship
        Variant variant = Variant.findById(Long.parseLong(geneAndVariantIds.get(1)))
        if (variant)
            variant.addToTranscripts(transcript)
        // add gene/transcript relationship
        Gene gene
        if (geneAndVariantIds.get(0) != '') {
            gene = Gene.createCriteria().get {
                eq('mgiId', geneAndVariantIds.get(0))
            }
        } else {
            gene = Gene.createCriteria().get {
                eq('ensemblGeneId', jsonResult.get('Parent'))
            }
        }
        if (gene)
            gene.addToTranscripts(transcript)

        return transcript
    }

    /**
     * request to mouse mine for data
     * @param fullQuery contains mousemine url and query
     * @param type can be 'gene', 'strain' or 'synonyms'
     * @return
     */
    protected List<GormEntity> loadData(String fullQuery, String type) {
        def jsonResult
        if (USE_FILE) {
            JsonSlurper slurper = new JsonSlurper(type: JsonParserType.CHARACTER_SOURCE)
            switch (type) {
                case 'strain':
                    jsonResult = slurper.parse(strainFeedFile)
                    break
                case 'gene':
                    jsonResult = slurper.parse(geneFeedFile)
                    break
                default:
                    break
            }
        } else {
            RestBuilder rest = new RestBuilder()
            RestResponse restResponse = rest.get(fullQuery)
            log.info("Request response = " + restResponse.statusCode.value())
            if (restResponse.statusCode.value() == 200 && restResponse.json) {
                jsonResult = restResponse.json.results
            } else {
                log.error("Response to mouse mine data request: " + restResponse.statusCode.value() + " restResponse.text= " + restResponse.text)
            }
        }
        return parseJsonData(jsonResult, type)
    }

    private String[] getSynonyms(JSONArray jsonArray) {
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
            } else if (mmObj instanceof Transcript) {
                obj = mmObj.properties as Transcript
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
                } else if (mmObj instanceof Transcript) {
                    Transcript.withTransaction {
                        batch.each { tr ->
                            tr.save(failOnError: true)
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

        List<String> batchOfGenesIds = []
        List<Transcript> batchOfTranscripts = []

        List<Transcript> listOfTranscripts = Transcript.findAll()
        int batchSize = 1000

        listOfTranscripts.eachWithIndex { transcript, idx ->
            batchOfTranscripts.add(transcript)
            batchOfGenesIds.add(transcript.geneIdentifier)
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
                it.mgiId == getMgiId(transcript.geneIdentifier)
            }
            if (geneFound != null) {
                insertGeneTranscripts.setLong(1, geneFound.id)
                insertGeneTranscripts.setLong(2, transcript.id)
                insertGeneTranscripts.addBatch()
            } else {
                print 'gene not found for id: ' + transcript.geneIdentifier
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
