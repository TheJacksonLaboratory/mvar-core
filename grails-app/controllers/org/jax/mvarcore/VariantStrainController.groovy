package org.jax.mvarcore

import io.swagger.annotations.*

import static org.springframework.http.HttpStatus.NO_CONTENT

@Api(value = "/api/v1", tags = ["variantStrain"], description = "Variant Strain Api's")
class VariantStrainController {
    static namespace = "v1"

    VariantStrainService variantStrainService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]


    @ApiOperation(
            value = "Query Variant across strains",
            nickname = "variantStrain/query",
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
            @ApiImplicitParam(name = "gene", paramType = "query", required = false, value = "Gene symbol", dataType = "string"),
            @ApiImplicitParam(name = "chr", paramType = "query", required = false, value = "Chromosome", dataType = "string"),
            @ApiImplicitParam(name = "startPos", paramType = "query", required = false, value = "Start position", dataType = "long"),
            @ApiImplicitParam(name = "endPos", paramType = "query", required = false, value = "End position", dataType = "long"),
            @ApiImplicitParam(name = "type", paramType = "query", required = false, value = "Variant type: can be SNP, INS or DEL", dataType = "string"),
            @ApiImplicitParam(name = "impact", paramType = "query", required = false, value = "Impact", dataType = "string"),
            @ApiImplicitParam(name = "consequence", paramType = "query", required = false, value = "Molecular Consequence", dataType = "string"),
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

        def genesMap = [genes: params.list('gene')]
        def varTypesMap = [types: params.list('type')]
        def impactsMap = [impacts: params.list('impact')]
        def consequencesMap = [consequences:  params.list('consequence')]
        def hgvsMap = [hgvsList:  params.list('hgvs')]
        def mvarIdMap = [mvarIdList: params.list('mvarId')]
        def paramsMap = genesMap + varTypesMap + impactsMap + consequencesMap + hgvsMap + mvarIdMap + params

        Map<String, Object> queryResults = variantStrainService.query(paramsMap)

        log.info('results variant count =' + queryResults.variantCount)

        render(view: 'index', model: [variantList: queryResults.variantList, variantCount: queryResults.variantCount])

    }

    @ApiOperation(
            value = "Find strains with variants in the DB",
            nickname = "variantStrain/strainsInDB",
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
    def strainsInDB() {

        List<Strain> strainsResults = variantStrainService.getDBStrains()

        log.info('Strains with data in DB =' + strainsResults.size())

        render(view: 'getStrains', model: [strainList: strainsResults])

    }
}
