package com.ehrengrenlund.cozyhomes.data;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class PlayerInitializer implements EntityComponentInitializer {
    public static final ComponentKey<HomeData> HOME_DATA =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("cozyhomes", "homes"), HomeData.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(HOME_DATA, playerEntity -> new HomeData(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}
