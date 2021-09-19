package skillter.immersivedamage.handler;


import skillter.immersivedamage.Reference;
import skillter.immersivedamage.util.MoreMath;

import static skillter.immersivedamage.ImmersiveDamage.config;

public class VibrationStrengthCalculator {
    // from 1 to 255

    public static int calculateStrength(float amountOfTakenDamage) {
        return MoreMath.constrainToRange(Math.round(((amountOfTakenDamage * (Reference.MAX_STRENGTH - Reference.MIN_STRENGTH)) / config.getConfig().maxStrengthAtHP) - Reference.MIN_STRENGTH), Reference.MIN_STRENGTH, Reference.MAX_STRENGTH);
    }

}
