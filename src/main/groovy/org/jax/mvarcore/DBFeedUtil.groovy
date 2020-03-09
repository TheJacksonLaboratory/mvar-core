package org.jax.mvarcore;


class DBFeedUtil {

   LoadService loadService

    /**
     * Initializer for data seeding
     */
    void init(){
        loadGeneReferenceData()
    }

    /**
     * Load gene reference data
     */
    private void loadGeneReferenceData(){
        loadService.loadData()
    }

}
