import org.jax.mvarcore.DBFeedUtil
import org.jax.mvarcore.VcfFileUploadRunner

// Place your Spring DSL code here
beans = {
    dbFeedUtil(DBFeedUtil){ bean ->

        bean.autowire = 'byName'
        loadService = ref ('loadService')
    }
    // Register vcfFileUploadRunner as spring bean
    vcfFileUploadRunner(VcfFileUploadRunner)
}
