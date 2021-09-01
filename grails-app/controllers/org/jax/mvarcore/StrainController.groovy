package org.jax.mvarcore

import grails.validation.ValidationException
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static org.springframework.http.HttpStatus.*

@Api(value = "/api/v1", tags = ["strain"], description = "Strain Api's")
class StrainController {

    static namespace = "v1"

    StrainService strainService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    @ApiOperation(
            value = "Search Strains",
            nickname = "strain",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Strain.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "name", paramType = "query", required = false, value = "Strain name", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def index() {
        params.max = Math.min(params.max ?: 10, 100)

        if (params.name){

            def count = 0
            List<Strain> strains = []

            count = Strain.countByNameLike('%'+ params.name +'%')
            log.info("strain count = " + count)
            strains =  Strain.findAllByNameLike('%'+ params.name +'%', [max: 10])

            render(view: 'index', model: [strainList: strains, strainCount: count])
            return

        }

        render (view:'index', model:[strainList: strainService.list(params), strainCount: strainService.count()])

    }

    @ApiOperation(
            value = "List Strains",
            nickname = "strain/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Strain.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Strain Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond strainService.get(id)
    }

    @ApiOperation(
            value = "Query Strains",
            nickname = "strain/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "POST",
            response = Strain.class
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
            @ApiImplicitParam(name = "name", paramType = "query", required = false, value = "Strain name", dataType = "string"),
            @ApiImplicitParam(name = "attributes", paramType = "query", required = false, value = "Strain attributes: 'inbred strain' for instance", dataType = "string"),
            @ApiImplicitParam(name = "primary_id", paramType = "query", required = false, value = "Strain Primary Identifier", dataType = "string"),
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
        Map<String, Object> queryResults = strainService.query(params)
        log.info('results strain count =' + queryResults.strainCount)
        render(view: 'index', model: [strainList: queryResults.strainList, strainCount: queryResults.strainCount])
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
