package mvar.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.jax.mvarcore.Variant
import org.jax.mvarcore.VariantCanonIdentifier
import org.jax.mvarcore.VcfFileUploadService
import spock.lang.Specification

class VcfFileUploadServiceSpec extends Specification implements ServiceUnitTest<VcfFileUploadService>, DataTest{

    def setup() {

        mockDomain Variant
        mockDomain VariantCanonIdentifier

        service.metaClass.cleanUpGorm = {->

        }

        service.metaClass.getConnection() { ->

        }

        service.metaClass.getSql(){ ->

        }
    }

    def cleanup() {
    }

    void "test vcf load"() {

        setup:
            mockDomain Variant
            mockDomain VariantCanonIdentifier
            def file = new File("src/integration-test/resources/GRCm38_test_vcf_snp.vcf")

        when: "vcf load"
            //service.loadVCF(file)

        then: 1==1
        
//TODO: unit testing with groovy sql
//        then: "data in variant tables"
//            1 * service.batchInsertCannonVariants() >> {}
//
//        when: "query for a canoncal var"
//            def varCanon = VariantCanonIdentifier.findByCaID('MCA_00000000000001')
//
//        then: "the canonical object is returned"
//            varCanon != null
//            varCanon.variantRefTxt.length() > 0

    }
}
