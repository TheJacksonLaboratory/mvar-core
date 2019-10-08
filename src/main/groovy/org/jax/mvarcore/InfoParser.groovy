package org.jax.mvarcore

import javax.naming.directory.InvalidAttributesException

/**
 * This class is the base class to parse the INFO column from a variant entry.
 */
abstract class InfoParser {

    String infoString
    String[] infoArray

    InfoParser(String infoString) {
        this.infoString = infoString
        if (!this.infoString.startsWith(getInfoId())) {
            throw new InvalidAttributesException("This INFO string does not have the " + getInfoId() + " id.")
        }
        String idStr = getInfoId() + "="
        int startIdx = this.infoString.indexOf(idStr) + idStr.length()
        this.infoString = this.infoString.substring(startIdx)
        infoArray = this.infoString.split("\\|")
        if (infoArray.length < getInfoLength()) {
            throw new InvalidAttributesException("This INFO string doesn't have the expected number of attributes : is " + infoArray.length + "; should be " + getInfoLength())
        } else {
            print("Warning, the expected Info length is " + getInfoLength() + " and the input has length :" + infoArray.length)
        }

    }

    /**
     * Returns the ID for the INFO column. To be implemented in child class.
     * @return
     */
    abstract String getInfoId()

    abstract int getInfoLength()
}
