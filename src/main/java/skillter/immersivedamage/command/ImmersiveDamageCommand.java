package skillter.immersivedamage.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import skillter.immersivedamage.ContactInfo;
import skillter.immersivedamage.ImmersiveDamage;
import skillter.immersivedamage.Reference;
import skillter.immersivedamage.handler.ManagementHandler;
import skillter.immersivedamage.communication.UDPManager;
import skillter.immersivedamage.communication.packet.DamagePacket;
import skillter.immersivedamage.util.EnumChatFormatting;

import static skillter.immersivedamage.ImmersiveDamage.config;

public class ImmersiveDamageCommand {

    private static String baseCommand = "immersivedamage";
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void registerCommands() {
        CommandDispatcher<FabricClientCommandSource> commandDispatcher = ClientCommandManager.DISPATCHER;

        commandDispatcher.register(ClientCommandManager.literal(baseCommand).executes(context -> {
                    mc.player.sendSystemMessage(Text.of(ImmersiveDamage.prefix + credits()), mc.player.getUuid());
                    return 1;
                })
                .then(ClientCommandManager.literal("toggle").executes(context -> {
                    ManagementHandler.toggleMod(true);
                    return 1;
                }))
                .then(ClientCommandManager.literal("ip").executes(context -> {
                    mc.player.sendSystemMessage(Text.of(ImmersiveDamage.prefix + "Change the IP on which packets will be sent \n" +
                            ImmersiveDamage.prefix + "eg. /" + baseCommand + " ip 127.0.0.1\n" +
                            ImmersiveDamage.prefix + "Current IP: " + config.getConfig().ip), mc.player.getUuid());
                    return 1;
                }).then(ClientCommandManager.argument("newIP", StringArgumentType.string()).executes(context -> {
                    ManagementHandler.changeIP(StringArgumentType.getString(context, "newIP"), true);
                    return 1;
                })))
                .then(ClientCommandManager.literal("port").executes(context -> {
                    mc.player.sendSystemMessage(Text.of(ImmersiveDamage.prefix + "Change the port on which packets will be sent\n" +
                            ImmersiveDamage.prefix + "eg. /" + baseCommand + " port 4447\n" +
                            ImmersiveDamage.prefix + "Current port: " + config.getConfig().port), mc.player.getUuid());
                    return 1;
                }).then(ClientCommandManager.argument("newPort", IntegerArgumentType.integer()).executes(context -> {
                    ManagementHandler.changePort(IntegerArgumentType.getInteger(context, "newPort"), true);
                    return 1;
                })))
                .then(ClientCommandManager.literal("test").executes(context -> {
                    sendTestPacket();
                    return 1;
                }).then(ClientCommandManager.argument("duration", IntegerArgumentType.integer()).executes(context -> {
                    int duration = IntegerArgumentType.getInteger(context, "duration");
                    sendTestPacket(duration);
                    return 1;
                }).then(ClientCommandManager.argument("strength", IntegerArgumentType.integer()).executes(context -> {
                    int duration = IntegerArgumentType.getInteger(context, "duration");
                    int strength = IntegerArgumentType.getInteger(context, "strength");
                    sendTestPacket(duration, strength);
                    return 1;
                }))))
                .then(ClientCommandManager.literal("maxStrengthAtHP").executes(context -> {
                    mc.player.sendSystemMessage(Text.of(ImmersiveDamage.prefix + "If a player takes damage of this value or higher, the phone will vibrate at the full strength, if he'll take damage of half of this value, the strength of vibration will be 50% etc. The value is in HP points (1 heart = 2 HP)\n" +
                            ImmersiveDamage.prefix + "eg. /" + baseCommand + " maxStrengthAtHP 6\n" +
                            ImmersiveDamage.prefix + "Current maxStrengthAtHP: " + config.getConfig().maxStrengthAtHP), mc.player.getUuid());
                    return 1;
                }).then(ClientCommandManager.argument("newStrength", IntegerArgumentType.integer()).executes(context -> {
                    ManagementHandler.changeMaxStrengthAtHP(IntegerArgumentType.getInteger(context, "newStrength"), true);
                    return 1;
                })))
                .then(ClientCommandManager.literal("duration").executes(context -> {
                    mc.player.sendSystemMessage(Text.of(ImmersiveDamage.prefix + "Change the duration of every vibration in miliseconds\n" +
                            ImmersiveDamage.prefix + "eg. /" + baseCommand + " duration 500\n" +
                            ImmersiveDamage.prefix + "Current duration: " + config.getConfig().duration), mc.player.getUuid());
                    return 1;
                }).then(ClientCommandManager.argument("newDuration", IntegerArgumentType.integer()).executes(context -> {
                    ManagementHandler.changePort(IntegerArgumentType.getInteger(context, "newDuration"), true);
                    return 1;
                }))));
    }

    private static void sendTestPacket() {
        sendTestPacket(config.getConfig().duration, 200);
    }

    private static void sendTestPacket(int duration) {
        sendTestPacket(duration, 200);
    }

    private static void sendTestPacket(int duration, int strength) {
        DamagePacket damagePacket = new DamagePacket(duration, strength);
        String ip = config.getConfig().ip;
        int port = config.getConfig().port;
        UDPManager.udpClient.sendPacketAsync(ip, port, damagePacket);
        mc.player.sendSystemMessage(Text.of(ImmersiveDamage.prefix + "Sending damage packet with duration " + damagePacket.duration + " and strength " + damagePacket.strength + " to the app on " + ip + ":" + port), mc.player.getUuid());
    }

    private static String credits() {
        return "Made with " + EnumChatFormatting.RED + "♥" + EnumChatFormatting.RESET + " by Skillter!\n" +
                EnumChatFormatting.GOLD + "[Commands]" + EnumChatFormatting.RESET + " /immersivedamage [toggle, ip, port, maxStrengthAtHP, duration, test]\n" +
                EnumChatFormatting.GOLD + "[Discord] " + EnumChatFormatting.GRAY + ContactInfo.DISCORD_USERNAME + "\n" +
                EnumChatFormatting.GOLD + "[Github] " + EnumChatFormatting.GRAY + ContactInfo.GITHUB_ACCOUNT_LINK + EnumChatFormatting.RESET;
    }

}
