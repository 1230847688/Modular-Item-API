package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.InterfaceDispatcher;
import com.redpxnda.nucleus.datapack.codec.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PaletteCreators {
    public static void setup() {
        var classLoading = PaletteCreators.class;
    }

    public static final Map<String, MaterialProperty.PaletteCreator> creators = new HashMap<>();
    public static final Map<String, MaterialProperty.FillerFunction> fillers = new HashMap<>();
    public static final InterfaceDispatcher<MaterialProperty.PaletteCreator> paletteCreator = InterfaceDispatcher.of(creators, "type");

    public static MaterialProperty.FillerFunction interpolateFiller;

    static {
        if (Platform.getEnvironment().equals(Env.CLIENT)) {
            interpolateFiller = (last, current, next, lX, cX, nX, placer) -> {
                for (int i = lX; i < cX; i++) {
                    float delta = (i - lX) / (float) (cX - lX);
                    placer.place(
                            new Color(
                                    MathHelper.lerp(delta, last.r, current.r),
                                    MathHelper.lerp(delta, last.g, current.g),
                                    MathHelper.lerp(delta, last.b, current.b),
                                    MathHelper.lerp(delta, last.a, current.a)
                            ), i, 0
                    );
                }
            };
            fillers.put("interpolate", interpolateFiller);
            fillers.put("current_to_last", (last, current, next, lX, cX, nX, placer) -> {
                for (int i = lX; i < cX; i++) {
                    placer.place(current, i, 0);
                }
            });
            fillers.put("last_to_current", (last, current, next, lX, cX, nX, placer) -> {
                for (int i = lX; i < cX; i++) {
                    placer.place(last, i, 0);
                }
            });
            fillers.put("current_last_shared", (last, current, next, lX, cX, nX, placer) -> {
                for (int i = lX; i < cX; i++) {
                    float delta = (i - lX) / (float) (cX - lX);
                    Color color = delta < 0.5 ? last : current;
                    placer.place(color, i, 0);
                }
            });

            creators.put("texture", (json, material) -> {
                if (json instanceof JsonObject object && object.has("location"))
                    return new Identifier(object.get("location").getAsString());
                else if (json instanceof JsonObject)
                    throw new JsonParseException("ModularItem API failed to parse texture sampling palette for material '" + material + "'! Missing member 'location'.");
                else
                    throw new JsonParseException("ModularItem API failed to parse texture sampling palette for material '" + material + "'! Not a JSON object -> " + json);
            });

            Codec<Integer> stringToIntCodec = Codec.STRING.xmap(Integer::parseInt, String::valueOf);
            creators.put("grayscale_map", (json, material) -> {
                if (json instanceof JsonObject object) {
                    try {
                        if (!object.has("colors"))
                            throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Missing member 'colors'.");

                        JsonElement element = object.get("colors");
                        Map<Integer, Color> colors = new HashMap<>(MiscCodecs.quickParse(
                                element, Codec.unboundedMap(stringToIntCodec, MiscCodecs.COLOR),
                                s -> Miapi.LOGGER.error("Failed to create material palette color map from JSON '" + element + "'! -> " + s)
                        ));
                        String key = object.has("filler") ? object.get("filler").getAsString() : "interpolate";
                        MaterialProperty.FillerFunction filler = fillers.getOrDefault(key, interpolateFiller);

                        Color black = new Color(0, 0, 0, 255);
                        Color white = new Color(255, 255, 255, 255);
                        if (!colors.containsKey(0))
                            colors.put(0, black);
                        if (!colors.containsKey(255))
                            colors.put(255, white);

                        Identifier identifier = new Identifier(Miapi.MOD_ID, "textures/generated_materials/" + material);
                        NativeImage image = new NativeImage(256, 1, false);
                        MaterialProperty.PixelPlacer placer = (color, x, y) -> image.setColor(x, y, color.abgr());

                        List<Map.Entry<Integer, Color>> list = colors.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
                        for (int i = 0; i < list.size(); i++) {
                            Map.Entry<Integer, Color> last = i == 0 ? Map.entry(0, black) : list.get(i - 1);
                            Map.Entry<Integer, Color> current = list.get(i);
                            Map.Entry<Integer, Color> next = i == list.size() - 1 ? Map.entry(255, white) : list.get(i + 1);

                            filler.fill(
                                    last.getValue(),
                                    current.getValue(),
                                    next.getValue(),
                                    last.getKey(),
                                    current.getKey(),
                                    next.getKey(),
                                    placer
                            );
                            image.setColor(current.getKey(), 0, current.getValue().abgr());
                        }
                        image.untrack();
                        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(image));
                        return identifier;
                    } catch (Exception e) {
                        RuntimeException runtime = new RuntimeException("Expetion parsing Material " + material);
                        runtime.addSuppressed(e);
                        throw runtime;
                    }

                    /*Path path = Path.of("miapi_dev").resolve("material_" + material + "_palette.png");
                    try {
                        image.writeTo(path);
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                    }*/

                }
                throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Not a JSON object -> " + json);
            });
        }
    }
}