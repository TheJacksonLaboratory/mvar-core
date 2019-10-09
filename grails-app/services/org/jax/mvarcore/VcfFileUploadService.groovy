package org.jax.mvarcore

import gngs.VCF
import grails.gorm.transactions.Transactional
import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.Sql
import org.apache.commons.lang.time.StopWatch
import org.hibernate.internal.SessionImpl
import org.springframework.dao.InvalidDataAccessApiUsageException

import java.sql.Connection

//import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

@Transactional
class VcfFileUploadService {

    def sessionFactory


    public void loadVCF(File vcfFile){

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()


        String vcfFileName = vcfFile.getName()
        String assembly = vcfFileName.substring(0, vcfFileName.indexOf("_"))

        if (! isAcceptedAssembly(assembly)){
            //Invalid file name. Expecting assembly as the first part of the file name
            return
        }

        println("vcf File: " + vcfFileName)
        try {
            persistData(vcfFile)
        }catch (Exception e){
            e.printStackTrace()
        }

        println("vcf File: " + vcfFileName)
        println("Vcf file complete parsing and persistance: ${stopWatch} time: ${new Date()}")
    }

    private persistData (File vcfFile){

        /*
            1. parse the vcf -- by chromosome ,
            2. Persist canonicals
            3. persist variants
            4. persist variant associations
                    - canonical
                    - strain
                    - gene
                    - jannovar data
                    - external ids
            5. construct search doc -- TODO: possible search docs for speed querying of data in site
        */


        String vcfFileName = vcfFile.getName()
        String assembly = vcfFileName.substring(0, vcfFileName.indexOf("_"))

        //persist data by chromosome -- TODO: check potential for multi-threaded process
        //TODO: add mouse chr to config
        List<String> mouseChromosomes = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19','X', 'Y', 'MT']

        mouseChromosomes.each {chr ->

            StopWatch stopWatch = new StopWatch()
            stopWatch.start()

            List<Map> vcfVariants = parseVcf(chr, vcfFile, assembly)
            println("CHR = " + chr + ', variant size= ' + vcfVariants.size())

            ///batch inserts:
            insertCanonVariantsBatch(vcfVariants)
            insertVariantsBatch(vcfVariants)
            //TODO Strain, gene, external id associations, and jannovar data


            println("Chr= " + chr + " : persistance load = ${stopWatch} time: ${new Date()}")
            stopWatch.reset()
            stopWatch.start()

        }

    }

    private void insertExternalIdentifiers(Map varIn){


    }

    // TODO: leaving this commented out for now. some concepts here are still useful
//    private void insertVariants(Map varIn){
//
//
//        String SELECT_LAST_INSERT_ID = 'SELECT LAST_INSERT_ID() as ID'
//        String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) VALUES (0, ?)'
//        String INSERT_INTO_DB_VARIANT = 'insert into variant (version, chr, position, alt, ref, type, assembly, parent_ref_ind, parent_variant_ref_txt, canon_var_identifier_id) ' +
//                'VALUES (0, ?,?,?,?,?,?,?,?,?)' //  + SELECT_CANONICAL_ID + ')'
//        String SELECT_EXTERNAL_SOURCE = '(select id from source where source_name = ?)'
//        String INSERT_INTO_DB_IDENTIFIER = 'insert into identifier (version, external_source_id, external_id) VALUES (0, ' + SELECT_EXTERNAL_SOURCE + ', ?)'
//        String INSERT_INTO_DB_VARIANT_IDENTIFIER = 'insert into variant_identifier (variant_identifier_id, identifier_id) VALUES (?, ?)'
//
//        final Sql sql = getSql()
//        //sql.withBatch(500, INSERT_INTO_DB_TERM_DB_GENES) { BatchingPreparedStatementWrapper ps ->
//
//        //canonical id
//        sql.execute(INSERT_INTO_DB_VARIANT_CANONICAL_ID, varIn.parentVariantRef)
//        String lastCanonVariantID = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID
//
//        //variant
//        sql.execute(INSERT_INTO_DB_VARIANT, [varIn.chr, varIn.pos, varIn.alt,
//                                             varIn.ref, varIn.type,  varIn.assembly, varIn.isParentVariant,
//                                             varIn.parentVariantRef, lastCanonVariantID]) //, varIn.parentVariantRef])
//        String lastVariantID = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID
//
//        if (varIn.ID && varIn.ID.size() > 1) {
//
//            //identifier
//            sql.execute(INSERT_INTO_DB_IDENTIFIER, ['dbSNP', varIn.ID])
//            String lastIdentifierId = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID
//
//            //variant_identifier relation
//            sql.execute(INSERT_INTO_DB_VARIANT_IDENTIFIER, [lastVariantID, lastIdentifierId])
//        }
//        //sql.commit()
//        //sql.close()
//
//    }



