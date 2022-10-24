package me.huiya.core.Entity;


import me.huiya.core.Encrypt.AES256Util;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;

@Convert
public class AESCryptConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        AES256Util aes256Util = new AES256Util();
        return aes256Util.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        AES256Util aes256Util = new AES256Util();
        return aes256Util.decrypt(dbData);
    }
}

