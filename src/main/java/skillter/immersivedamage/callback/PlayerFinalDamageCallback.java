package skillter.immersivedamage.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerFinalDamageCallback {

    Event<PlayerFinalDamageCallback> EVENT = EventFactory.createArrayBacked(PlayerFinalDamageCallback.class,
            (listeners) -> (player, amount) -> {
                for (PlayerFinalDamageCallback listener : listeners) {
                    ActionResult result = listener.interact(player, amount);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(ClientPlayerEntity player, float amount);
}
