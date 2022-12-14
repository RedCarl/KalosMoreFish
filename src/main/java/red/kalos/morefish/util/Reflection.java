package red.kalos.morefish.util;

import org.bukkit.Bukkit;

public class Reflection {
  private static String nms = "net.minecraft.server.";
  private static String craftbukkit = "org.bukkit.craftbukkit.";

  static {
    String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    nms += version + ".";
    craftbukkit += version + ".";
  }

  private Reflection() {
    throw new IllegalAccessError("Utility class");
  }

  public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
    return Class.forName(nms + name);
  }

  public static Class<?> getCBClass(String name) throws ClassNotFoundException {
    return Class.forName(craftbukkit + name);
  }
}