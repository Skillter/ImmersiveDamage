package skillter.immersivedamage;

import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import skillter.immersivedamage.command.ImmersiveDamageCommand;
import skillter.immersivedamage.communication.UDPManager;
import skillter.immersivedamage.communication.client.UDPClient;
import skillter.immersivedamage.config.Config;
import skillter.immersivedamage.listener.PlayerFinalDamageListener;
import skillter.immersivedamage.util.EnumChatFormatting;

public class ImmersiveDamage implements ClientModInitializer {


    public static ConfigHolder<Config> config = null;

    public static String prefix = EnumChatFormatting.GOLD + "[ImmersiveDamage] " + EnumChatFormatting.RESET;

    @Override
    public void onInitializeClient() {
        config = Config.init(); // Set up config
        ImmersiveDamageCommand.registerCommands(); // Register all commands
        PlayerFinalDamageListener.registerEvent();
        UDPManager.udpClient = new UDPClient(); // Run UDP Client
    }

}
