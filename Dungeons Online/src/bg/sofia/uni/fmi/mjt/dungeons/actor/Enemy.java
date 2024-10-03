package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.treasure.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;

public class Enemy extends CharacterBase {
    private static final int MIN_LEVEL = 1;

    public Enemy(String name, int attack, int defence, int health, int mana, Weapon weapon, Spell spell, int level) {
        super(name, attack, defence, health, mana, weapon, spell, level);
        while (level > MIN_LEVEL) {
            levelUp();
            level--;
        }

    }
}
