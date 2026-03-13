package skillter.immersivedamage.handler;

import skillter.immersivedamage.Reference;
import skillter.immersivedamage.util.MoreMath;

import static skillter.immersivedamage.ImmersiveDamage.config;

public class VibrationStrengthCalculator {

    public static int calculateStrength(float amountOfTakenDamage) {
        // Linear scale: maxStrengthAtHP damage = MAX_STRENGTH (255)
        float ratio = amountOfTakenDamage / config.getConfig().maxStrengthAtHP;
        int strength = Math.round(Reference.MIN_STRENGTH + (ratio * (Reference.MAX_STRENGTH - Reference.MIN_STRENGTH)));
        return MoreMath.constrainToRange(strength, Reference.MIN_STRENGTH, Reference.MAX_STRENGTH);
    }

}
