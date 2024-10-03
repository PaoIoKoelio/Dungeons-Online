package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoSuchItemException;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.HealthPotion;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.ManaPotion;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HeroTest {
    Hero hero = new Hero("name", new Position(1, 2), 1);

    @Test
    void testUseHealthPotion() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new HealthPotion(100));
        Assertions.assertEquals(hero.useItem(0), "Used health potion! +100HP", "Correct message should be returned when using a health potion");
    }

    @Test
    void testUseManaPotion() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new ManaPotion(100));
        Assertions.assertEquals(hero.useItem(0), "Used mana potion! +100MP", "Correct message should be returned when using a mana potion");
    }

    @Test
    void testEquipWeapon() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Weapon("sword", 100, 0));
        Assertions.assertEquals(hero.useItem(0), "Equipped weapon. sword Damage:100 Level:0", "Correct message should be returned when equipping a weapon");
    }

    @Test
    void testEquipSpell() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("spell", 100, 10, 1));
        Assertions.assertEquals(hero.useItem(0), "Equipped Spell. spell Damage:100 ManaCost:10 Level:1", "Correct message should be returned when equipping a spell");
    }

    @Test
    void testCollectItem() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new HealthPotion(100));
        Assertions.assertEquals("Health Potion+100", hero.getItem(0).getName(), "Should successfully collect a health potion");
    }

    @Test
    void testCollectManaPotion() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new ManaPotion(50));
        Assertions.assertEquals("Mana Potion+50", hero.getItem(0).getName(), "Should successfully collect a mana potion");
    }

    @Test
    void testCollectWeapon() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Weapon("sword", 100, 1));
        Assertions.assertEquals("sword Damage:100 Level:1", hero.getItem(0).getName(), "Should successfully collect a weapon");
    }

    @Test
    void testCollectSpell() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("sword", 20, 10, 1));
        Assertions.assertEquals("sword Damage:20 ManaCost:10 Level:1", hero.getItem(0).getName(), "Should successfully collect a spell");
    }

    @Test
    void testCollectItemWhenBackpackIsFull() throws BackpackCapacityExceededException {
        for (int i = 0; i < 10; i++) {
            hero.pickUp(new HealthPotion(100));
        }
        Assertions.assertThrows(BackpackCapacityExceededException.class, () -> hero.pickUp(new HealthPotion(100)), "Should throw BackpackCapacityExceededException when trying to add an item to a full backpack.");
    }

    @Test
    void testGainExperience() {
        hero.gainExperience(300);
        Assertions.assertEquals(hero.getLevel(), 3, "300 XP should make the hero level up 2 items, making them level 3");
    }

    @Test
    void testDropRandomItem() throws BackpackCapacityExceededException {
        hero.pickUp(new HealthPotion(10));
        hero.pickUp(new Weapon("axe", 10, 2));
        hero.dieAndResurrect(new Position(4, 4));
        Assertions.assertEquals(hero.getBackpack().size(), 1, "After dying and dropping a random item from the backpack with 2 items there should be only 1 item left");
    }

    @Test
    void testDieAndResurrect() {
        hero.gainExperience(300);//lvl is now 3
        hero.dieAndResurrect(new Position(1, 1));
        Assertions.assertEquals(hero.getLevel(), 1, "After dying heroes level should be back down to 1");
    }

    @Test
    void testTakeDamage() {
        hero.takeDamage(100);
        Assertions.assertEquals(hero.getHealth(), 50, "Health of the hero should be 50 after taking 100 damage(because of 50 defence)");
    }

    @Test
    void testTakeHealing() {
        hero.takeDamage(70);
        hero.takeHealing(10);
        Assertions.assertEquals(hero.getHealth(), 90, "Correct health should be returned to hero when he takes healing");
    }


    @Test
    void testTakeExcessiveHealing() {
        hero.takeDamage(70);
        hero.takeHealing(100);
        Assertions.assertEquals(hero.getHealth(), 100, "Health should be able to go above max health");
    }

    @Test
    void testTakeExcessiveMana() {
        hero.takeMana(100);
        Assertions.assertEquals(hero.getMana(), 100, "Mana should not be able to go above max mana");
    }


    @Test
    void testGetBackpackItems() throws BackpackCapacityExceededException {
        hero.pickUp(new Weapon("axe", 1, 1));
        hero.pickUp(new HealthPotion(100));
        Assertions.assertEquals(hero.getBackpackItems(), "BACKPACK:\naxe Damage:1 Level:1\nHealth Potion+100\n", "Should print out backpack items properly");
    }

    @Test
    void testAttackWithoutWeaponAndSpell() {
        Assertions.assertEquals(hero.attack(), 50, "when no weapon or spell is present should deal base damage(attack)");
    }

    @Test
    void testAttackWithWeapon() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Weapon("halberd", 10, 1));
        hero.useItem(0);
        Assertions.assertEquals(hero.attack(), 60, "When only a weapon is present should deal the damage of the weapon + base damage(attack)");
    }

    @Test
    void testAttackWithSpell() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("fireball", 60, 1, 1));
        hero.useItem(0);
        Assertions.assertEquals(hero.attack(), 60, "When only a spell is present and it deals more damage than the base attack should deal spell's damage");
    }

    @Test
    void testManaAfterAttackWithSpell() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("fireball", 60, 15, 1));
        hero.useItem(0);
        hero.attack();
        Assertions.assertEquals(hero.getMana(), 85, "When a spell is cast mana should be taken");
    }

    @Test
    void testAttackWithBaseDamageWithInferiorSpell() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("fireball", 10, 1, 1));
        hero.useItem(0);
        Assertions.assertEquals(hero.attack(), 50, "When only a spell is present and it deals less damage than the base attack should deal spell's base attack damage");
    }

    @Test
    void testAttackWithSpellAndWeaponPresentWithWeakerSpell() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("fireball", 60, 1, 1));
        hero.pickUp(new Weapon("axe", 60, 1));
        hero.useItem(0);
        hero.useItem(1);
        Assertions.assertEquals(hero.attack(), 110, "When both spell and weapon are present but weapon+base damage has more attack should attack with weapon");
    }

    @Test
    void testAttackWithSpellAndWeaponPresentWithWeakerWeapon() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("fireball", 120, 10, 1));
        hero.pickUp(new Weapon("axe", 60, 1));
        hero.useItem(0);
        hero.useItem(1);
        Assertions.assertEquals(hero.attack(), 120, "When both spell and weapon are present but weapon+base damage has less attack should attack with spell");
    }

    @Test
    void testAttackWithSpellAndWeaponWithInsufficientMana() throws BackpackCapacityExceededException, NoSuchItemException {
        hero.pickUp(new Spell("fireball", 120, 200, 1));
        hero.pickUp(new Weapon("axe", 60, 1));
        hero.useItem(0);
        hero.useItem(1);
        Assertions.assertEquals(hero.attack(), 110, "When spell is better option but hero doesnt have sufficient mana should attack with weapon.");
    }

    @Test
    void testIsAlive() {
        hero.takeDamage(150);
        Assertions.assertFalse(hero.isAlive(), "When character health gets to 0 isAlive should return false");
    }

    @Test
    void testTakeHealthWithNegativeHealingPoints() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> hero.takeHealing(-1), "Trying to heal with negative points should throw IllegalArgumentException");
    }

    @Test
    void testTakeManaWithNegativeManaPoints() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> hero.takeMana(-1), "Trying to get mana with negative points should throw IllegalArgumentException");
    }

    @Test
    void testGetItemWithInvalidIndex() {
        Assertions.assertThrows(NoSuchItemException.class, () -> hero.getItem(-1), "Trying to get item by invalid index should throw NoSuchItemException");
    }

    @Test
    void testRemoveItemWithInvalidIndex() {
        Assertions.assertThrows(NoSuchItemException.class, () -> hero.removeItem(-1), "Trying to remove item by invalid index should throw NoSuchItemException");
    }

    @Test
    void testEquipNullWeapon() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> hero.equip(null), "Trying to equip null weapon should throw IllegalArgumentException");
    }

    @Test
    void testEquipNullSpell() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> hero.learn(null), "Trying to learn null spell should throw IllegalArgumentException");
    }
}
