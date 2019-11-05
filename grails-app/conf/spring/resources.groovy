import org.jax.mvarcore.DBFeedUtil

// Place your Spring DSL code here
beans = {
    dbFeedUtil(DBFeedUtil){ bean ->

        bean.autowire = 'byName'
        loadService = ref ('loadService')
    }
}
