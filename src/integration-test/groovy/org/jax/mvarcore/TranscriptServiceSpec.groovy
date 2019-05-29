package org.jax.mvarcore

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class TranscriptServiceSpec extends Specification {

    TranscriptService transcriptService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Transcript(...).save(flush: true, failOnError: true)
        //new Transcript(...).save(flush: true, failOnError: true)
        //Transcript transcript = new Transcript(...).save(flush: true, failOnError: true)
        //new Transcript(...).save(flush: true, failOnError: true)
        //new Transcript(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //transcript.id
    }

    void "test get"() {
        setupData()

        expect:
        transcriptService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Transcript> transcriptList = transcriptService.list(max: 2, offset: 2)

        then:
        transcriptList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        transcriptService.count() == 5
    }

    void "test delete"() {
        Long transcriptId = setupData()

        expect:
        transcriptService.count() == 5

        when:
        transcriptService.delete(transcriptId)
        sessionFactory.currentSession.flush()

        then:
        transcriptService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Transcript transcript = new Transcript()
        transcriptService.save(transcript)

        then:
        transcript.id != null
    }
}
