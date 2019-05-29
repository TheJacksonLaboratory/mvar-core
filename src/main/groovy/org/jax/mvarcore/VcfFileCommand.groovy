package org.jax.mvarcore

import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile

class VcfFileCommand implements Validateable {

    MultipartFile vcfFile

    //TODO: Add additional constraints to support OWASP recommendations (https://www.owasp.org/index.php/Unrestricted_File_Upload)
    static constraints = {

        vcfFile validator: { val, obj, errors ->


            if (val == null){
                return false
            }

            if (val.empty){
                return false
            }

            ['vcf'].any { extension ->

                val.originalFilename?.toLowerCase()?.endsWith(extension)

            }
        }
    }
}
