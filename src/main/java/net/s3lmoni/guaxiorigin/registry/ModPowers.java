package net.s3lmoni.guaxiorigin.registry;

import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.PowerFactorySupplier;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.core.Registry;
import net.s3lmoni.guaxiorigin.GuaxiOrigin;
import net.s3lmoni.guaxiorigin.power.ActionOnDeathPower;
import net.s3lmoni.guaxiorigin.power.EdibleItemPower;

public class ModPowers {
    public static void register() {
        GuaxiOrigin.LOGGER.info("Registering new power EdibleItem for " + GuaxiOrigin.MOD_ID);
        register(EdibleItemPower::createFactory);

        GuaxiOrigin.LOGGER.info("Registering new power ActionOnDeath for " + GuaxiOrigin.MOD_ID);
        register(ActionOnDeathPower::createFactory);
    }

    public static void register(PowerFactory<?> powerFactory) {
        Registry.register(ApoliRegistries.POWER_FACTORY, powerFactory.getSerializerId(), powerFactory);
    }

    public static void register(PowerFactorySupplier<?> powerFactorySupplier) {
        register(powerFactorySupplier.createFactory());
    }
}
