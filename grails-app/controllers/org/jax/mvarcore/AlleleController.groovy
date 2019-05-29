package org.jax.mvarcore

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class AlleleController {

    AlleleService alleleService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond alleleService.list(params), model:[alleleCount: alleleService.count()]
    }

    def show(Long id) {
        respond alleleService.get(id)
    }

    def save(Allele allele) {
        if (allele == null) {
            render status: NOT_FOUND
            return
        }

        try {
            alleleService.save(allele)
        } catch (ValidationException e) {
            respond allele.errors, view:'create'
            return
        }

        respond allele, [status: CREATED, view:"show"]
    }

    def update(Allele allele) {
        if (allele == null) {
            render status: NOT_FOUND
            return
        }

        try {
            alleleService.save(allele)
        } catch (ValidationException e) {
            respond allele.errors, view:'edit'
            return
        }

        respond allele, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        alleleService.delete(id)

        render status: NO_CONTENT
    }
}
