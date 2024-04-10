package org.comroid.mcsd.api.dto.mc;

import lombok.Data;

import java.util.UUID;

@Data
public class WhitelistEntry {
    private UUID uuid;
    private String name;
}
