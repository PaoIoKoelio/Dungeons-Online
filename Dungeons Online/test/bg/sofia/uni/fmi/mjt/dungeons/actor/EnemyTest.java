package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnemyTest {
    @Test
    void testEnemyInitialization() {
        Enemy enemy = new Enemy("Grendel", 100, 100, 100, 100, new Weapon("axe", 1, 1), null, 4);
        Assertions.assertEquals(enemy.getHealth(), 130, "Enemy health should increase according to the set level and the set base stats.");
    }
}
