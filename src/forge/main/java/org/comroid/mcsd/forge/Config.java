package org.comroid.mcsd.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.comroid.api.Polyfill;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MCSD_Forge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<String> TargetId = BUILDER.comment("The agents target ID").define("target", "<mcsd target id>");
    public static final ForgeConfigSpec.ConfigValue<String> AgentId = BUILDER.comment("The agents ID").define("agent", "<mcsd agent id>");
    public static final ForgeConfigSpec.ConfigValue<String> Token = BUILDER.comment("The agents token").define("token", "<mcsd agent token>");
    public static final ForgeConfigSpec.ConfigValue<String> BaseUrl = BUILDER.comment("The agents base url").define("baseUrl", "<mcsd agent base url>");
    public static final ForgeConfigSpec.ConfigValue<String> HubBaseUrl = BUILDER.comment("The hub base url").define("hubBaseUrl", "https://mc.comroid.org");

    static final ForgeConfigSpec SPEC = BUILDER.build();

    //static AgentInfo info;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        var target= UUID.fromString(TargetId.get());
        var agent=UUID.fromString(AgentId.get());
        var hubBaseUrl=Token.get();
        var token=BaseUrl.get();
        var baseUrl=HubBaseUrl.get();
        //info=new AgentInfo(target,agent,hubBaseUrl,token,baseUrl);
    }
}
