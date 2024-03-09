package org.jax.mvarcore

class Mm10mm39Temp {
    String refTxtMm10;
    String refTxtMm39;

    static mapping = {
        refTxtMm10 index: true
        refTxtMm39 index: true
        refTxtMm10 sqlType: "varchar(700)"
        refTxtMm39 sqlType: "varchar(700)"
        version false
    }
}