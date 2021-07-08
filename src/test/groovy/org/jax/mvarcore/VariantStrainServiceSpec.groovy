package org.jax.mvarcore

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class VariantStrainServiceSpec extends Specification implements ServiceUnitTest<VariantStrainService>, DataTest{

    def setup() {
        mockDomain MvarStrains
        mockDomain Strain
        mockDomain VariantCanonIdentifier
        mockDomain Variant
        mockDomain VariantStrain
        mockDomain Gene
        setupData()
    }

    def setupData(){
        Gene g1 = new Gene(name:'abc', symbol: "ABC", chr:'1', mgiId: "mg1").save(failOnError: true)
        Gene g2 = new Gene(name:'def', symbol: "DEF", chr:'1', mgiId: "mg1").save(failOnError: true)

        Strain s1  = new Strain(name:'s1', primaryIdentifier:'PI_1').save(failOnError: true)
        Strain s2  = new Strain(name:'s2', primaryIdentifier:'PI_2').save(failOnError: true)

        MvarStrains mvarS1 = new MvarStrains(name:'s1', strain: s1).save(failOnError: true)
        MvarStrains mvarS2 = new MvarStrains(name:'s2', strain: s2).save(failOnError: true)

        VariantCanonIdentifier vci1 = new VariantCanonIdentifier(chr:'1', position: '12314', ref:'A', alt:'C',
                variantRefTxt:'vrt').save(failOnError: true)
        VariantCanonIdentifier vci2 = new VariantCanonIdentifier(chr:'2', position: '45678', ref:'A', alt:'C',
                variantRefTxt:'vrt').save(failOnError: true)

        Variant var1 = new Variant(chr:'1', position: '10000', ref:'A', alt:'C',  assembly:'38',
                parentVariantRefTxt:'test', parentRefInd:false, strainName:'s', variantRefTxt:'test', type:'a',
                accession:'rs1234', canonVarIdentifier:vci1, gene: g1).save(failOnError: true, flush:true)

        Variant var2 = new Variant(chr:'1', position: '20000', ref:'T', alt:'C',  assembly:'38',
                parentVariantRefTxt:'test', parentRefInd:false, strainName:'s', variantRefTxt:'test', type:'a',
                accession:'rs5678', canonVarIdentifier:vci2, gene:g2).save(failOnError: true, flush:true)

        new VariantStrain(variant: Variant.findByPosition('10000'), strain: Strain.findByName('s1'), genotype: '1/1').save(failOnError: true)
        new VariantStrain(variant: Variant.findByPosition('20000'), strain: Strain.findByName('s1'), genotype: '1/1').save(failOnError: true)
        new VariantStrain(variant: Variant.findByPosition('20000'), strain: Strain.findByName('s2'), genotype: '1/1').save(failOnError: true)

    }

    def cleanup() {
    }


    void "Test query by gene"(){

        when:
            def results = service.query(params)

        then:
            results.variantList.size() == expectSize
            results.variantCount == expectSize
            results.variantList[0]?.position == expectPosition

        where:

            params                  | expectSize |   expectPosition |   desc
            [genes:[]]              |    0       |   null           | 'no results found'
            [genes:['XXX']]         |    0       |   null           | 'no results found'
            [genes:['ABC']]         |    1       |   10000          | 'gene 1'
            [genes:['DEF']]         |    1       |   20000          | 'gene 2'
            [genes:['ABC', 'DEF']]  |    2       |   10000          | 'two genes'

    }

    void "Test query by position coordinates"(){

        when:
        def results = service.query(params)

        then:
        results.variantList.size() == expectSize
        results.variantCount == expectSize
        results.variantList[0]?.position == expectPosition

        where:

        params                               | expectSize |   expectPosition |   desc
        [chr:1]                              |    0       |   null           | 'Invalid parameters'
        [chr:1, startPos:5000]               |    0       |   null           | 'Invalid parameters'
        [chr:1, endPos:5000]                 |    0       |   null           | 'Invalid parameters'
        [chr:1, startPos:1000, endPos:5000]  |    0       |   null           | 'Out of range coordinates'
        [chr:1, startPos:9000, endPos:11000] |    1       |   10000          | 'In range for one var 1'
        [chr:1, startPos:11000, endPos:21000]|    1       |   20000          | 'In range for one var 2'
        [chr:1, startPos:9000, endPos:21000] |    2       |   10000          | 'In range for two vars'
    }

    void "Test invalid number of parameters"(){

        when:
            service.query([])

        then:
            thrown IllegalArgumentException
    }

    void "Test strains with variants in DB"(){

        when:
            def strains = service.getDBStrains()
        then:
            strains.size() == 2
            strains[0].name == 's1'
            strains[1].name == 's2'
    }

    void "Test query by dbSNPId"(){

        when:
        def results = service.query(params)

        then:
        results.variantList.size() == expectSize
        results.variantCount == expectSize
        results.variantList[0]?.accession == expectDBsnpId

        where:

        params                               | expectSize |   expectDBsnpId  |   desc
        [dbSNPidList:['rsInvalid']]          |    0       |   null           | 'Invalid parameters'
        [dbSNPidList:['rs1234']]             |    1       |   'rs1234'       | 'var 1'
        [dbSNPidList:['rs5678']]             |    1       |   'rs5678'       | 'var 2'
        [dbSNPidList:['rs1234', 'rs5678']]   |    2       |   'rs1234'       | 'var 1 and var 2'
    }
}
