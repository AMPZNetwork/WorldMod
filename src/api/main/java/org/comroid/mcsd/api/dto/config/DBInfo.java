package org.comroid.mcsd.api.dto.config;

import lombok.Value;

@Value
public class DBInfo {
    String url;
    String username;
    String password;
}
