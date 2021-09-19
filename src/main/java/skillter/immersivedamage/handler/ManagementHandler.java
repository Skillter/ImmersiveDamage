package skillter.immersivedamage.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import skillter.immersivedamage.ImmersiveDamage;
import skillter.immersivedamage.Reference;
import skillter.immersivedamage.config.Config;
import skillter.immersivedamage.util.EnumChatFormatting;
import skillter.immersivedamage.util.MoreMath;

import static skillter.immersivedamage.ImmersiveDamage.config;

public class ManagementHandler {

    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void toggleMod(boolean playerChatFeedback) {
        if (config.getConfig().enabled == false) {
            config.getConfig().enabled = true;
            config.save();
            if (playerChatFeedback) sendFeedback(EnumChatFormatting.GREEN + "The mod has been enabled!");
        } else {
            config.getConfig().enabled = false;
            config.save();
            if (playerChatFeedback) sendFeedback(EnumChatFormatting.RED + "The mod has been disabled!");
        }
    }

    public static void changeIP(String ip, boolean playerChatFeedback) {
        config.getConfig().ip = ip;
        config.save();
        if (playerChatFeedback) sendFeedback(EnumChatFormatting.GREEN + "The IP has been changed to: " + ip);
    }

    public static void changePort(int port, boolean playerChatFeedback) {
        config.getConfig().port = port;
        config.save();
        if (playerChatFeedback) sendFeedback(EnumChatFormatting.GREEN + "The port has been changed to: " + port);
    }

    public static void changeMaxStrengthAtHP(int newValue, boolean playerChatFeedback) {
        if (newValue < Reference.MIN_STRENGTH || newValue > Reference.MAX_STRENGTH) {
            newValue = MoreMath.constrainToRange(newValue, Reference.MIN_STRENGTH, Reference.MAX_STRENGTH);
            if (playerChatFeedback)
                sendFeedback(EnumChatFormatting.RED + "Wrong value! " + EnumChatFormatting.RESET + "Min. " + Reference.MIN_STRENGTH + " Max. " + Reference.MAX_STRENGTH + " The value has been shrunk to " + newValue + " to meet the requirement");
        }
        config.getConfig().maxStrengthAtHP = newValue;
        config.save();
        if (playerChatFeedback)
            sendFeedback(EnumChatFormatting.GREEN + "The maxStrengthAtHP has been changed to: " + newValue);
    }

    public static void changeDuration(int newValue, boolean playerChatFeedback) {
        if (newValue < 1 || newValue > Reference.MAX_DURATION) {
            newValue = MoreMath.constrainToRange(newValue, 1, Reference.MAX_DURATION);
            if (playerChatFeedback)
                sendFeedback(EnumChatFormatting.RED + "Wrong value! " + EnumChatFormatting.RESET + "Min. " + 1 + " Max. " + Reference.MAX_DURATION + " The value has been shrunk to " + newValue + " to meet the requirement");
        }
        config.getConfig().duration = newValue;
        config.save();
        if (playerChatFeedback)
            sendFeedback(EnumChatFormatting.GREEN + "The duration has been changed to: " + newValue);
    }


    private static void sendFeedback(String message) {
        if (mc.player != null)
            mc.player.sendSystemMessage(Text.of(ImmersiveDamage.prefix + message), mc.player.getUuid());
    }
}
