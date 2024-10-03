package bg.sofia.uni.fmi.mjt.dungeons.treasure;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Position;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TreasureTest {
    Hero hero = new Hero("hero", new Position(1, 1), 1);

    @Test
    void testCollectHealthPotion() throws BackpackCapacityExceededException {
        Assertions.assertEquals(new HealthPotion(100).collect(hero), "You found a health potion!", "Should return proper message when a health potion is collected");
    }

    @Test
    void testCollectManaPotion() throws BackpackCapacityExceededException {
        Assertions.assertEquals(new ManaPotion(100).collect(hero), "You found a mana potion!", "Should return proper message when a mana potion is collected");
    }

    @Test
    void testCollectWeapon() throws BackpackCapacityExceededException {
        Assertions.assertEquals(new Weapon("axe", 100, 1).collect(hero), "You found a weapon! Damage:100 Level:1", "Should return proper message when a weapon is collected");
    }

    @Test
    void testCollectSpellPotion() throws BackpackCapacityExceededException {
        Assertions.assertEquals(new Spell("fireball", 100, 1, 1).collect(hero), "You found a spell! Damage:100 ManaCost:1 Level:1", "Should return proper message when a spell is collected");
    }

    @Test
    void testUseHealthPotion() {
        Assertions.assertEquals(new HealthPotion(100).use(hero), "Used health potion! +100HP", "Should return proper message when a health potion is used");
    }

    @Test
    void testUseManaPotion() {
        Assertions.assertEquals(new ManaPotion(100).use(hero), "Used mana potion! +100MP", "Should return proper message when a mana potion is used");
    }

    @Test
    void testUseWeapon() {
        Assertions.assertEquals(new Weapon("AXE", 100, 1).use(hero), "Equipped weapon. AXE Damage:100 Level:1", "Should return proper message when a weapon is equipped");
    }

    @Test
    void testUseSpell() {
        Assertions.assertEquals(new Spell("fireball", 1100, 1, 1).use(hero), "Equipped Spell. fireball Damage:1100 ManaCost:1 Level:1", "Should return proper message when a spell is equipped");
    }

}
