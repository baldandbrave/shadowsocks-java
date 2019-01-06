package org.shadowsocks.relay;

/**
 * relayStateEnum
 */
public enum relayStateEnum {
    READ_TARGET,
    READ_IPV4,
    READ_DOMAIN,
    READ_IPV6,
    READ_PORT,
    READ_PAYLOAD;
}