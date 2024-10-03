package bg.sofia.uni.fmi.mjt.dungeons.treasure;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;

public interface Treasure {
    String getName();

    String collect(Hero hero) throws BackpackCapacityExceededException;

    String use(Hero hero);
}
