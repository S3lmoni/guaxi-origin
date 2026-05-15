package net.s3lmoni.guaxiorigin;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;
import net.s3lmoni.guaxiorigin.power.GuaxiPowers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuaxiOrigin implements ModInitializer {
	public static final String MOD_ID = "guaxi-origin";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation identifier(String id) {
        return new ResourceLocation(MOD_ID, id);
    }

	@Override
	public void onInitialize() {
		GuaxiPowers.register();
		LOGGER.info("Hello from GuaxiOrigin");
	}
}