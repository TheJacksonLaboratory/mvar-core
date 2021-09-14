package org.jax.mvarcore


import grails.rest.*
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

@Api(value = "/api/v1", tags = ["sequenceOntology"], description = "Sequence Ontology Api's")
class SequenceOntologyController {

    static namespace = "v1"

    SequenceOntologyService sequenceOntologyService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    @ApiOperation(
            value = "Search Sequence Ontology",
            nickname = "sequenceOntology",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = SequenceOntology.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "label", paramType = "query", required = false, value = "Sequence Ontology name", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def index() {
        params.max = Math.min(params.max ?: 10, 100)

        if (params.name){

            def count = 0
            List<SequenceOntology> sequenceOntologies = []

            count = SequenceOntology.countByLabel(params.name)
            log.info("sequence ontology count = " + count)
            sequenceOntologies =  SequenceOntology.findAllByLabel(params.name, [max: 10])

            render(view: 'index', model: [sequenceOntologyList: sequenceOntologies, sequenceOntologyCount: count])
            return

        }

        render (view:'index', model:[sequenceOntologyList: sequenceOntologyService.list(params), sequenceOntologyCount: sequenceOntologyService.count()])
    }

    @ApiOperation(
            value = "List Sequence Ontology",
            nickname = "sequenceOntology/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = SequenceOntology.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Sequence ontology Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond sequenceOntologyService.get(id)
    }

    @ApiOperation(
            value = "Query Sequence Ontology",
            nickname = "sequenceOntology/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = SequenceOntology.class
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
            @ApiImplicitParam(name = "soId", paramType = "query", required = false, value = "Sequence Ontology id", dataType = "string"),
            @ApiImplicitParam(name = "label", paramType = "query", required = false, value = "Sequence Ontology name", dataType = "string"),
            @ApiImplicitParam(name = "definition", paramType = "query", required = false, value = "Sequence Ontology definition", dataType = "string"),
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
        Map<String, Object> queryResults = sequenceOntologyService.query(params)
        log.info('results sequence ontology count =' + queryResults.sequenceOntologyCount)
        render(view: 'index', model: [sequenceOntologyList: queryResults.sequenceOntologyList, sequenceOntologyCount: queryResults.sequenceOntologyCount])
    }
}
