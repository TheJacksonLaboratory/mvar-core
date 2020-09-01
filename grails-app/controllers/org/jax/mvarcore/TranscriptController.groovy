package org.jax.mvarcore

import grails.validation.ValidationException
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static org.springframework.http.HttpStatus.*

@Api(value = "/api/v1", tags = ["transcript"], description = "Transcript Api's")
class TranscriptController {

    static namespace = "v1"

    TranscriptService transcriptService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    @ApiOperation(
            value = "Search Transcripts",
            nickname = "transcript",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Transcript.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "primary_id", paramType = "query", required = false, value = "Transcript Primary Identifier", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        if (params.primary_id) {
            def count = Gene.countByNameLike('%'+ params.primary_id +'%')
            println("gene count = " + count)
            List<Transcript> transcripts =  Transcript.findAllByPrimaryIdentifierLike('%'+ params.primary_id +'%', [max: 10])
            //respond transcript
            render (view:'index', model:[transcriptList: transcripts, transcriptCount: count])
            return
        }
        render (view:'index', model:[geneList: transcriptService.list(params), geneCount: transcriptService.count()])

        //respond transcriptService.list(params), model:[transcriptCount: transcriptService.count()]
    }

    @ApiOperation(
            value = "List Transcripts",
            nickname = "transcript/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Transcript.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Transcript Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond transcriptService.get(id)
    }

    @ApiOperation(
            value = "Query Transcripts",
            nickname = "transcript/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Transcript.class
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
            @ApiImplicitParam(name = "primary_id", paramType = "query", required = false, value = "Transcript Primary Identifier", dataType = "string"),
            @ApiImplicitParam(name = "chr", paramType = "query", required = false, value = "Transcript chromosome", dataType = "string"),
            @ApiImplicitParam(name = "mgi_gene_id", paramType = "query", required = false, value = "Transcript MGI Gene Id", dataType = "string"),
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
        Map<String, Object> queryResults = transcriptService.query(params)
        log.info('results transcript count =' + queryResults.transcriptCount)
        render(view: 'index', model: [transcriptList: queryResults.transcriptList, transcriptCount: queryResults.transcriptCount])
    }

    def save(Transcript transcript) {
        if (transcript == null) {
            render status: NOT_FOUND
            return
        }

        try {
            transcriptService.save(transcript)
        } catch (ValidationException e) {
            respond transcript.errors, view:'create'
            return
        }

        respond transcript, [status: CREATED, view:"show"]
    }

    def update(Transcript transcript) {
        if (transcript == null) {
            render status: NOT_FOUND
            return
        }

        try {
            transcriptService.save(transcript)
        } catch (ValidationException e) {
            respond transcript.errors, view:'edit'
            return
        }

        respond transcript, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        transcriptService.delete(id)

        render status: NO_CONTENT
    }
}
