package org.jax.mvarcore

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class StrainController {

    StrainService strainService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond strainService.list(params), model:[strainCount: strainService.count()]
    }

    def show(Long id) {
        respond strainService.get(id)
    }

    def save(Strain strain) {
        if (strain == null) {
            render status: NOT_FOUND
            return
        }

        try {
            strainService.save(strain)
        } catch (ValidationException e) {
            respond strain.errors, view:'create'
            return
        }

        respond strain, [status: CREATED, view:"show"]
    }

    def update(Strain strain) {
        if (strain == null) {
            render status: NOT_FOUND
            return
        }

        try {
            strainService.save(strain)
        } catch (ValidationException e) {
            respond strain.errors, view:'edit'
            return
        }

        respond strain, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        strainService.delete(id)

        render status: NO_CONTENT
    }
}
