package org.jax.mvarcore

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class VariantServiceSpec extends Specification {

    VariantService variantService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Variant(...).save(flush: true, failOnError: true)
        //new Variant(...).save(flush: true, failOnError: true)
        //Variant variant = new Variant(...).save(flush: true, failOnError: true)
        //new Variant(...).save(flush: true, failOnError: true)
        //new Variant(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //variant.id
    }

    void "test get"() {
        setupData()

        expect:
        variantService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Variant> variantList = variantService.list(max: 2, offset: 2)

        then:
        variantList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        variantService.count() == 5
    }

    void "test delete"() {
        Long variantId = setupData()

        expect:
        variantService.count() == 5

        when:
        variantService.delete(variantId)
        sessionFactory.currentSession.flush()

        then:
        variantService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Variant variant = new Variant()
        variantService.save(variant)

        then:
        variant.id != null
    }
}
