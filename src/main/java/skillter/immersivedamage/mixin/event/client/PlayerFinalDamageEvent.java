package skillter.immersivedamage.mixin.event.client;


import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skillter.immersivedamage.callback.PlayerFinalDamageCallback;

@Mixin(ClientPlayerEntity.class)
public class PlayerFinalDamageEvent {

    @Inject(method = "updateHealth", at = @At("HEAD"))
    private void onDamage(float amount, CallbackInfo callback) {

        PlayerFinalDamageCallback.EVENT.invoker().interact((ClientPlayerEntity) (Object) this, amount);
    }

}
