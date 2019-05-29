package org.jax.mvarcore

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class VariantController {

    VariantService variantService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond variantService.list(params), model:[variantCount: variantService.count()]
    }

    def show(Long id) {
        respond variantService.get(id)
    }

    def save(Variant variant) {
        if (variant == null) {
            render status: NOT_FOUND
            return
        }

        try {
            variantService.save(variant)
        } catch (ValidationException e) {
            respond variant.errors, view:'create'
            return
        }

        respond variant, [status: CREATED, view:"show"]
    }

    def update(Variant variant) {
        if (variant == null) {
            render status: NOT_FOUND
            return
        }

        try {
            variantService.save(variant)
        } catch (ValidationException e) {
            respond variant.errors, view:'edit'
            return
        }

        respond variant, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        variantService.delete(id)

        render status: NO_CONTENT
    }
}
