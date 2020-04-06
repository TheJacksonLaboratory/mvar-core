package org.jax.mvarcore

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class GeneController {

    GeneService geneService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max, String symbol) {
        params.max = Math.min(max ?: 10, 100)
        println("symbol = " + symbol)
        if (symbol) {
            def count = Gene.countBySymbolLike('%'+ symbol +'%')
            println("gene count = " + count)
            List<Gene> genes =  Gene.findAllBySymbolLike('%'+ symbol +'%', [max: 10])
            //respond genes
            render (view:'index', model:[geneList: genes, geneCount: count])
            return
        }
        render (view:'index', model:[geneList: geneService.list(params), geneCount: geneService.count()])
    }

    def show(Long id) {
        respond geneService.get(id)
    }

//    def save(Gene allele) {
//        if (allele == null) {
//            render status: NOT_FOUND
//            return
//        }
//
//        try {
//            geneService.save(allele)
//        } catch (ValidationException e) {
//            respond allele.errors, view:'create'
//            return
//        }
//
//        respond allele, [status: CREATED, view:"show"]
//    }
//
//    def update(Gene allele) {
//        if (allele == null) {
//            render status: NOT_FOUND
//            return
//        }
//
//        try {
//            geneService.save(allele)
//        } catch (ValidationException e) {
//            respond allele.errors, view:'edit'
//            return
//        }
//
//        respond allele, [status: OK, view:"show"]
//    }
//
//    def delete(Long id) {
//        if (id == null) {
//            render status: NOT_FOUND
//            return
//        }
//
//        geneService.delete(id)
//
//        render status: NO_CONTENT
//    }
}
