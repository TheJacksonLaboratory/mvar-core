package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse

import org.grails.io.support.ClassPathResource
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement

@Transactional
class LoadService {

    def sessionFactory //inject session factory

    File geneFeedFile = new ClassPathResource('gene_seed.json').file


    def serviceMethod() {

    }
    

    /**
     * public interface to start the strain data load
     */
    void loadMouseStrains(){
        println("*** STRAIN LOAD **")
        log.info("*** STRAIN LOAD **")
        List<Strain> strainList = loadMouseMineStrainData()
        saveStrains(strainList)
    }

    /**
     * request to mouse mine for strain data
     * @return
     */
    protected List<Strain> loadMouseMineStrainData(){

        //TODO move these values to configuration
        String mouseMineUrl = 'http://www.mousemine.org/mousemine/service/query/results'
        String query = '<query name="" model="genomic" view="Strain.primaryIdentifier Strain.name Strain.attributeString Strain.carries.symbol Strain.carries.name Strain.carries.primaryIdentifier Strain.carries.alleleType" longDescription="Returns the strains that carry the specified allele(s)." sortOrder="Strain.primaryIdentifier asc"><constraint path="Strain.carries.organism.taxonId" op="=" value="10090"/></query>'
        String format = 'json'

        RestBuilder rest = new RestBuilder()
        String url = "${mouseMineUrl}"

        RestResponse restResponse = rest.get(url + '?query=' + query + '&format=' + format)

        log.info("Request response = " + restResponse.statusCode.value())

        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {

            return parseStrainJsonData(restResponse.json.results)
        }

        log.error("Response to mouse mine strain data request: " + restResponse.statusCode.value() + " restResponse.text= " + restResponse.text)

        null
    }

    /**
     * parse strain json data into POJO objects
     * @param json
     * @return
     */
    protected List<Strain> parseStrainJsonData(JSONElement json){

        List<Strain> mgiStrains = []

        json.each { it->
            JSONArray strainProps =  it

            Strain strain = new Strain(
                    identifier: strainProps.isNull(0) ? null : strainProps.get(0),
                    name: strainProps.isNull(1) ? null : strainProps.get(1),
                    description: strainProps.isNull(2) ? null : strainProps.get(2),
                    carriesAlleleSymbol: strainProps.isNull(3) ? null : strainProps.get(3),
                    carriesAlleleName: strainProps.isNull(4) ? null : strainProps.get(4),
                    carriesAlleleIdentifier: strainProps.isNull(5) ? null : strainProps.get(5),
                    carriesAlleleType: strainProps.isNull(6) ? null : strainProps.get(6)
            )

            log.info("Strain pojo: " + strain.properties)

           mgiStrains.add(strain)
        }

        mgiStrains
    }

    /**
     * persist strain data in batches
     * @param mgiStrainList
     * @return
     */
    private saveStrains(List<Strain> mgiStrainList){

        List<Strain> strainBatch = []
        int batchSize = 500

        mgiStrainList.eachWithIndex { mgiStrain, idx ->

            Strain strain = mgiStrain.properties as Strain

            if (strain.validate()) {
                strainBatch.add(strain)
            }else{
                log.error("Error with strain data : " + strain.properties + " This record wont be persisted")
                log.error(strain.errors.toString())
            }

            if (idx > 1 && idx % batchSize == 0) {

                    log.info("strainBatch count" + strainBatch.size())
                    Strain.withTransaction {

                        strainBatch.each { st ->
                            st.save(failOnError:true)
                        }
                    }

                    strainBatch.clear()
                    cleanUpGorm()
            }
        }

        //save the rest
        if (strainBatch.size() > 0){
            strainBatch.each { st ->
                st.save(failOnError:true)
            }
            strainBatch.clear()
            cleanUpGorm()
        }

    }

    /**
     * public interface to start the gene data load
     */
    void loadMouseGenes(){


        println("*** GENE LOAD **")
        log.info("*** GENE LOAD **")
        List<Gene> geneList = loadMouseMineGeneData()
        saveGenes(geneList)
    }

    /**
     * request to mouse mine for gene data
     * @return
     */
    protected List<Gene> loadMouseMineGeneData(){

        //TODO move these values to configuration
        String mouseMineUrl = 'http://www.mousemine.org/mousemine/service/query/results'
        //query all mgd mouse genes where there is an entrez id or ensembl id present
        String query = '<query name="" model="genomic" view="Gene.primaryIdentifier Gene.symbol Gene.name Gene.description Gene.mgiType Gene.chromosome.symbol" longDescription="" sortOrder="Gene.symbol asc" constraintLogic="A and (B or C)"><constraint path="Gene.organism.species" code="A" op="=" value="musculus"/><constraint path="Gene.crossReferences.source.name" code="B" op="=" value="Ensembl Gene Model"/><constraint path="Gene.crossReferences.source.name" code="C" op="=" value="Entrez Gene"/></query>'
        String format = 'json'

        RestBuilder rest = new RestBuilder()
        String url = "${mouseMineUrl}"

        RestResponse restResponse = rest.get(url + '?query=' + query + '&format=' + format)

        log.info("Request response = " + restResponse.statusCode.value())

        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {

            return parseGeneJsonData(restResponse.json.results)
        }

        log.error("Response to mouse mine gene data request: " + restResponse.statusCode.value() + " restResponse.text= " + restResponse.text)

        null
    }

    /**
     * parse gene json data into POJO objects
     * @param json
     * @return
     */
    protected List<Gene> parseGeneJsonData(JSONElement json){

        List<Gene> mgdGenes = []

        json.each { it->
            JSONArray geneProps =  it

            Gene gene = new Gene(
                    mgiId: geneProps.isNull(0) ? null : geneProps.get(0),
                    symbol: geneProps.isNull(1) ? null : geneProps.get(1),
                    name: geneProps.isNull(2) ? null : geneProps.get(2),
                    description: geneProps.isNull(3) ? null : geneProps.get(3),
                    type: geneProps.isNull(4) ? null : geneProps.get(4),
                    chr: geneProps.isNull(5) ? null : geneProps.get(5),
            )

            log.info("Gene pojo: " + gene.properties)

            mgdGenes.add(gene)
        }

        mgdGenes
    }

    /**
     * persist gene data in batches
     * @param geneList
     * @return
     */
    private saveGenes(List<Gene> geneList){

        List<Gene> geneBatch = []
        int batchSize = 500

        geneList.eachWithIndex { mgdGene, idx ->

            Gene gene = mgdGene.properties as Gene

            if (gene.validate()) {
                geneBatch.add(gene)
            }else{
                log.error("Error with gene data : " + gene.properties + " This record wont be persisted")
                log.error(gene.errors.toString())
            }

            if (idx > 1 && idx % batchSize == 0) {

                log.info("geneBatch count" + geneBatch.size())
                Gene.withTransaction {

                    geneBatch.each { gn ->
                        gn.save(failOnError:true)
                    }
                }

                geneBatch.clear()
                cleanUpGorm()
            }
        }

        //save the rest
        if (geneBatch.size() > 0){
            geneBatch.each { gn ->
                gn.save(failOnError:true)
            }
            geneBatch.clear()
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
