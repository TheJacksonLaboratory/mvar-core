package org.jax.mvarcore


class SynonymController {
    SynonymService synonymService

    static responseFormats = ['json', 'xml']

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond synonymService.list(params), model:[synonymCount: synonymService.count()]
    }

    def show(Long id) {
        respond synonymService.get(id)
    }
}
