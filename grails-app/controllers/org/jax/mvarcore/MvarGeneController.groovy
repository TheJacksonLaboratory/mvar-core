package org.jax.mvarcore

import io.swagger.annotations.*

import static org.springframework.http.HttpStatus.NO_CONTENT

@Api(value = "/api/v1", tags = ["mvargene"])
class MvarGeneController {

    static namespace = "v1"

    MvarGeneService mvarGeneService

    static responseFormats = ['json', 'xml']
//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    @ApiOperation(
            value = "Get all mvar genes symbols",
            nickname = "mvarGene",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = MvarGene.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),
            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        Map<String, Object> genes = mvarGeneService.getAllMvarGenes()
        render (view:'index', model:[mvarGeneList: genes.mvarGeneList, mvarGeneCount: genes.mvarGeneCount])
    }

    @ApiOperation(
            value = "List mvar Genes symbol",
            nickname = "mvarGene/id",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = MvarGene.class
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
        respond mvarGeneService.get(id)
    }

}
