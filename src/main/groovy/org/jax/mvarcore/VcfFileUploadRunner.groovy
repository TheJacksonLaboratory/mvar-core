package org.jax.mvarcore

import groovy.io.FileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner

/**
 * Class that will run after the application has been started.
 * Takes the first program argument provided by the application.
 * A folder absolute path is expected. Then the folder is recursively searched
 * and a list of .gz and.vcf files is populated and parsed to be inserted in the DB.
 */
class VcfFileUploadRunner implements CommandLineRunner {
    @Autowired
    VcfFileUploadService vcfFileUploadService

    @Override
    void run(String...args) throws Exception {
        String folderPath
        if (args != null && args.size() > 0) {
            folderPath = args[0]
        } else {
            return
        }
        List<File> list = []
        File dir = new File(folderPath)
        dir.eachFileRecurse (FileType.FILES) { file ->
            list << file
        }

        list.each { it ->
            String path = it.path
            File file = new File(path)
            if (file.isFile() && (path.endsWith('.gz')||(path.endsWith('.vcf'))))
                vcfFileUploadService.loadVCF(file)
        }
    }

}

