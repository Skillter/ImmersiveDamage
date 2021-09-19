package skillter.immersivedamage.config;

import com.google.common.primitives.Ints;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.example.ExampleConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import skillter.immersivedamage.Reference;

import java.util.concurrent.atomic.AtomicInteger;

@me.shedaniel.autoconfig.annotation.Config(name = Reference.MOD_ID)
public class Config implements ConfigData {


    public boolean enabled = true;

    @Comment("IP on which packets will be sent")
    public String ip = "localhost";

    @Comment("Port on which packets will be sent")
    public int port = 4447;

    @Comment("If a player takes damage of this value or higher,\nthe phone will vibrate at the full strength,\nif he'll take damage of half of this value,\nthe strength of vibration will be 50% etc.")
    @ConfigEntry.BoundedDiscrete(min = 1L, max = 20L)
    public int maxStrengthAtHP = 6; // For example, if set to 8, then when a player takes 8 or more HP of damage, the phone will vibrate at 100% power, when 4 HP, then 50% etc.

    @Comment("Duration of every vibration in miliseconds")
    public int duration = 500;

    // TODO: Make an expandable tab with duration settings: min, max, min starts at, max ends at

    public static ConfigHolder<Config> init() {
        ConfigHolder<Config> holder = AutoConfig.register(Config.class, GsonConfigSerializer::new);

        // Listen for when the server is reloading (i.e. /reload), and reload the config
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((s, m) -> AutoConfig.getConfigHolder(Config.class).load()
        );

        return holder;
    }

}
