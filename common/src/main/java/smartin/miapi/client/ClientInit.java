package smartin.miapi.client;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.model.CustomColorProvider;
import smartin.miapi.mixin.ItemRendererAccessor;

public class ClientInit {

    public ClientInit(){
        ((ItemRendererAccessor) MinecraftClient.getInstance().getItemRenderer()).color().register(new CustomColorProvider(), Miapi.modularItem);
        ClientChatEvent.SEND.register((message, component) -> {
            Miapi.LOGGER.warn("opening 1");
            Miapi.server.getPlayerManager().getPlayerList().forEach(serverPlayer->{
                serverPlayer.openHandledScreen(test());
            });
            return EventResult.interrupt(false);
        });

    }

    public static NamedScreenHandlerFactory test(){
        //return Miapi.CRAFTING_SCREEN_HANDLER.create(213213,MinecraftClient.getInstance().player.getInventory());
        Miapi.LOGGER.warn("opening 2");
        Text text = Text.literal("test");
        return new SimpleNamedScreenHandlerFactory( (syncId, inventory, player) -> {
            Miapi.LOGGER.warn("opening 3");
            return new CraftingScreenHandler(syncId, inventory);
            }, text);
    }
}
