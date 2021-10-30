package org.jax.mvarcore

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.internal.SessionImpl

import java.sql.Connection
import java.sql.SQLException

//@Service(Gene)
@Transactional
class GeneService {

    SessionFactory sessionFactory
    // List of Mvar Genes
    List<Gene> mvarGenes

    def getGenesVariants(String geneSymbol, int max) {

        if (!mvarGenes) {
            mvarGenes = []
            try {
                final Sql sql = getSql()
                def result = sql.rows("SELECT * FROM mvar_core.gene where id in (select distinct gene_id from variant);")
                if (result) {
                    for (int i = 0; i < result.size(); i++) {
                        Gene gene = new Gene()
                        gene.id = result.id[i]
                        gene.name = result.name[i]
                        gene.symbol = result.symbol[i]
                        gene.chr = result.chr[i]
                        gene.description = result.description[i]
                        gene.ensemblGeneId = result.ensembl_gene_id[i]
                        gene.entrezGeneId = result.entrez_gene_id[i]
                        gene.type = result.type[i]
                        gene.mgiId = result.mgi_id[i]
                        mvarGenes.add(gene)
                    }
                }
            }catch (SQLException exc) {
                log.debug('The following SQLException occurred: ' + exc.toString())
            } finally {
                cleanUpGorm()
            }
        }
        int idx = 0
        List<Gene> genes = []
        for (int i = 0; i < mvarGenes.size(); i++) {
            // limit number of genes returned to max
            if (idx == max)
                return genes
            if (mvarGenes.get(i).symbol.toLowerCase().contains(geneSymbol) && !genes.contains(mvarGenes.get(i))) {
                genes.add(mvarGenes.get(i))
                idx++
            }
        }
        return genes
    }

    Map<String, Object> query(Map params) {

        Map<String, Object> queryResults = [geneList:[], geneCount:0L]

        //max
        Integer max = params.max? Integer.valueOf(params.max): 10 as Integer
        //offset
        Long offset = params.offset? Long.valueOf(params.offset) : 0

        //sort by
        String orderBy = params.sortBy
        //sort direction
        String orderDirection = params.sortDirection? params.sortDirection: 'asc'

        println('query params: ' + params)

        //name
        def nameList = params.list('name')

        // chr
        def chrList = params.list('chr')

        //type
        def typeList = params.list('type')

        // ensembl id
        def ensemblList = params.list('ensembl_id')

        //TYPE
        def mgiList = params.list('mgi_id')


        //generate query
        def results = Gene.createCriteria().list ([max:max, offset:offset]) {

            if (nameList) {
                and {
                    like('name', nameList)
                }
            }

            if (chrList) {
                and {
                    inList ('chr', chrList)
                }
            }

            if (typeList) {
                and {
                    inList('type', typeList)
                }
            }

            if (ensemblList) {
                and {
                    inList('ensembl_gene_id', ensemblList)
                }
            }

            if (mgiList) {
                and {
                    inList('mgi_id', mgiList)
                }
            }

            //handle order by
//            if (orderBy) {
//                if (orderBy == 'symbol') {
//                    gene {
//                        order('symbol', orderDirection)
//                    }
//                } else if (orderBy == 'strainId') {
//                    strain {
//                        order('sampleId', orderDirection)
//                    }
//                } else{
//                    order(orderBy, orderDirection)
//                }
//
//            }
        }

        Long count = results.totalCount

        println("gene search results count = " + count)
        log.info("gene search results count = " + count)

        queryResults.geneList = results
        queryResults.geneCount = count

        return queryResults
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
}
