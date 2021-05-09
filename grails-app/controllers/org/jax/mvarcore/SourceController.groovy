package org.jax.mvarcore


import grails.rest.*
import grails.converters.*
import grails.validation.ValidationException
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

@Api(value = "/api/v1", tags = ["source"], description = "Source Api's")
class SourceController {

    static namespace = "v1"

    SourceService sourceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]


    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond sourceService.list(params), model:[sourceCount: sourceService.count()]
    }

    @ApiOperation(
            value = "List Sources",
            nickname = "source/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Source.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Source Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond sourceService.get(id)
    }

    @ApiOperation(
            value = "Query Sources",
            nickname = "source/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Source.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "sortBy", paramType = "query", required = false, value = "Sorting condition", dataType = "string"),
            @ApiImplicitParam(name = "sortDirection", paramType = "query", required = false, value = "asc or desc", dataType = "string"),
            @ApiImplicitParam(name = "name", paramType = "query", required = false, value = "Source name", dataType = "string"),
            @ApiImplicitParam(name = "sourceVersion", paramType = "query", required = false, value = "Source version", dataType = "string"),
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
        Map<String, Object> queryResults = sourceService.query(params)
        log.info('results source count =' + queryResults.sourceCount)
        render(view: 'index', model: [sourceList: queryResults.sourceList, sourceCount: queryResults.sourceCount])
    }

    def save(Source source) {
        if (source == null) {
            render status: NOT_FOUND
            return
        }

        try {
            sourceService.save(source)
        } catch (ValidationException e) {
            respond source.errors, view:'create'
            return
        }
        respond source, [status: CREATED, view:"show"]
    }

    def update(Source source) {
        if (source == null) {
            render status: NOT_FOUND
            return
        }
        try {
            sourceService.save(source)
        } catch (ValidationException e) {
            respond source.errors, view:'edit'
            return
        }
        respond source, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }
        sourceService.delete(id)
        render status: NO_CONTENT
    }
}
