package org.jax.mvarcore

import grails.validation.ValidationException
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static org.springframework.http.HttpStatus.*

@Api(value = "/api/v1", tags = ["gene"], description = "Gene Api's")
class GeneController {

    static namespace = "v1"

    GeneService geneService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    @ApiOperation(
            value = "Search Genes",
            nickname = "gene",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Gene.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),
            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "symbol", paramType = "query", required = false, value = "Gene Symbol", dataType = "string"),
            @ApiImplicitParam(name = "mvar", paramType = "query", required = false, value = "false or true, if true search is done through all genes", dataType = "boolean"),
            @ApiImplicitParam(name = "all", paramType = "query", required = false, value = "if true (or present), all mvar Genes are returned.", dataType = "boolean"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)

        // Search through all genes
        if (!params.mvar && params.symbol) {
            def count = Gene.countByNameLike('%'+ params.symbol +'%')
            println("gene count = " + count)
            List<Gene> genes =  Gene.findAllBySymbolLike('%'+ params.symbol +'%', [max: 10])
            //respond genes
            render (view:'index', model:[geneList: genes, geneCount: genes.size()])
            return
        } else if (params.mvar && params.symbol) { // search through mvar genes
            Map<String, Object> genes = geneService.getMvarGenes(params)
            render (view:'index', model:[geneList: genes.mvarGeneList, geneCount: genes.mvarGeneCount])
            return
        }
        if (params.all) {
            Map<String, Object> genes = geneService.getAllMvarGenes()
            render (view:'index', model:[geneList: genes.mvarGeneList, geneCount: genes.mvarGeneCount])
            return
        }

        render (view:'index', model:[geneList: geneService.list(params), geneCount: geneService.count()])
    }

    @ApiOperation(
            value = "List Genes",
            nickname = "gene/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Gene.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Gene Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond geneService.get(id)
    }

    @ApiOperation(
            value = "Query Genes",
            nickname = "gene/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Gene.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "offset", paramType = "query", required = false, value = "Offset value", dataType = "long"),
            @ApiImplicitParam(name = "sortBy", paramType = "query", required = false, value = "Sorting condition", dataType = "string"),
            @ApiImplicitParam(name = "sortDirection", paramType = "query", required = false, value = "asc or desc", dataType = "string"),
            @ApiImplicitParam(name = "name", paramType = "query", required = false, value = "Gene name", dataType = "string"),
            @ApiImplicitParam(name = "type", paramType = "query", required = false, value = "Gene type", dataType = "string"),
            @ApiImplicitParam(name = "chr", paramType = "query", required = false, value = "chromosome", dataType = "string"),
            @ApiImplicitParam(name = "ensembl_id", paramType = "query", required = false, value = "ENSEMBL id", dataType = "string"),
            @ApiImplicitParam(name = "mgi_id", paramType = "query", required = false, value = "MGI id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def query() {
        if (params == null) {
            log.info("params are null")
            render status: NO_CONTENT
            return
        }
        log.info("params =" + params)
        Map<String, Object> queryResults = geneService.query(params)
        log.info('results gene count =' + queryResults.geneCount)
        render(view: 'index', model: [geneList: queryResults.geneList, geneCount: queryResults.geneCount])
    }
}