    private insertVariantsBatch(List<Map> varList){

        List<Map> batchOfVars = []
        List<String> batchOfParentVariantRefTxt = []
        List<String> batchOfVariantRefTxt = []
        int batchSize = 500

        varList.eachWithIndex { var, idx ->

            batchOfVars.add(var)
            batchOfParentVariantRefTxt.add(var.parentVariantRef)
            batchOfVariantRefTxt.add(var.variantRefTxt)

            if (idx > 1 && idx % batchSize == 0){

                batchInsertVariants3(batchOfVars, batchOfVariantRefTxt, batchOfParentVariantRefTxt)

                //clear batch lists
                batchOfVars.clear()
                batchOfParentVariantRefTxt.clear()
                batchOfVariantRefTxt.clear()
                cleanUpGorm()
            }
        }

        //last batch
        if (batchOfVars.size() > 0){
            batchInsertVariants3(batchOfVars, batchOfVariantRefTxt, batchOfParentVariantRefTxt)
        }

    }


    private batchInsertVariants3(List<Map> batchOfVars,  List<String> batchOfariantRefTxt, List<String> batchOfParentVariantRefTxt){

        String INSERT_INTO_DB_VARIANT = 'insert into variant (version, chr, position, alt, ref, type, assembly, parent_ref_ind, parent_variant_ref_txt, variant_ref_txt, canon_var_identifier_id) ' +
                'VALUES (0, ?,?,?,?,?,?,?,?,?,?)' //  + SELECT_CANONICAL_ID + ')'

        int batchSize = 500
        def foundRecs = Variant.findAllByVariantRefTxtInList(batchOfariantRefTxt)
        List<String> found = foundRecs.collect{
            it.variantRefTxt
        }

        def cannonRecs = VariantCanonIdentifier.findAllByVariantRefTxtInList(batchOfParentVariantRefTxt)


        final Sql sql = getSql()
        sql.withBatch(batchSize, INSERT_INTO_DB_VARIANT) { BatchingPreparedStatementWrapper ps ->

            batchOfVars.eachWithIndex { variant, idx2 ->
                if (found.find { it == variant.variantRefTxt }) {
                    println(idx2 + " Existing record ID = " + variant.variantRefTxt)

                } else {

                    //println("var = " + variant)
                    VariantCanonIdentifier canonIdentifier = cannonRecs.find { it.variantRefTxt == variant.parentVariantRef}

                    ps.addBatch([
                            variant.chr,
                            variant.pos,
                            variant.alt,
                            variant.ref,
                            variant.type,
                            variant.assembly,
                            variant.isParentVariant,
                            variant.parentVariantRef,
                            variant.variantRefTxt,
                            canonIdentifier.id
                    ])
                }
            }
        }
    }

    private insertCanonVariantsBatch(List<Map> varList){

        String UPDATE_CANONICAL_ID = 'update variant_canon_identifier set caid = concat(\'MCA_\', lpad(id, 14, 0)) where caid is NULL'


        List<Map> batchOfVars = []
        List<String> batchOfParentVariantRef = []
        int batchSize = 500

        varList.eachWithIndex { var, idx ->

            batchOfVars.add(var)
            batchOfParentVariantRef.add(var.parentVariantRef)

            if (idx > 1 && idx % batchSize == 0){
                batchInsertCannonVariants(batchOfVars, batchOfParentVariantRef)

                //clear batch lists
                batchOfVars.clear()
                batchOfParentVariantRef.clear()
                cleanUpGorm()
            }
        }

        //last batch
        if (batchOfVars.size() > 0){
            batchInsertCannonVariants(batchOfVars, batchOfParentVariantRef)
        }

        // update canonical id
        final Sql sql = getSql()
        sql.execute(UPDATE_CANONICAL_ID)
        //sql.commit()
    }


    protected batchInsertCannonVariants(List<Map> batchOfVars,  List<String> batchOfParentVariantRef){

        String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) VALUES (0, ?, ?, ?, ?, ?)'

