package net.gpdev.darkly.actions;

import net.gpdev.darkly.actors.GameEntity;

public class Attack extends EntityAction {

    private final int attackSkill;
    private final int maxDamage;


    public Attack(final GameEntity source, final GameEntity target, final int attackSkill, final int maxDamage) {
        super(Type.ATTACK, source, target);
        this.attackSkill = attackSkill;
        this.maxDamage = maxDamage;
    }

    public int getAttackSkill() {
        return attackSkill;
    }

    public int getMaxDamage() {
        return maxDamage;
    }
}
