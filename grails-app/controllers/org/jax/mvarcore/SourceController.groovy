package org.jax.mvarcore

import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses


@Api(value = "/api/v1", tags = ["source"], description = "Source Api's")
class SourceController {

    static namespace = "v1"

    SourceService sourceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]


    @ApiOperation(
            value = "Returns all sources",
            nickname = "source",
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
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def index() {

        Map<String, Object> queryResults = sourceService.show()
        log.info('results source count =' + queryResults.sourceCount)
        render(view: 'index', model: [sourceList: queryResults.sourceList, sourceCount: queryResults.sourceCount])
    }

    @ApiOperation(
            value = "Get all mvar sources",
            nickname = "source/mvar",
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
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def mvar() {
        Map<String, Object> queryResults = sourceService.mvarSource()
        log.info('results source count =' + queryResults.sourceCount)
        render(view: 'index', model: [sourceList: queryResults.sourceList, sourceCount: queryResults.sourceCount])
    }

}
