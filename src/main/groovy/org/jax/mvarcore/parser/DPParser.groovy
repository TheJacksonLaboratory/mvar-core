package org.jax.mvarcore.parser

/**
 * ##INFO=<ID=DP,Number=1,Type=Integer,Description="Raw read depth">
 */
class DPParser extends InfoParser {

    @Override
    String getInfoId() {
        return "DP"
    }

    @Override
    Object parse(String infoString) {
        // split by " 'id'= "
        infos = infoString.split(getInfoId() + "=")
        def dpMap = [:]
        if (infos.size() > 1) {
            def val = infos[1].split(';')[0]
            dpMap[getAnnotationKeys().get(0)] = val
        }
        return dpMap
    }

    @Override
    List getAnnotationKeys() {
        return ["Raw read depth"]
    }
}
