package smartin.miapi.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvent;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.network.Networking;

public class TrulyModularFabric implements ModInitializer {


    static{
        Miapi.staticSetup();
    }

    @Override
    public void onInitialize() {
        Miapi.init();

        ReloadEvent.Data.subscribe(((path, data) -> {
            //Miapi.LOGGER.info(path);
            //Miapi.LOGGER.warn(data);
        }));
        ReloadEvent.Data.subscribe(ItemModule::loadFromData);
        ClientSync.init();
        Miapi.itemRegistry.addCallback(item ->   {
            Registry.register(Registry.ITEM, Miapi.modularItemIdentifier, item);
            Miapi.LOGGER.info("registered Item successfully");
        });
        //NETWORKING
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);

        //DATA
        if(Environment.isClient()){
            MiapiClient.setupClient();
        }
    }

    public static ModelLoader getModelLoader() {
        return null;
    }

    private void onServerStart(MinecraftServer minecraftServer) {
        Networking.setImplementation(new NetworkingImplFabric());
    }
}