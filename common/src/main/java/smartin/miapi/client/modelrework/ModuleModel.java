package smartin.miapi.client.modelrework;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleModel {
    public List<Pair<Matrix4f, MiapiModel>> models;
    public Map<String, List<Pair<Matrix4f, MiapiModel>>> otherModels;
    public final ItemModule.ModuleInstance instance;
    public Map<Integer, ModuleModel> subModuleModels = new HashMap<>();

    public ModuleModel(ItemModule.ModuleInstance instance) {
        this.instance = instance;
        models = generateModel(null);
        Miapi.LOGGER.error("model is generated");
        otherModels = new HashMap<>();
        otherModels.put("item", models);
    }

    private List<Pair<Matrix4f, MiapiModel>> generateModel(String key) {
        List<Pair<Matrix4f, MiapiModel>> modelList = new ArrayList<>();
        Transform transform = SlotProperty.getTransformStack(instance).get(key).copy();
        Matrix4f matrix4f = Transform.toModelTransformation(transform).toMatrix();
        Miapi.LOGGER.error(instance.module.getName() + " " + key + " " + transform.toString());
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            supplier.getModels(key, instance).forEach(model -> {
                modelList.add(new Pair<>(new Matrix4f(matrix4f), model));
            });
        }
        return modelList;
    }

    public void render(String modelTypeRaw, ItemStack stack, MatrixStack matrices, ModelTransformationMode mode, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        String modelType = modelTypeRaw == null ? "item" : modelTypeRaw;
        if (!otherModels.containsKey(modelType)) {
            Miapi.LOGGER.error("generating model for Type " + modelType);
            otherModels.put(modelType, generateModel(modelType));
        }
        Map<Integer, Matrix4f> map = new HashMap<>();
        //render own Models
        otherModels.get(modelType).forEach(matrix4fMiapiModelPair -> {
            matrices.push();
            matrices.peek().getPositionMatrix().mul(matrix4fMiapiModelPair.getFirst());
            matrix4fMiapiModelPair.getSecond().render(matrices, stack, mode, tickDelta, vertexConsumers, light, overlay);
            matrices.pop();

            //prepare for submodules
            instance.subModules.forEach((integer, instance1) -> {
                Matrix4f matrix4f = map.getOrDefault(integer, new Matrix4f());
                Matrix4f subModuleMatrix = matrix4fMiapiModelPair.getSecond().subModuleMatrix(integer);
                if (subModuleMatrix != null) {
                    matrix4f.mul(matrix4fMiapiModelPair.getSecond().subModuleMatrix(integer));
                }
                map.put(integer, matrix4f);
            });
        });
        //render submodules
        instance.subModules.forEach((integer, instance1) -> {
            matrices.push();
            matrices.multiplyPositionMatrix(map.get(integer));
            ModuleModel subModuleModel = subModuleModels.get(integer);
            if (subModuleModel == null) {
                subModuleModel = new ModuleModel(instance1);
                subModuleModels.put(integer, subModuleModel);
                Miapi.LOGGER.error("regen Model");
            }
            subModuleModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, integer, overlay);
            matrices.pop();
        });
    }
}