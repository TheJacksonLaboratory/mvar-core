package org.jax.mvarcore

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class SourceController {

    SourceService sourceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond sourceService.list(params), model:[sourceCount: sourceService.count()]
    }

    def show(Long id) {
        respond sourceService.get(id)
    }

    def save(Source source) {
        if (source == null) {
            render status: NOT_FOUND
            return
        }

        try {
            sourceService.save(source)
        } catch (ValidationException e) {
            respond source.errors, view:'create'
            return
        }

        respond source, [status: CREATED, view:"show"]
    }

    def update(Source source) {
        if (source == null) {
            render status: NOT_FOUND
            return
        }

        try {
            sourceService.save(source)
        } catch (ValidationException e) {
            respond source.errors, view:'edit'
            return
        }

        respond source, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        sourceService.delete(id)

        render status: NO_CONTENT
    }
}
