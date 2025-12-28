package com.example.fingerprint_api.utils;


import com.digitalpersona.uareu.*;
import java.util.Base64;

public class FingerprintUtils {

    public static Fmd decodeBase64ToFmd(String base64) throws UareUException {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return UareUGlobal.GetImporter().ImportFmd(bytes, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
    }

    public static byte[] fmdToBytes(Fmd fmd) {
        return fmd.getData();
    }
}