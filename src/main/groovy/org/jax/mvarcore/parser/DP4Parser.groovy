package org.jax.mvarcore.parser

/**
 * ##INFO=<ID=DP4,Number=4,Type=Integer,Description="Total Number of high-quality ref-fwd, ref-reverse, alt-fwd and alt-reverse bases">
 */
class DP4Parser extends InfoParser {

    @Override
    String getInfoId() {
        return "DP4"
    }

    @Override
    List<Map> parse(String infoString) {
        // split by " 'id'= "
        infos = infoString.split(getInfoId() + "=")
        def dp4Map = [:]
        if (infos.size() > 1) {
            // Total Number of high-quality ref-fwd, ref-reverse, alt-fwd and alt-reverse bases
            // remove all string after tab
            String cleanDP4 = infos[1].split('\t')[0]
            String[] dp4Info = cleanDP4.split(',')
            if (dp4Info.size() != getInfoLength()) {
                throw new Exception("Expecting " + getInfoId() + " identifier to have " + getInfoLength() + " blocks. Had " + dp4Info.size() + " instead.")
            }
            dp4Map["ref-fwd"] = dp4Info[0]
            dp4Map["ref-reverse"] = dp4Info[1]
            dp4Map["alt-fwd"] = dp4Info[2]
            dp4Map["alt-reverse"] = dp4Info[3]
        }
        return new ArrayList<Map>(dp4Map)
    }

    @Override
    List getAnnotationKeys() {
        return ["ref-fwd", "ref-reverse", "alt-fwd", "alt-reverse"]
    }
}
