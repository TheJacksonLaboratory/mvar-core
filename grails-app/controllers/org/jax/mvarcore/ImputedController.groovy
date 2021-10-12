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

@Api(value = "/api/v1", tags = ["imputed"])
class ImputedController {

    static namespace = "v1"

    ImputedService imputedService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [show: "GET", update: "PUT"]

    @ApiOperation(
            value = "Show Imputed",
            nickname = "imputed",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Imputed.class
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
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render (view:'index', model:[imputedList: imputedService.list(params), imputedCount: imputedService.count()])
    }

    @ApiOperation(
            value = "Show Imputed",
            nickname = "imputed/list",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Imputed.class
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
        render (view:'index', model:[imputedList: imputedService.show(), imputedCount: 1])
    }
}
