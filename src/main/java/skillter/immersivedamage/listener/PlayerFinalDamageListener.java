package skillter.immersivedamage.listener;

import net.minecraft.util.ActionResult;
import skillter.immersivedamage.callback.PlayerFinalDamageCallback;
import skillter.immersivedamage.communication.NetworkDiscovery;
import skillter.immersivedamage.communication.UDPManager;
import skillter.immersivedamage.communication.packet.DamagePacket;
import skillter.immersivedamage.handler.VibrationStrengthCalculator;

import static skillter.immersivedamage.ImmersiveDamage.config;

public class PlayerFinalDamageListener {

    public static void registerEvent() {
        PlayerFinalDamageCallback.EVENT.register((player, amount) -> {
            sendDamagePacket(amount);
            return ActionResult.PASS;
        });
    }

    public static void sendDamagePacket(float amount) {
        if (config.getConfig().enabled) {
            int duration = config.getConfig().duration;
            int strength = VibrationStrengthCalculator.calculateStrength(amount);
            DamagePacket damagePacket = new DamagePacket(0, duration, strength);

            String ip;
            int port;

            // use auto-detected IP if enabled and available
            if (config.getConfig().autoDetect && NetworkDiscovery.hasDiscoveredDevice()) {
                NetworkDiscovery.DiscoveredDevice device = NetworkDiscovery.getBestDevice();
                ip = device.ip;
                port = device.port;
            } else {
                ip = config.getConfig().ip;
                port = config.getConfig().port;
            }

            UDPManager.udpClient.sendPacketReliableAsync(ip, port, damagePacket);

            System.out.println("Sending a packet to: " + ip + ":" + port + " Duration: " + damagePacket.duration + " Strength: " + damagePacket.strength);
        }
    }


}
