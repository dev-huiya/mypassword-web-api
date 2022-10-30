package me.huiya.core.Encrypt;

import java.security.MessageDigest;

public class SHA256Util {

    public enum Type {
        HEX,
        CHAR
    }

    public static String encrypt(String plainText) {
        return _encrypt(plainText, Type.HEX);
    }

    public static String encrypt(String plainText, Type type) {
        return _encrypt(plainText, type);
    }

    private static String _encrypt(String plainText, Type type) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(plainText.getBytes());
            byte[] byteData = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            // type == CHAR
            if(type.equals(Type.CHAR)) {
                return new String(byteData);
            }

            // default action to HEX
            return toHex(byteData);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<bytes.length;i++) {
            String hex=Integer.toHexString(0xff & bytes[i]);
            if(hex.length()==1){
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

}