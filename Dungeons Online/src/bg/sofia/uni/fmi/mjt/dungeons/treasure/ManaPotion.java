package bg.sofia.uni.fmi.mjt.dungeons.treasure;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;

public class ManaPotion implements Treasure {
    private static final String MANA_POTION = "Mana Potion+";
    private static final String FOUND_MANA_POTION = "You found a mana potion!";
    private static final String USED_MANA_POTION = "Used mana potion! +";
    private static final String MP = "MP";
    private int manaPoints;
    private String name;

    public ManaPotion(int manaPoints) {
        this.name = MANA_POTION + manaPoints;
        this.manaPoints = manaPoints;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String collect(Hero hero) throws BackpackCapacityExceededException {
        hero.pickUp(this);
        return FOUND_MANA_POTION;
    }

    @Override
    public String use(Hero hero) {
        hero.takeMana(manaPoints);
        int currentManaPoints = manaPoints;
        manaPoints = 0;
        return USED_MANA_POTION + currentManaPoints + MP;
    }

    public int heal() {
        return manaPoints;
    }
}
