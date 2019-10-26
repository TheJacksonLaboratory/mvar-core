package org.jax.mvarcore


/**
 * This class is the base class to parse the INFO column from a variant entry.
 */
abstract class InfoParser {

    String infoString
    String[] infoArray

    InfoParser(String infoString) {
        this.infoString = infoString
        if (!this.infoString.startsWith(getInfoId())) {
            throw new IllegalArgumentException("This INFO string does not have the " + getInfoId() + " id.")
        }
        String idStr = getInfoId() + "="
        int startIdx = this.infoString.indexOf(idStr) + idStr.length()
        this.infoString = this.infoString.substring(startIdx)
        infoArray = this.infoString.split("\\|")
    }

    /**
     * Returns the ID for the INFO column. To be implemented in child class.
     * @return
     */
    abstract String getInfoId()

    abstract int getInfoLength()

}
