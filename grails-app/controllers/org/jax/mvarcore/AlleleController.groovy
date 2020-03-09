package org.jax.mvarcore


import grails.rest.*
import grails.converters.*

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
}
