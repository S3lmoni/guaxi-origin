package net.s3lmoni.guaxiorigin.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.s3lmoni.guaxiorigin.GuaxiOrigin;

public class ModItems {

    public static final Item GUAXI_ICON = registerItem("guaxi_icon", new Item(new Item.Properties()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, GuaxiOrigin.identifier(name), item);
    }

    public static void registerModItems() {
        GuaxiOrigin.LOGGER.info("Registering icon for " + GuaxiOrigin.MOD_ID);
    }
}
