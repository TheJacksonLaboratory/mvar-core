package org.jax.mvarcore.parser

class DPParser extends InfoParser {

    def dpMap

    /**
     * ##INFO=<ID=DP,Number=1,Type=Integer,Description="Raw read depth">
     * @param infoString
     */
    DPParser(String infoString) {
        super(infoString)
    }

    @Override
    String getInfoId() {
        return "DP"
    }

    @Override
    Object parse() {
        // split by " 'id'= "
        infos = infoString.split(getInfoId() + "=")
        dpMap = [:]
        if (infos.size() > 1) {
            def val = infos[1].split(';')[0]
            dpMap[getAnnotationKeys().get(0)] = val
        }
    }

    @Override
    List getAnnotationKeys() {
        return ["Raw read depth"]
    }
}
