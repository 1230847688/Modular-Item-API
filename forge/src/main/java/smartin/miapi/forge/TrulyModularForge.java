package smartin.miapi.forge;

import com.google.common.collect.Lists;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.world.SaveProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.AttributeProperty;

import java.util.Collection;
import java.util.Iterator;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.attributes.AttributeRegistry.SWIM_SPEED;

@Mod(MOD_ID)
public class TrulyModularForge {
    public TrulyModularForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Miapi.init();
        //ATTRIBUTE REPLACEMENT
        //AttributeRegistry.REACH = ForgeMod.BLOCK_REACH.getRaw();
        //AttributeRegistry.ATTACK_RANGE = ForgeMod.ENTITY_REACH.getRaw();
        LifecycleEvent.SERVER_STARTING.register((instance -> {
            AttributeRegistry.REACH = ForgeMod.BLOCK_REACH.get();
            AttributeRegistry.ATTACK_RANGE = ForgeMod.ENTITY_REACH.get();
            AttributeRegistry.SWIM_SPEED = ForgeMod.SWIM_SPEED.get();
        }));
        LifecycleEvent.SERVER_STARTED.register((minecraftServer -> {
            if(!Environment.isClient()){
                ResourcePackManager resourcePackManager = minecraftServer.getDataPackManager();
                SaveProperties saveProperties = minecraftServer.getSaveProperties();
                Collection<String> collection = resourcePackManager.getEnabledNames();
                Collection<String> collection2 = findNewDataPacks(resourcePackManager, saveProperties, collection);
                minecraftServer.reloadResources(collection2).exceptionally((throwable) -> {
                    Miapi.LOGGER.warn("Failed to execute sceduled reload reload", throwable);
                    return null;
                });
            }
        }));
        AttributeProperty.replaceMap.put("miapi:generic.reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("forge:block_reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("forge:entity_reach", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.swim_speed", () -> SWIM_SPEED);
    }

    private static Collection<String> findNewDataPacks(ResourcePackManager dataPackManager, SaveProperties saveProperties, Collection<String> enabledDataPacks) {
        dataPackManager.scanPacks();
        Collection<String> collection = Lists.newArrayList(enabledDataPacks);
        Collection<String> collection2 = saveProperties.getDataConfiguration().dataPacks().getDisabled();
        Iterator var5 = dataPackManager.getNames().iterator();

        while(var5.hasNext()) {
            String string = (String)var5.next();
            if (!collection2.contains(string) && !collection.contains(string)) {
                collection.add(string);
            }
        }

        return collection;
    }


    public static class ClientEvents {
        @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
        public static class ModBus {
            @SubscribeEvent
            public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
                AttributeRegistry.REACH = ForgeMod.BLOCK_REACH.get();
                AttributeRegistry.ATTACK_RANGE = ForgeMod.ENTITY_REACH.get();
            }
        }
    }
}