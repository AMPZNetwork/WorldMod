package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.worldmod.api.model.mini.OwnedByParty;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import org.comroid.api.attr.Named;

public interface Group extends OwnedByParty, Prioritized, FlagContainer, Named {
}
