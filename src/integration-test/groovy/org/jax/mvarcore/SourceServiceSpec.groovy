package org.jax.mvarcore

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class SourceServiceSpec extends Specification {

    SourceService sourceService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Source(...).save(flush: true, failOnError: true)
        //new Source(...).save(flush: true, failOnError: true)
        //Source source = new Source(...).save(flush: true, failOnError: true)
        //new Source(...).save(flush: true, failOnError: true)
        //new Source(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //source.id
    }

    void "test get"() {
        setupData()

        expect:
        sourceService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Source> sourceList = sourceService.list(max: 2, offset: 2)

        then:
        sourceList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        sourceService.count() == 5
    }

    void "test delete"() {
        Long sourceId = setupData()

        expect:
        sourceService.count() == 5

        when:
        sourceService.delete(sourceId)
        sessionFactory.currentSession.flush()

        then:
        sourceService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Source source = new Source()
        sourceService.save(source)

        then:
        source.id != null
    }
}
