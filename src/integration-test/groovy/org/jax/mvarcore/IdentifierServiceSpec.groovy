package org.jax.mvarcore

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class IdentifierServiceSpec extends Specification {

    IdentifierService indentifierService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Indentifier(...).save(flush: true, failOnError: true)
        //new Indentifier(...).save(flush: true, failOnError: true)
        //Indentifier indentifier = new Indentifier(...).save(flush: true, failOnError: true)
        //new Indentifier(...).save(flush: true, failOnError: true)
        //new Indentifier(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //indentifier.id
    }

    void "test get"() {
        setupData()

        expect:
        indentifierService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Identifier> indentifierList = indentifierService.list(max: 2, offset: 2)

        then:
        indentifierList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        indentifierService.count() == 5
    }

    void "test delete"() {
        Long indentifierId = setupData()

        expect:
        indentifierService.count() == 5

        when:
        indentifierService.delete(indentifierId)
        sessionFactory.currentSession.flush()

        then:
        indentifierService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Identifier indentifier = new Identifier()
        indentifierService.save(indentifier)

        then:
        indentifier.id != null
    }
}
