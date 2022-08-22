package red.kalos.morefish.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class IdentityUtils
{
  private IdentityUtils() {
    throw new IllegalAccessError("Utility class");
  }

  public static Material getMaterial(String minecraftId) {
    try {
      Class<?> minecraftKey = Reflection.getNMSClass("MinecraftKey");
      Object mk = minecraftKey.getConstructor(new Class[] { String.class }).newInstance(new Object[] { minecraftId });

      Class<?> craftItemStack = Reflection.getCBClass("inventory.CraftItemStack");
      Class<?> itemClass = Reflection.getNMSClass("Item");
      Class<?> registryMaterials = Reflection.getNMSClass("RegistryMaterials");

      Field field = itemClass.getDeclaredField("REGISTRY");
      field.setAccessible(true);
      Object registry = field.get(null);

      Method get = registryMaterials.getMethod("get", new Class[] { Object.class });
      Object item = get.invoke(registry, new Object[] { mk });

      Method asNewCraftStack = craftItemStack.getMethod("asNewCraftStack", new Class[] { itemClass });
      ItemStack itemStack = (ItemStack)asNewCraftStack.invoke(null, new Object[] { item });

      return itemStack.getType();
    } catch (Exception e) {
      return null;
    }
  }

  public static Enchantment getEnchantment(String minecraftId) {
    try {
      Class<?> minecraftKey = Reflection.getNMSClass("MinecraftKey");
      Object mk = minecraftKey.getConstructor(new Class[] { String.class }).newInstance(new Object[] { minecraftId });

      Class<?> craftEnchantment = Reflection.getCBClass("enchantments.CraftEnchantment");
      Class<?> enchantmentClass = Reflection.getNMSClass("Enchantment");
      Class<?> registryMaterials = Reflection.getNMSClass("RegistryMaterials");

      Field field = enchantmentClass.getDeclaredField("enchantments");
      field.setAccessible(true);
      Object registry = field.get(null);

      Method get = registryMaterials.getMethod("get", new Class[] { Object.class });
      Object ench = get.invoke(registry, new Object[] { mk });

      for (Enchantment enchantment : Enchantment.values()) {
        Method getHandle = craftEnchantment.getMethod("getHandle", new Class[0]);
        Object handle = getHandle.invoke(enchantment, new Object[0]);

        if (ench.equals(handle)) {
          return enchantment;
        }
      }

      return null;
    }
    catch (Exception e) {
      return null;
    }
  }

  public static PotionEffectType getPotionEffectType(String minecraftId) {
    try {
      Class<?> minecraftKey = Reflection.getNMSClass("MinecraftKey");
      Object mk = minecraftKey.getConstructor(new Class[] { String.class }).newInstance(new Object[] { minecraftId });

      Class<?> craftPotionEffectType = Reflection.getCBClass("potion.CraftPotionEffectType");
      Class<?> mobEffectListClass = Reflection.getNMSClass("MobEffectList");
      Class<?> registryMaterials = Reflection.getNMSClass("RegistryMaterials");

      Field field = mobEffectListClass.getDeclaredField("REGISTRY");
      field.setAccessible(true);
      Object registry = field.get(null);

      Method get = registryMaterials.getMethod("get", new Class[] { Object.class });
      Object effect = get.invoke(registry, new Object[] { mk });

      for (PotionEffectType effectType : PotionEffectType.values()) {
        if (effectType != null) {


          Method getHandle = craftPotionEffectType.getMethod("getHandle", new Class[0]);
          Object handle = getHandle.invoke(effectType, new Object[0]);

          if (effect.equals(handle)) {
            return effectType;
          }
        }
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}