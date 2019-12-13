package org.jax.mvarcore


/**
 * This class is the base class to parse the INFO column from a variant entry.
 */
abstract class InfoParser {

    String infoString
    List<String[]> listOfInfoArray

    InfoParser(String infoString) {
        this.infoString = infoString
        if (!this.infoString.startsWith(getInfoId())) {
            throw new IllegalArgumentException("This INFO string does not have the " + getInfoId() + " id.")
        }
        String idStr = getInfoId() + "="
        int startIdx = this.infoString.indexOf(idStr) + idStr.length()
        this.infoString = this.infoString.substring(startIdx)
        // split string by commas: a comma in the jannovar string separates multiple transcripts
        String[] transcripts = this.infoString.split(",")
        this.listOfInfoArray = new ArrayList<String[]>()
        for (int i = 0; i < transcripts.size(); i++) {
            String[] infoTmpArray = transcripts[i].split("\\|")
            this.listOfInfoArray.add(infoTmpArray)
        }
    }

    /**
     * Returns the ID for the INFO column. To be implemented in child class.
     * @return
     */
    abstract String getInfoId()

    abstract int getInfoLength()

}
