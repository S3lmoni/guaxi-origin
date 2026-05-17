package net.s3lmoni.guaxiorigin.power;

import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.PowerFactorySupplier;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.core.Registry;

public class GuaxiPowers {
    public static void register() {
        register(EdibleItemPower::createFactory);
        register(ActionOnDeathPower::createFactory);
    }

    public static void register(PowerFactory<?> powerFactory) {
        Registry.register(ApoliRegistries.POWER_FACTORY, powerFactory.getSerializerId(), powerFactory);
    }

    public static void register(PowerFactorySupplier<?> powerFactorySupplier) {
        register(powerFactorySupplier.createFactory());
    }
}
