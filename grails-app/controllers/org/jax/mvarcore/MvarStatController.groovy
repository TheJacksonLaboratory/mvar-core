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

@Api(value = "/api/v1", tags = ["mvarStat"])
class MvarStatController {

    static namespace = "v1"

    MvarStatService mvarStatService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [show: "GET", update: "PUT"]

    @ApiOperation(
            value = "Show MvarStat",
            nickname = "mvarStat",
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
    def index() {
        render (view:'index', model:[mvarStatList: mvarStatService.show(), mvarStatCount: 1])
    }

    @ApiOperation(
            value = "Show MvarStat Analysis",
            nickname = "mvarStat/list",
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
        render (view:'index', model:[mvarStatList: mvarStatService.show(), mvarStatCount: 1])
    }
}