        int batchSize = 500
        def foundRecs = VariantCanonIdentifier.findAllByVariantRefTxtInList(batchOfParentVariantRef)
        List<String> found = foundRecs.collect{
            it.variantRefTxt
        }

        final Sql sql = getSql()
        sql.withBatch(batchSize, INSERT_INTO_DB_VARIANT_CANONICAL_ID) { BatchingPreparedStatementWrapper ps ->

            batchOfVars.eachWithIndex { variant, idx2 ->
                if (found.find { it == variant.parentVariantRef }) {
                    println(idx2 + " Existing record ID = " + variant.parentVariantRef)

                } else {
                    //println("indx = " + idx2 + ", " + variant)
                    ps.addBatch([
                            variant.chr,
                            variant.pos,
                            variant.ref,
                            variant.alt,
                            variant.parentVariantRef
                    ])
                }
            }
        }
    }


    protected Sql getSql() {
        new Sql(getConnection())
    }

    /**
     * @return a Connection with the underlying connection for the active session
     */
    protected Connection getConnection() {
        SessionImpl sessionImpl = sessionFactory.currentSession as SessionImpl
        sessionImpl.connection()
    }


    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }


    private boolean isAcceptedAssembly(String inAssembly){

        //TODO define configuration for accepted assemblies
        List<String> assemblies = ['GRCm38', 'NCBI37', 'NCBI36']
        if (assemblies.contains(inAssembly))
            return true
        else
            return false
    }

    private boolean isRefAssembly(String inAssembly){

        //TODO define configuration for reference assembly
        String refAssembly = 'GRCm38'
        if (inAssembly == refAssembly)
            return true
        else
            return false
    }

    private List<Map> parseVcf(String chromosome, File vcfFile, String assembly) {

        //Parsing of liftover files should be ONLY for liftover to GRCm38
        //GRCm38 vcf files should have start with GRCm38

        boolean refAssembly = isRefAssembly(assembly) ?: false

        List<Map> variantList = []
        try {

            //vcfFileInputStream.line
            def vcf = VCF.parse(vcfFile.getPath()){v->

                v.chr == 'chr' + chromosome || v.chr == chromosome
            }
            println("parsed variants = " + vcf.getVariants().size())
            vcf.getVariants().each { vcfVariant ->

                //compile the vcfVariant and vcfVariant info to build a JSON object
                //def jsp = new JsonSlurper()
                Map<String, Object> variantOut = [:]


                Map<String, String> variant = [:]
                variant.put("ID", vcfVariant.getId())
                variant.put("pos", vcfVariant.getPos())
                variant.put("chr", vcfVariant.getChr().replace('ch','').replace('r',''))
                variant.put("ref", vcfVariant.getRef())
                variant.put("alt", vcfVariant.getAlt())
                variant.put("type", vcfVariant.getType())
                variant.assembly = assembly


                ///holds full variation change for the parent reference <chr_pos_ref_alt>  -- ref and alt is empty will have '.' as value
                String refIn = variant.ref ?: '.'
                String altIn = variant.alt ?: '.'
                String parentRefVariant = variant.chr + '_' + variant.pos + '_' + variant.ref + '_' + variant.alt
                variant.put("parentVariantRef", parentRefVariant)


                String orgPos = vcfVariant.getInfo().OriginalStart
                if (orgPos) {

                    variant.pos = vcfVariant.getInfo().OriginalStart
                    variant.isParentVariant = false
                }else if(refAssembly) {

                    variant.isParentVariant = true
                }else{
                    //something is wrong. Either
                    //-liftover variants should have original position  OR
                    //-refAssembly should be GRCm38
                    throw new InvalidDataAccessApiUsageException("Expects GRCm38 file or liftover to GRCm38")
                    return
                }


                if (! vcfVariant.getInfo().OriginalAlleles){
                    // if original alleles properties are missed in liftover then they are the same
                    //TODO check and assign values when original alleles are different in liftover variant
                    variant.put("ref", vcfVariant.getRef())
                    variant.put("alt", vcfVariant.getAlt())
                }

                String varRefTxt = variant.chr + '_' + variant.pos + '_' + variant.ref + '_' + variant.alt
                variant.variantRefTxt = varRefTxt

                //variantOut.put('variant', variant)

                variantList.add(variant)

            }
        }catch (Exception e){

            String error = "Error reading the VCF file " + e.getMessage()
            println(error)
            throw e
        }


        return variantList
    }
}
