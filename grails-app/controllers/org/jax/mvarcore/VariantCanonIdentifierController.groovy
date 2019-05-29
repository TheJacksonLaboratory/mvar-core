package org.jax.mvarcore

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class VariantCanonIdentifierController {

    VariantCanonIdentifierService variantCanonIdentifierService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond variantCanonIdentifierService.list(params), model:[variantCanonIdentifierCount: variantCanonIdentifierService.count()]
    }

    def show(Long id) {
        respond variantCanonIdentifierService.get(id)
    }

    def save(VariantCanonIdentifier variantCanonIdentifier) {
        if (variantCanonIdentifier == null) {
            render status: NOT_FOUND
            return
        }

        try {
            variantCanonIdentifierService.save(variantCanonIdentifier)
        } catch (ValidationException e) {
            respond variantCanonIdentifier.errors, view:'create'
            return
        }

        respond variantCanonIdentifier, [status: CREATED, view:"show"]
    }

    def update(VariantCanonIdentifier variantCanonIdentifier) {
        if (variantCanonIdentifier == null) {
            render status: NOT_FOUND
            return
        }

        try {
            variantCanonIdentifierService.save(variantCanonIdentifier)
        } catch (ValidationException e) {
            respond variantCanonIdentifier.errors, view:'edit'
            return
        }

        respond variantCanonIdentifier, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        variantCanonIdentifierService.delete(id)

        render status: NO_CONTENT
    }
}
