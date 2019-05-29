package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import gngs.VCF
import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.Sql
import org.apache.commons.lang.time.StopWatch
import org.apache.commons.validator.Var
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.hibernate.Transaction
import org.hibernate.internal.SessionImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.InvalidDataAccessApiUsageException

import java.sql.Connection
import java.sql.PreparedStatement

//import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

@Transactional
class VcfFileUploadService {

    def sessionFactory

    def serviceMethod() {

    }


    public void loadVCF(File vcfFile){

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        //1. parse the vcf -- by chromosome ,
        //2. log
        //3. annotate with jannovar outputs
        //4. persist domain objects
        //5. log
        //6. construct search doc
        //7. log

        String vcfFileName = vcfFile.getName()
        String assembly = vcfFileName.substring(0, vcfFileName.indexOf("_"))

        if (! isAcceptedAssembly(assembly)){
            //Invalid file name. Expecting assembly as the first part of the file name
            return
        }

        Map<String, List<Map>> vcfVariants = parseVcf(vcfFile, assembly)

        println("variant size= " +  vcfVariants.variants.size())

        int batchSize = 100
        int currCount = 0


        //StatelessSession session = sessionFactory.openStatelessSession()
        //Transaction tx = session.beginTransaction()
        def varList = []
        vcfVariants.variants.each { var ->
            println("variant in= " + var)
            def varIn = var.variant

            //get or crate cannonical Identifier
            //VariantCanonIdentifier vca = null
            //if it is a parent variant and the canonical already exist then avoid reinserting the record
//            if (varIn.isParentVariant) {
//                vca = VariantCanonIdentifier.findByVariantRefTxt(varIn.parentVariantRef)
//                if (vca) {
//                    println("Existing cannonical ID = " + vca.caID)
//                    return
//                }
//            }

            //batch inserts
            varList.add(varIn)




//            if (!vca) {
//                vca = getCanonicalIdentifier(varIn)
//            }

//            Variant variant = new Variant(chr: varIn.chr, position: varIn.pos, alt: varIn.alt,
//                    ref: varIn.ref, type: varIn.type, assembly: varIn.assembly, parentRefInd: varIn.isParentVariant,
//                    parentVariantRefTxt: varIn.parentVariantRef, canonVarIdentifier: vca)

//            if (varIn.ID && varIn.ID.size() > 1) {
//                println('WITH ID = ' + varIn.ID)
//                //variant.setIndentifiers(new Indentifier(externalId: varIn.ID, externalSource: Source.findBySourceName('dbSNP')))
//                variant.addToIndentifiers(new Indentifier(externalId: varIn.ID, externalSource: Source.findBySourceName('dbSNP')))
//            }

           //println("variant saving= " + variant.properties)
//            insertVariants(varIn)



//            session.insert(vca)
//            session.insert(variant)


            //currCount++
            //variant.save()

//            if (currCount > 0 && currCount % batchSize == 0) {
//
//                println("CLEARING SESSION")
//
//                cleanUpGorm()
//
//            }

        }

        println("vcf parsing time: ${stopWatch} time: ${new Date()}")
        stopWatch.reset()
        stopWatch.start()
        ///batch inserts:
        insertCanonVariantsBatch3(varList)


        println("vcf Load duration: ${stopWatch} time: ${new Date()}")
        log.info("vcf Load duration: ${stopWatch} time: ${new Date()}")
        //tx.commit()
        //session.close()

    }

    private void insertExternalIdentifiers(Map varIn){


    }

    private void insertVariants(Map varIn){


        String SELECT_LAST_INSERT_ID = 'SELECT LAST_INSERT_ID() as ID'
        String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) VALUES (0, ?)'
        String INSERT_INTO_DB_VARIANT = 'insert into variant (version, chr, position, alt, ref, type, assembly, parent_ref_ind, parent_variant_ref_txt, canon_var_identifier_id) ' +
                'VALUES (0, ?,?,?,?,?,?,?,?,?)' //  + SELECT_CANONICAL_ID + ')'
        String SELECT_EXTERNAL_SOURCE = '(select id from source where source_name = ?)'
        String INSERT_INTO_DB_IDENTIFIER = 'insert into identifier (version, external_source_id, external_id) VALUES (0, ' + SELECT_EXTERNAL_SOURCE + ', ?)'
        String INSERT_INTO_DB_VARIANT_IDENTIFIER = 'insert into variant_identifier (variant_identifier_id, identifier_id) VALUES (?, ?)'

        final Sql sql = getSql()
        //sql.withBatch(500, INSERT_INTO_DB_TERM_DB_GENES) { BatchingPreparedStatementWrapper ps ->

        //canonical id
        sql.execute(INSERT_INTO_DB_VARIANT_CANONICAL_ID, varIn.parentVariantRef)
        String lastCanonVariantID = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID

        //variant
        sql.execute(INSERT_INTO_DB_VARIANT, [varIn.chr, varIn.pos, varIn.alt,
                                             varIn.ref, varIn.type,  varIn.assembly, varIn.isParentVariant,
                                             varIn.parentVariantRef, lastCanonVariantID]) //, varIn.parentVariantRef])
        String lastVariantID = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID

