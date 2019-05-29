package org.jax.mvarcore

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class StrainServiceSpec extends Specification {

    StrainService strainService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Strain(...).save(flush: true, failOnError: true)
        //new Strain(...).save(flush: true, failOnError: true)
        //Strain strain = new Strain(...).save(flush: true, failOnError: true)
        //new Strain(...).save(flush: true, failOnError: true)
        //new Strain(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //strain.id
    }

    void "test get"() {
        setupData()

        expect:
        strainService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Strain> strainList = strainService.list(max: 2, offset: 2)

        then:
        strainList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        strainService.count() == 5
    }

    void "test delete"() {
        Long strainId = setupData()

        expect:
        strainService.count() == 5

        when:
        strainService.delete(strainId)
        sessionFactory.currentSession.flush()

        then:
        strainService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Strain strain = new Strain()
        strainService.save(strain)

        then:
        strain.id != null
    }
}
