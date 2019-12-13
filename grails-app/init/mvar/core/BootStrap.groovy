package mvar.core

import org.jax.mvarcore.DBFeedUtil

class BootStrap {

    //Util bean to seed the database (with Gene and Strain data from MouseMine)
    DBFeedUtil dbFeedUtil

    def init = { servletContext ->
        dbFeedUtil.init()
    }
    def destroy = {
    }
}
