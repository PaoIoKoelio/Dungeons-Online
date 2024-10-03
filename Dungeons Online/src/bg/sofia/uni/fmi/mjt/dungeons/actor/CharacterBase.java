package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.treasure.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;

public abstract class CharacterBase implements Actor {
    protected String name;
    protected Weapon weapon;
    protected Spell spell;
    protected int health;
    protected int mana;
    protected int attack;
    protected int defence;
    protected int maxHealth;
    protected int maxMana;
    protected int level;
    private static final int HEALTH_AND_MANA_INCREASE = 10;
    private static final int ATTACK_AND_DEFENCE_INCREASE = 10;
    private static final int MIN_EFFECTIVE_DAMAGE = 1;
    private static final int MIN_HEALTH = 0;

    public CharacterBase(String name, int attack, int defence, int health,
                         int mana, Weapon weapon, Spell spell, int level) {
        this.name = name;
        this.attack = attack;
        this.defence = defence;
        this.health = health;
        this.mana = mana;
        this.maxHealth = health;
        this.maxMana = mana;
        this.weapon = weapon;
        this.spell = spell;
        this.level = level;
    }

    protected void levelUp() {
        attack += ATTACK_AND_DEFENCE_INCREASE;
        defence += ATTACK_AND_DEFENCE_INCREASE;
        maxHealth += HEALTH_AND_MANA_INCREASE;
        maxMana += HEALTH_AND_MANA_INCREASE;
        health = maxHealth;
        mana = maxMana;
        level++;
    }

    @Override
    public int attack() {
        if (weapon == null && spell == null) {
            return attack;
        }

        int weaponDamage = (weapon != null) ? weapon.getDamage() + attack : attack;
        int spellDamage = (spell != null && mana >= spell.getManaCost()) ? spell.getDamage() : 0;

        if (spellDamage > weaponDamage) {
            mana -= spell.getManaCost();
            return spellDamage;
        }

        return weaponDamage;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMana() {
        return mana;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefence() {
        return defence;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean isAlive() {
        return health != MIN_HEALTH;
    }

    @Override
    public void takeDamage(int damagePoints) {
        int effectiveDamage;
        if (damagePoints <= defence) {
            effectiveDamage = MIN_EFFECTIVE_DAMAGE;
        } else {
            effectiveDamage = damagePoints - defence;
        }
        health = Math.max(health - effectiveDamage, MIN_HEALTH);
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }
}
