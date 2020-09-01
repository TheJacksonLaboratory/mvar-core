package mvar.core

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        "/allele/$id"(controller: "allele", action: "show", method: "GET")
        "/gene/$id"(controller: "gene", action: "show", method: "GET")
        "/variant/$id"(controller: "variant", action: "show", method: "GET")
        "/variantCanonIdendifier/$id"(controller: "variantCanonIdendifier", action: "show", method: "GET")
        "/transcript/$id"(controller: "transcript", action: "show", method: "GET")
        "/strain/$id"(controller: "strain", action: "show", method: "GET")

        "/allele/query" (controller: 'allele', method: "GET", action: 'query')
        "/gene/query" (controller: 'gene', method: "GET", action: 'query')
        "/variant/query" (controller: 'variant', method: "GET", action: 'query')
        "/variantCanonIdendifier/query" (controller: 'variantCanonIdendifier', method: "GET", action: 'query')
        "/transcript/query" (controller: 'transcript', method: "GET", action: 'query')
        "/strain/query" (controller: 'strain', method: "GET", action: 'query')

//        "variantCanonIdentifier/query" (controller: 'variantCanonIdentifier', method: "GET", action: 'query')
//        "strain/query" (controller: 'strain', method: "GET", action: 'query')
//        "transcript/query" (controller: 'transcript', method: "GET", action: 'query')
//        "gene/query" (controller: 'gene', method: "GET", action: 'query')
        "/"(controller: 'application', action:'index')
        "/vcf/upload/" (controller: 'VcfFileUpload', method:"POST", action:'upload')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
