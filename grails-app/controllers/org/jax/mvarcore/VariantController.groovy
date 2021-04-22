package org.jax.mvarcore

import grails.core.support.GrailsConfigurationAware
import grails.validation.ValidationException
import io.swagger.annotations.*

import static org.springframework.http.HttpStatus.*

@Api(value = "/api/v1", tags = ["variant"], description = "Variant Api's")
class VariantController implements GrailsConfigurationAware {

    static namespace = "v1"

    VariantService variantService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond variantService.list(params), model:[variantCount: variantService.count()]
    }

    @ApiOperation(
            value = "List Variants",
            nickname = "variant/id",
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
            @ApiImplicitParam(name = "id", paramType = "path", required = true, value = "Variant Id", dataType = "string"),
            @ApiImplicitParam(name = "applicationType", paramType = "header", required = true, defaultValue = "web", value = "Application Types", dataType = "string"),
            @ApiImplicitParam(name = "Accept-Language", paramType = "header", required = true, defaultValue = "en", value = "Accept-Language", dataType = "string")
    ])
    def show(Long id) {
        respond variantService.get(id)
    }

    @ApiOperation(
            value = "Query Variants",
            nickname = "variant/query",
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
            @ApiImplicitParam(name = "max", paramType = "query", required = false, value = "Max number of results", dataType = "integer"),
            @ApiImplicitParam(name = "offset", paramType = "query", required = false, value = "Offset value", dataType = "long"),
            @ApiImplicitParam(name = "sortBy", paramType = "query", required = false, value = "Sorting condition", dataType = "string"),
            @ApiImplicitParam(name = "sortDirection", paramType = "query", required = false, value = "asc or desc", dataType = "string"),
            @ApiImplicitParam(name = "gene", paramType = "query", required = false, value = "Gene symbol", dataType = "string"),
            @ApiImplicitParam(name = "strain", paramType = "query", required = false, value = "Strain name", dataType = "string"),
            @ApiImplicitParam(name = "variantRefTxt", paramType = "query", required = false, value = "Variant canonical Id", dataType = "string"),
            @ApiImplicitParam(name = "caid", paramType = "query", required = false, value = "MVAR id: MCA_*", dataType = "string"),
            @ApiImplicitParam(name = "type", paramType = "query", required = false, value = "Variant type: can be SNP, INS or DEL", dataType = "string"),
            @ApiImplicitParam(name = "annotation", paramType = "query", required = false, value = "Sequence Ontology name", dataType = "string"),
            @ApiImplicitParam(name = "hgvs", paramType = "query", required = false, value = "HGVS genomic nomenclature"),
            @ApiImplicitParam(name = "startPos", paramType = "query", required = false, value = "Starting position", dataType = "integer"),
            @ApiImplicitParam(name = "endPos", paramType = "query", required = false, value = "Ending position", dataType = "integer"),
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

        Map<String, Object> queryResults = variantService.query(params)

        log.info('results variant count =' + queryResults.variantCount)

        render(view: 'index', model: [variantList: queryResults.variantList, variantCount: queryResults.variantCount])

    }

    def save(Variant variant) {
        if (variant == null) {
            render status: NOT_FOUND
            return
        }

        try {
            variantService.save(variant)
        } catch (ValidationException e) {
            respond variant.errors, view:'create'
            return
        }

        respond variant, [status: CREATED, view:"show"]
    }

    def update(Variant variant) {
        if (variant == null) {
            render status: NOT_FOUND
            return
        }

        try {
            variantService.save(variant)
        } catch (ValidationException e) {
            respond variant.errors, view:'edit'
            return
        }

        respond variant, [status: OK, view:"show"]
    }

    def delete(Long id) {
        if (id == null) {
            render status: NOT_FOUND
            return
        }

        variantService.delete(id)

        render status: NO_CONTENT
    }

    @Override
    void setConfiguration(grails.config.Config co) {

    }
}
