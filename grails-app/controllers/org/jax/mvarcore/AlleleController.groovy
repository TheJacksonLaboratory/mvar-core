package org.jax.mvarcore


import grails.rest.*
import grails.converters.*
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static org.springframework.http.HttpStatus.NO_CONTENT

@Api(value = "/api/v1", tags = ["allele"], description = "Allele Api's")
class AlleleController {

    static namespace = "v1"

    AlleleService alleleService
	static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    @ApiOperation(
            value = "Search Alleles",
            nickname = "allele/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Allele.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),
            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "symbol", paramType = "query", required = false, value = "Allele Symbol", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        if (params.symbol) {
            def count = Allele.countByNameLike('%'+ params.symbol +'%')
            println("allele count = " + count)
            List<Allele> alleles =  Allele.findAllBySymbolLike('%'+ params.symbol +'%', [max: 10])
            //respond alleles
            render (view:'index', model:[alleleList: alleles, alleleCount: count])
            return
        }
        render (view:'index', model:[alleleList: alleleService.list(params), geneCount: alleleService.count()])

//        respond alleleService.list(params), model:[alleleCount: alleleService.count()]
    }

    @ApiOperation(
            value = "List Alleles",
            nickname = "allele/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Allele.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Allele Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond alleleService.get(id)
    }

    @ApiOperation(
            value = "Query Alleles",
            nickname = "allele/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Allele.class
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
            @ApiImplicitParam(name = "name", paramType = "query", required = false, value = "Allele name", dataType = "string"),
            @ApiImplicitParam(name = "type", paramType = "query", required = false, value = "Allele type", dataType = "string"),
            @ApiImplicitParam(name = "primary_id", paramType = "query", required = false, value = "Primary Identifier", dataType = "string"),
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
        Map<String, Object> queryResults = alleleService.query(params)
        log.info('results allele count =' + queryResults.alleleCount)
        render(view: 'index', model: [alleleList: queryResults.alleleList, alleleCount: queryResults.alleleCount])
    }
}
