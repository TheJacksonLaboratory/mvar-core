package org.jax.mvarcore

import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

import static org.springframework.http.HttpStatus.NO_CONTENT

@Api(value = "/api/v1", tags = ["mvarStrain"])
class MvarStrainController {

    static namespace = "v1"

    MvarStrainService mvarStrainService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [show: "GET", update: "PUT"]

    @ApiOperation(
            value = "Show MvarStrain",
            nickname = "mvarStrain",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = MvarStrain.class
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
        render (view:'index', model:[mvarStrainList: mvarStrainService.show(), mvarStrainCount: 10])
    }

    @ApiOperation(
            value = "list MvarStrain",
            nickname = "mvarStrain/list",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = MvarStat.class
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
    def show() {
        render (view:'index', model:[mvarStrainList: mvarStrainService.show(), mvarStrainCount: 1])
    }

    @ApiOperation(
            value = "Query MvarStrain",
            nickname = "mvarStrain/query",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = MvarStrain.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "imputed", paramType = "query", required = false, value = "Imputed value (can be 0, 1, 2, as defined in 'imputed' endpoint", dataType = "integer"),
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
        println("params =" + params)
        Map<String, Object> queryResults = mvarStrainService.query(params)
        log.info('results mvarStrain count =' + queryResults.mvarStrainCount)
        render(view: 'index', model: [mvarStrainList: queryResults.mvarStrainList, mvarStrainCount: queryResults.mvarStrainCount])
    }
}
