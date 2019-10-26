package mvar.core


import grails.rest.*
import grails.converters.*
import grails.validation.ValidationException
import org.jax.mvarcore.VcfFileCommand
import org.jax.mvarcore.VcfFileUploadService

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

class VcfFileUploadController {

    VcfFileUploadService vcfFileUploadService

    static responseFormats = ['json', 'xml']



    def index() { }


    /**
     * Controller method to parse and convert vcf file contents into a JSON format
     * @param vcfFileCmd
     * @return JSON
     */
    def upload(VcfFileCommand vcfFileCmd){

        if (vcfFileCmd == null){
            render status: NOT_FOUND
            return
        }

        if (vcfFileCmd.hasErrors()){
            respond(vcfFileCmd.errors, status: BAD_REQUEST)
            return
        }

        String path = grailsApplication.config.getProperty("uploads.vcf")
        //Map<String, List<Map>> serviceResponse = [:]
        try {

            File vcfFile = new File(path + '/' + vcfFileCmd.vcfFile.getOriginalFilename())
            vcfFileCmd.vcfFile.transferTo(vcfFile)
            vcfFileUploadService.loadVCF(vcfFile)

        }catch (ValidationException | Exception e){

            //TODO log error
            println(e.toString())
            render view: '../error', status: BAD_REQUEST, message: e.toString() //, model:[responseMap:[message: "Error parsing VCF file" , status: BAD_REQUEST, error: e.toString()]]
            return
        }

        render status: OK

    }
}
