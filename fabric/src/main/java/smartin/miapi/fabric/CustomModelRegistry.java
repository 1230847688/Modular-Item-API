package smartin.miapi.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.CustomModel;

@Environment(EnvType.CLIENT)
public class CustomModelRegistry implements ModelResourceProvider{
    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
        if(isModularItem(identifier)) {
            return new CustomModel();
        } else {
            return null;
        }
    }

    private static boolean isModularItem(Identifier identifier){
        return Miapi.modularItemRegistry.get(identifier.toString().replace("item/",""))!=null;
    }
}
