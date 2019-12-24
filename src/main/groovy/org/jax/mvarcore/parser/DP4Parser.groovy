package org.jax.mvarcore.parser

class DP4Parser extends InfoParser {

    def dp4Map

    /**
     * ##INFO=<ID=DP4,Number=4,Type=Integer,Description="Total Number of high-quality ref-fwd, ref-reverse, alt-fwd and alt-reverse bases">
     * @param infoString
     */
    DP4Parser(String infoString) {
        super(infoString)
    }

    @Override
    String getInfoId() {
        return "DP4"
    }

    @Override
    Object parse() {
        // split by " 'id'= "
        infos = infoString.split(getInfoId() + "=")
        dp4Map = [:]
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
    }

    @Override
    List getAnnotationKeys() {
        return ["ref-fwd", "ref-reverse", "alt-fwd", "alt-reverse"]
    }
}
