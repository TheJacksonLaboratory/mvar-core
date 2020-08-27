package org.jax.mvarcore


import grails.rest.*
import grails.converters.*

class GenotypeController {
	static responseFormats = ['json', 'xml']
	
    def index() { }

    def show(Long id) {
        respond genotypeService.get(id)
    }
}
