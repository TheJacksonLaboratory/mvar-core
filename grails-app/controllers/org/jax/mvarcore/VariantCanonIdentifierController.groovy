package org.jax.mvarcore

import grails.validation.ValidationException
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static org.springframework.http.HttpStatus.*

@Api(value = "/api/v1", tags = ["variantCanonIdentifier"], description = "Variant Canonical Identifier Api's")
class VariantCanonIdentifierController {

    static namespace = "v1"

    VariantCanonIdentifierService variantCanonIdentifierService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond variantCanonIdentifierService.list(params), model:[variantCanonIdentifierCount: variantCanonIdentifierService.count()]
    }

    @ApiOperation(
            value = "List Variant Canonical Identifiers",
            nickname = "variantCanonIdentifier/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Variant.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Variant Canonical Identifiers Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond variantCanonIdentifierService.get(id)
    }

    @ApiOperation(
            value = "Query Variant Canonical Identifiers",
            nickname = "variantCanonIdentifier/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = VariantCanonIdentifier.class
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
            @ApiImplicitParam(name = "variantRefTxt", paramType = "query", required = false, value = "Variant canonical Id", dataType = "string"),
            @ApiImplicitParam(name = "caid", paramType = "query", required = false, value = "MVAR id: MCA_*", dataType = "string"),
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

        Map<String, Object> queryResults = variantCanonIdentifierService.query(params)

        log.info('results variantCanonIdentifier count =' + queryResults.variantCanonIdentifierCount)

        render(view: 'index', model: [variantCanonIdentifierList: queryResults.variantCanonIdentifierList, variantCanonIdentifierCount: queryResults.variantCanonIdentifierCount])

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
