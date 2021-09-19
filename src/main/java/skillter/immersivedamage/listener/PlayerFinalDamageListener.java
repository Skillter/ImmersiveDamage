package skillter.immersivedamage.listener;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import skillter.immersivedamage.ImmersiveDamage;
import skillter.immersivedamage.Reference;
import skillter.immersivedamage.callback.PlayerFinalDamageCallback;
import skillter.immersivedamage.communication.UDPManager;
import skillter.immersivedamage.communication.packet.DamagePacket;
import skillter.immersivedamage.handler.VibrationStrengthCalculator;

import static skillter.immersivedamage.ImmersiveDamage.config;

public class PlayerFinalDamageListener {

    private static float lastHP = 0;
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void registerEvent() {
        PlayerFinalDamageCallback.EVENT.register((player, amount) -> {
            if (lastHP != 0) {
                float takenDamage = lastHP - amount;
                if (takenDamage > 0) {
                    sendDamagePacket(takenDamage);
                }
            }
            lastHP = amount;

            return ActionResult.PASS;
        });
    }

    public static void sendDamagePacket(float amount) {
        if (config.getConfig().enabled) {
            int duration = config.getConfig().duration;
            int strength = VibrationStrengthCalculator.calculateStrength(amount);
            DamagePacket damagePacket = new DamagePacket(duration, strength);

            String ip = config.getConfig().ip;
            int port = config.getConfig().port;
            UDPManager.udpClient.sendPacketAsync(ip, port, damagePacket);

            System.out.println("Sending a packet to: " + ip + ":" + port + " Duration: " + damagePacket.duration + " Strength: " + damagePacket.strength);
        }
    }


}
