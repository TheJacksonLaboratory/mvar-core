package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse

import org.grails.io.support.ClassPathResource
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject


@Transactional
class LoadService {

    def sessionFactory //inject session factory

    // TODO move to config file
    private final String MM_URL = 'http://www.mousemine.org/mousemine/service/query/results'

    File geneFeedFile = new ClassPathResource('gene_seed.json').file


    def serviceMethod() {

    }
    

    /**
     * public interface to start the strain data load
     */
    void loadMouseStrains(){
        println("*** STRAIN LOAD **")
        log.info("*** STRAIN LOAD **")
        String strainQuery = '<query name="" model="genomic" view="Strain.primaryIdentifier Strain.name Strain.attributeString Strain.carries.symbol Strain.carries.name Strain.carries.primaryIdentifier Strain.carries.alleleType" longDescription="Returns the strains that carry the specified allele(s)." sortOrder="Strain.primaryIdentifier asc"><constraint path="Strain.carries.organism.taxonId" op="=" value="10090"/></query>'
        String url = "${MM_URL}"
        String fullQuery = url + '?query=' + strainQuery + '&format=jsonobjects'
        List<Strain> strainList = loadMouseMineData(fullQuery, 'strain')
        saveMouseMineObjects(strainList)
    }

    /**
     * public interface to start the gene data load
     */
    void loadMouseGenes(){
        println("*** GENE LOAD **")
        log.info("*** GENE LOAD **")
        //query all mgd mouse genes where there is an entrez id or ensembl id present
        String geneQuery = '<query name="" model="genomic" view="Gene.primaryIdentifier Gene.symbol Gene.name Gene.description Gene.mgiType Gene.chromosome.symbol Gene.synonyms.value" longDescription="" sortOrder="Gene.symbol asc" constraintLogic="A and (B or C)"><constraint path="Gene.organism.species" code="A" op="=" value="musculus"/><constraint path="Gene.crossReferences.source.name" code="B" op="=" value="Ensembl Gene Model"/><constraint path="Gene.crossReferences.source.name" code="C" op="=" value="Entrez Gene"/></query>'
        String url = "${MM_URL}"
        String fullQuery = url + '?query=' + geneQuery + '&format=jsonobjects'
        List<Gene> geneList = loadMouseMineData(fullQuery, 'gene')
        saveMouseMineObjects(geneList)
    }

    /**
     * request to mouse mine for data
     * @param fullQuery contains mousemine url and query
     * @param type can be 'gene', 'strain' or 'synonyms'
     * @return
     */
    protected List<IMouseMineObject> loadMouseMineData(String fullQuery, String type){
        RestBuilder rest = new RestBuilder()
        RestResponse restResponse = rest.get(fullQuery)
        log.info("Request response = " + restResponse.statusCode.value())
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            return parseJsonData(restResponse.json.results, type)
        }
        log.error("Response to mouse mine data request: " + restResponse.statusCode.value() + " restResponse.text= " + restResponse.text)

        null
    }

    private String getSynonyms(JSONArray jsonArray) {
        String synonyms = ''
        for (int i=0; i < jsonArray.size(); i++) {
            def obj = ((JSONObject) jsonArray[i]).get('value')
            synonyms = synonyms != '' ? synonyms + ',' + obj : obj
        }
        return synonyms
    }

    /**
     * parse mousemine json data into POJO objects
     * @param json
     * @param type can be 'gene', 'strain' or 'synonyms'
     * @return
     */
    protected List<IMouseMineObject> parseJsonData(JSONElement json, String type){

        List<IMouseMineObject> mgdObj = []
        IMouseMineObject obj
        json.each { it->
            JSONObject mmProps =  it
            if (type == 'gene') {
                obj = new Gene(
                        mgiId: mmProps.get('primaryIdentifier'),
                        symbol: mmProps.get('symbol'),
                        name: mmProps.get('name'),
                        description: mmProps.get('description'),
                        type: mmProps.get('mgiType'),
                        chr: ((JSONObject) mmProps.get('chromosome')).get('symbol'),
                        synonyms: getSynonyms(mmProps.get('synonyms')),
                )
            } else if (type == 'strain') {
                obj = new Strain(
                        identifier: mmProps.get('primaryIdentifier'),
                        name: mmProps.get('name'),
                        description: mmProps.get('attributeString'),
                        carriesAlleleSymbol: ((JSONObject) mmProps.get('carries')[0]).get('symbol'),
                        carriesAlleleName: ((JSONObject) mmProps.get('carries')[0]).get('name'),
                        carriesAlleleIdentifier: ((JSONObject) mmProps.get('carries')[0]).get('primaryIdentifier'),
                        carriesAlleleType: ((JSONObject) mmProps.get('carries')[0]).get('alleleType')
                )
            }
            log.info("mousemine pojo: " + obj.properties)
            mgdObj.add(obj)
        }
        mgdObj
    }

    /**
     * persist mousemine data in batches
     * @param mousemine object List
     * @return
     */
    private saveMouseMineObjects(List<IMouseMineObject> mmList){

        List<IMouseMineObject> batch = []
        int batchSize = 500
        IMouseMineObject obj
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
            }else{
                log.error("Error with mousemine data : " + obj.properties + " This record wont be persisted")
                log.error(obj.errors.toString())
            }

            if (idx > 1 && idx % batchSize == 0) {

                log.info("Batch count" + batch.size())
                if (mmObj instanceof Gene) {
                    Gene.withTransaction {
                        batch.each { gn ->
                            gn.save(failOnError:true)
                        }
                    }
                } else if (mmObj instanceof Strain) {
                    Strain.withTransaction {
                        batch.each { st ->
                            st.save(failOnError:true)
                        }
                    }
                }
                batch.clear()
                cleanUpGorm()
            }
        }
        //save the rest
        if (batch.size() > 0){
            batch.each { gn ->
                gn.save(failOnError:true)
            }
            batch.clear()
            cleanUpGorm()
        }
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
