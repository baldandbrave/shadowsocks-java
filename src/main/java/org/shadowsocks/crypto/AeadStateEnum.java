package org.shadowsocks.crypto;


/**
 * AeadStateEnum
 */
public enum AeadStateEnum {
    READ_SALT,
    READ_LENGTH,
    READ_PAYLOAD,
    READ_PROTOCOL;
}