package org.jax.mvarcore

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class VariantCanonIdentifierServiceSpec extends Specification {

    VariantCanonIdentifierService variantCanonIdentifierService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new VariantCanonIdentifier(...).save(flush: true, failOnError: true)
        //new VariantCanonIdentifier(...).save(flush: true, failOnError: true)
        //VariantCanonIdentifier variantCanonIdentifier = new VariantCanonIdentifier(...).save(flush: true, failOnError: true)
        //new VariantCanonIdentifier(...).save(flush: true, failOnError: true)
        //new VariantCanonIdentifier(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //variantCanonIdentifier.id
    }

    void "test get"() {
        setupData()

        expect:
        variantCanonIdentifierService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<VariantCanonIdentifier> variantCanonIdentifierList = variantCanonIdentifierService.list(max: 2, offset: 2)

        then:
        variantCanonIdentifierList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        variantCanonIdentifierService.count() == 5
    }

    void "test delete"() {
        Long variantCanonIdentifierId = setupData()

        expect:
        variantCanonIdentifierService.count() == 5

        when:
        variantCanonIdentifierService.delete(variantCanonIdentifierId)
        sessionFactory.currentSession.flush()

        then:
        variantCanonIdentifierService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        VariantCanonIdentifier variantCanonIdentifier = new VariantCanonIdentifier()
        variantCanonIdentifierService.save(variantCanonIdentifier)

        then:
        variantCanonIdentifier.id != null
    }
}
