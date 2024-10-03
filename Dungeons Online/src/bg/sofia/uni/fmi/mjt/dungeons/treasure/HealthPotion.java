package bg.sofia.uni.fmi.mjt.dungeons.treasure;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;

public class HealthPotion implements Treasure {

    private String name;
    private int healingPoints;
    private static final String HEALTH_POTION = "Health Potion+";
    private static final String FOUND_HEALTH_POTION = "You found a health potion!";
    private static final String USED_HEALTH_POTION = "Used health potion! +";
    private static final String HP = "HP";

    public HealthPotion(int healthPoints) {
        this.name = HEALTH_POTION + healthPoints;
        this.healingPoints = healthPoints;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String collect(Hero hero) throws BackpackCapacityExceededException {
        hero.pickUp(this);
        return FOUND_HEALTH_POTION;
    }

    @Override
    public String use(Hero hero) {
        hero.takeHealing(healingPoints);
        int currentHealingPoints = healingPoints;
        healingPoints = 0;
        return USED_HEALTH_POTION + currentHealingPoints + HP;
    }

    public int heal() {
        return healingPoints;
    }
}
