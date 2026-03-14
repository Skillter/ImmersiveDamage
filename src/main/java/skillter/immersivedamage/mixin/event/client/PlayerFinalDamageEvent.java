package skillter.immersivedamage.mixin.event.client;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skillter.immersivedamage.callback.PlayerFinalDamageCallback;

@Mixin(ClientPlayerEntity.class)
public class PlayerFinalDamageEvent {

    // track both health and absorption so damage that eats golden hearts still triggers
    private float lastTotalHealth = -1;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        float currentTotal = player.getHealth() + player.getAbsorptionAmount();

        // set initial value so we can compare in future ticks
        if (lastTotalHealth < 0) {
            lastTotalHealth = currentTotal;
            return;
        }

        float damageTaken = lastTotalHealth - currentTotal;

        // only trigger on actual health loss - this filters out fire resistance,
        // damage immunity frames, and any other case where damage is blocked
        if (damageTaken > 0.001f) {
            PlayerFinalDamageCallback.EVENT.invoker().interact(player, damageTaken);
        }

        lastTotalHealth = currentTotal;
    }

}
