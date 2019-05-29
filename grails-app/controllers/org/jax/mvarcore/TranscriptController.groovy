package org.jax.mvarcore

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class TranscriptController {

    TranscriptService transcriptService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond transcriptService.list(params), model:[transcriptCount: transcriptService.count()]
    }

    def show(Long id) {
        respond transcriptService.get(id)
    }

    def save(Transcript transcript) {
        if (transcript == null) {
            render status: NOT_FOUND
            return
        }

        try {
            transcriptService.save(transcript)
        } catch (ValidationException e) {
            respond transcript.errors, view:'create'
            return
        }

        respond transcript, [status: CREATED, view:"show"]
    }

    def update(Transcript transcript) {
        if (transcript == null) {
            render status: NOT_FOUND
            return
        }

        try {
            transcriptService.save(transcript)
        } catch (ValidationException e) {
            respond transcript.errors, view:'edit'
            return
        }

        respond transcript, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        transcriptService.delete(id)

        render status: NO_CONTENT
    }
}
