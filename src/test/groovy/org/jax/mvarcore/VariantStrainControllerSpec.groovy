package org.jax.mvarcore

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK

class VariantStrainControllerSpec extends Specification implements ControllerUnitTest<VariantStrainController> {

    def setup() {

    }

    def cleanup() {
    }

    void "Test query variant strain"() {
        given:
            controller.variantStrainService = Mock(VariantStrainService) {
                1 * query(_) >> [variantList:[
                        [id: 1, chr:'1', position: '10000', ref:'A', alt:'C',  variantStrain:[variantId:1, strainId:10]],
                        [id: 2, chr:'1', position: '20000', ref:'T', alt:'C',  variantStrain:[variantId:2, strainId:20]]
                ],
                variantCount: 2]

            }

        when:"The query action is executed"
            controller.query()

        then:"The response is correct"
            response.status == OK.value()
            model.variantList
            model.variantList.size() == 2
            model.variantList.find { it.id == 2}
            model.variantList.find { it.position == '20000'}
            model.variantList.find { it.variantStrain.strainId == 20}
    }

    void "Test get strains"() {
        given:

            List<Strain> strains = [[id: 36, strain: "129P2/OlaHsd"],
                                    [id: 2, strain: "129S1/SvImJ"]]

            controller.variantStrainService = Mock(VariantStrainService) {
                1 * getDBStrains() >> strains
            }

        when:"The strainsInDB action is executed"
            controller.strainsInDB()

        then:"The response is correct"
            response.status == OK.value()
            model.strainList
            model.strainList.size() == 2
            model.strainList.find { it.id == 36}
            model.strainList.find { it.id == 2}
            model.strainList.find { it.strain == '129P2/OlaHsd'}
    }

}

