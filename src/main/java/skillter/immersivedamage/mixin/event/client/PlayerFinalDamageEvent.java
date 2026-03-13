package skillter.immersivedamage.mixin.event.client;


import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skillter.immersivedamage.callback.PlayerFinalDamageCallback;

@Mixin(ClientPlayerEntity.class)
public class PlayerFinalDamageEvent {

    // Inject into damage method to catch all damage including absorption
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount > 0) {
            PlayerFinalDamageCallback.EVENT.invoker().interact((ClientPlayerEntity) (Object) this, amount);
        }
    }

}