        if (varIn.ID && varIn.ID.size() > 1) {

            //identifier
            sql.execute(INSERT_INTO_DB_IDENTIFIER, ['dbSNP', varIn.ID])
            String lastIdentifierId = sql.rows(SELECT_LAST_INSERT_ID).get(0).ID

            //variant_identifier relation
            sql.execute(INSERT_INTO_DB_VARIANT_IDENTIFIER, [lastVariantID, lastIdentifierId])
        }
        //sql.commit()
        //sql.close()

    }

    private insertCanonVariantsBatch(List<Map> varList){


        //String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) SELECT 0, ? from dual WHERE NOT EXISTS ( SELECT 1 FROM variant_canon_identifier WHERE variant_ref_txt = ? )'
        //String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) VALUES (0, ?)'
        String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) VALUES (0, ?, ?, ?, ?, ?)'
        final Sql sql = getSql()
        sql.withBatch(500, INSERT_INTO_DB_VARIANT_CANONICAL_ID) { BatchingPreparedStatementWrapper ps ->
            varList.each { var ->
                if (!VariantCanonIdentifier.findByChrAndPositionAndRefAndAlt(var.chr, var.pos, var.ref, var.alt)) {
                    ps.addBatch([
                            var.chr,
                            var.pos,
                            var.ref,
                            var.alt,
                            var.parentVariantRef
                    ])
                }
            }
            cleanUpGorm()
        }

    }

    private insertCanonVariantsBatch2(List<Map> varList){


        //String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) SELECT 0, ? from dual WHERE NOT EXISTS ( SELECT 1 FROM variant_canon_identifier WHERE variant_ref_txt = ? )'
        //String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) VALUES (0, ?)'
        String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) VALUES (0, ?, ?, ?, ?, ?)'


        List<String> batchOfParentVariantRef = varList.collect{
            it.parentVariantRef
        }

        println ("batch of parents size = " + batchOfParentVariantRef.size())

        def foundRecs = VariantCanonIdentifier.findAllByVariantRefTxtInList(batchOfParentVariantRef)

        println ("batch of found size = " + foundRecs.size())

        List<String> found = foundRecs.collect{
            it.variantRefTxt
        }

        final Sql sql = getSql()
        sql.withBatch(500, INSERT_INTO_DB_VARIANT_CANONICAL_ID) { BatchingPreparedStatementWrapper ps ->

            varList.eachWithIndex { var, idx ->
                if (found.find{ it == var.parentVariantRef} ) {
                    println(idx + " Existing record ID = " + var.parentVariantRef)

                }else{
                        ps.addBatch([
                                var.chr,
                                var.pos,
                                var.ref,
                                var.alt,
                                var.parentVariantRef
                        ])
                }
            }

            cleanUpGorm()
        }

    }

    private insertCanonVariantsBatch3(List<Map> varList){


        //String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) SELECT 0, ? from dual WHERE NOT EXISTS ( SELECT 1 FROM variant_canon_identifier WHERE variant_ref_txt = ? )'
        //String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, variant_ref_txt) VALUES (0, ?)'
        String INSERT_INTO_DB_VARIANT_CANONICAL_ID = 'insert into variant_canon_identifier (version, chr, position, ref, alt, variant_ref_txt) VALUES (0, ?, ?, ?, ?, ?)'
        String UPDATE_CANONICAL_ID = 'update variant_canon_identifier set caid = concat(\'MCA_\', lpad(id, 14, 0)) where caid is NULL'


        List<Map> batchOfVars = []
        List<String> batchOfParentVariantRef = []
        int batchSize = 500

        varList.eachWithIndex { var, idx ->

            batchOfVars.add(var)
            batchOfParentVariantRef.add(var.parentVariantRef)

            if (idx > 1 && idx % batchSize == 0){
                println ("batch of parents size = " + batchOfParentVariantRef.size())

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
    }


    private batchInsertCannonVariants(List<Map> batchOfVars,  List<String> batchOfParentVariantRef){

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


    private Sql getSql() {
        new Sql(getConnection())
    }

    /**
     * @return a Connection with the underlying connection for the active session
     */
    private Connection getConnection() {
        SessionImpl sessionImpl = sessionFactory.currentSession as SessionImpl
        sessionImpl.connection()
    }


    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }


    private VariantCanonIdentifier getCanonicalIdentifier(Map variant) {

        VariantCanonIdentifier canonVarIdentifier
        //find parent ref

        if (! variant.isParentVariant){

            canonVarIdentifier = VariantCanonIdentifier.findByVariantRefTxt (variant.parentVariantRef)
            if (! canonVarIdentifier){

                //if there is not an existing parent canonical identifier, then create one with the liftover data
                canonVarIdentifier = new VariantCanonIdentifier(variantRefTxt: variant.parentVariantRef)
                //canonVarIdentifier.save(flush: true, failOnError:true)
            }
        } else{

            canonVarIdentifier = new VariantCanonIdentifier(variantRefTxt: variant.parentVariantRef)
            //canonVarIdentifier.save(flush: true, failOnError:true)
        }

        println("CANON IDENTIFIER: " + canonVarIdentifier.toString())
        return canonVarIdentifier
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

    public Map<String, List<Map>> parseVcf(File vcfFile, String assembly) {

        //Parsing of liftover files should be ONLY for liftover to GRCm38
        //GRCm38 vcf files should have start with GRCm38

        boolean refAssembly = isRefAssembly(assembly) ?: false

        Map<String, List<Map>> out = [:]
        List<Map> variantList = []
        try {

            //vcfFileInputStream.line
            def vcf = VCF.parse(vcfFile.getPath()){v->

                v.chr == 'chr1' || v.chr == '1'
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


                //println(vcfVariant.get)

                variantOut.put('variant', variant)
                //variantOut.put('coordinates', jsp.parseText(vcfVariant.toJson())) //TODO might need this info for structural variants
                //variantOut.put('info', vcfVariant.getInfo())

                variantList.add(variantOut)

            }
        }catch (Exception e){

            String error = "Error reading the VCF file " + e.getMessage()
            println(error)
            throw e
        }

        out.put("variants", variantList)
        return out
    }
}
