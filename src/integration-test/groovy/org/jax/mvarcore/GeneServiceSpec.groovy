package org.jax.mvarcore

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class GeneServiceSpec extends Specification {

    GeneService alleleService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Allele(...).save(flush: true, failOnError: true)
        //new Allele(...).save(flush: true, failOnError: true)
        //Allele allele = new Allele(...).save(flush: true, failOnError: true)
        //new Allele(...).save(flush: true, failOnError: true)
        //new Allele(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //allele.id
    }

    void "test get"() {
        setupData()

        expect:
        alleleService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Gene> alleleList = alleleService.list(max: 2, offset: 2)

        then:
        alleleList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        alleleService.count() == 5
    }

    void "test delete"() {
        Long alleleId = setupData()

        expect:
        alleleService.count() == 5

        when:
        alleleService.delete(alleleId)
        sessionFactory.currentSession.flush()

        then:
        alleleService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Gene allele = new Gene()
        alleleService.save(allele)

        then:
        allele.id != null
    }
}
