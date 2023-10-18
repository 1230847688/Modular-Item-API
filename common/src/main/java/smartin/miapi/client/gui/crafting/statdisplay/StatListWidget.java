package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.modules.properties.*;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class StatListWidget extends InteractAbleWidget {
    private static final List<InteractAbleWidget> statDisplays = new ArrayList<>();
    private static final List<StatWidgetSupplier> statWidgetSupplier = new ArrayList<>();
    private final BoxList boxList;
    private TransformableWidget transformableWidget;
    private TransformableWidget hoverText;
    private ItemStack original = ItemStack.EMPTY;
    private ItemStack compareTo = ItemStack.EMPTY;

    public static void setup() {
        statDisplays.clear();
        addStatDisplay(new FlattenedListPropertyStatDisplay<>(
                PotionEffectProperty.property,
                stk -> statTranslation("tipped"))
                .withLimitedDescSize(350).withArrowsInTitle());
        addStatDisplay(AttributeSingleDisplay
                .builder(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                .setTranslationKey("damage")
                .setDefault(1)
                .setMax(13.0).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(EntityAttributes.GENERIC_ATTACK_SPEED)
                .setTranslationKey("attack_speed")
                .setDefault(4)
                .setMax(4.0).build());
        addStatDisplay(new DpsStatDisplay());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.BACK_STAB)
                .setTranslationKey("back_stab")
                .setDefault(1)
                .setFormat("##.#")
                .setMax(5).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.SHIELD_BREAK)
                .setTranslationKey("shield_break")
                .setDefault(0)
                .setFormat("##.#")
                .setMax(5).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.REACH)
                .setTranslationKey("reach")
                .setDefault(0)
                .setFormat("##.#")
                .setMax(2).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.ATTACK_RANGE)
                .setTranslationKey("attack_range")
                .setDefault(0)
                .setFormat("##.#")
                .setMax(2).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_DAMAGE)
                .setTranslationKey("projectile_damage")
                .setFormat("##.##")
                .setDefault(0)
                .setMax(10).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_SPEED)
                .setTranslationKey("projectile_speed")
                .setFormat("##.##")
                .setDefault(0)
                .setMin(-3)
                .setMax(10).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_ACCURACY)
                .setTranslationKey("projectile_accuracy")
                .setFormat("##.##")
                .setDefault(0)
                .setMin(-2)
                .setMax(2).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_PIERCING)
                .setTranslationKey("projectile_piercing")
                .setFormat("##.##")
                .setMax(10).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER)
                .setTranslationKey("projectile_crit_multiplier")
                .setFormat("##.##")
                .setMax(10).build());
        addStatDisplay(MiningLevelStatDisplay
                .builder("pickaxe")
                .setAttribute(AttributeRegistry.MINING_SPEED_PICKAXE).build());
        addStatDisplay(MiningLevelStatDisplay
                .builder("axe")
                .setAttribute(AttributeRegistry.MINING_SPEED_AXE).build());
        addStatDisplay(MiningLevelStatDisplay
                .builder("shovel")
                .setAttribute(AttributeRegistry.MINING_SPEED_SHOVEL).build());
        addStatDisplay(MiningLevelStatDisplay
                .builder("hoe")
                .setAttribute(AttributeRegistry.MINING_SPEED_HOE).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(FlexibilityProperty.property)
                .setTranslationKey(FlexibilityProperty.KEY)
                .setMax(10)
                .build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(HealthPercentDamage.property)
                .setMax(50)
                .setTranslationKey(HealthPercentDamage.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(ArmorPenProperty.property)
                .setMin(-20)
                .setMax(50)
                .setTranslationKey(ArmorPenProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(BlockProperty.property)
                .setMax(50)
                .setTranslationKey(BlockProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(AirDragProperty.property)
                .setMax(1)
                .setTranslationKey(AirDragProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(WaterDragProperty.property)
                .setMax(1)
                .setTranslationKey(WaterDragProperty.KEY).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.BOW_DRAW_TIME)
                .setMax(30)
                .setMin(1)
                .setDefault(20)
                .setTranslationKey("bow_draw_time")
                .inverseNumber(true)
                .setFormat("##.##").build());

        addStatDisplay(SinglePropertyStatDisplay
                .builder(DurabilityProperty.property)
                .setMax(2000)
                .setFormat("##")
                .setTranslationKey(DurabilityProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(FracturingProperty.property)
                .setMax(50)
                .setTranslationKey(FracturingProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(FortuneProperty.property)
                .setMax(5)
                .setTranslationKey(FortuneProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(MendingProperty.property)
                .setMax(1)
                .setTranslationKey(MendingProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(ImmolateProperty.property)
                .setMax(4)
                .setTranslationKey(ImmolateProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(LeechingProperty.property)
                .setMax(2)
                .setTranslationKey(LeechingProperty.KEY).build());

        AttributeSingleDisplay.attributesWithDisplay.add(AttributeRegistry.MINING_SPEED_AXE);
        AttributeSingleDisplay.attributesWithDisplay.add(AttributeRegistry.MINING_SPEED_PICKAXE);
        AttributeSingleDisplay.attributesWithDisplay.add(AttributeRegistry.MINING_SPEED_HOE);
        AttributeSingleDisplay.attributesWithDisplay.add(AttributeRegistry.MINING_SPEED_SHOVEL);
        AttributeSingleDisplay.attributesWithDisplay.add(AttributeRegistry.ARMOR_CRUSHING);
        Registries.ATTRIBUTE.forEach(entityAttribute -> {
            if (!AttributeSingleDisplay.attributesWithDisplay.contains(entityAttribute)) {
                addStatDisplay(AttributeSingleDisplay
                        .builder(entityAttribute).build());
            }
        });
    }

    public StatListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        transformableWidget = new TransformableWidget(x, y, width, height, Text.empty());
        boxList = new BoxList(x, y, width, height, Text.empty(), new ArrayList<>());
        ScrollList list = new ScrollList(x, y, width, height, List.of(boxList));
        list.altDesign = true;
        list.alwaysEnableScrollbar = true;
        transformableWidget.addChild(list);
        transformableWidget.rawProjection = new Matrix4f();
        //transformableWidget.rawProjection.scale(0.5f, 0.5f, 0.5f);
        addChild(transformableWidget);
        hoverText = new TransformableWidget(x, y, width, height, Text.empty());
        hoverText.rawProjection = new Matrix4f().scale(0.667f, 0.667f, 0.667f);
        addChild(hoverText);
    }

    public static MutableText statTranslation(String statName) {
        return Text.translatable(Miapi.MOD_ID + ".stat." + statName);
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        Vector4f vector4f = transformableWidget.transFormMousePos(mouseX, mouseY);
        InteractAbleWidget hoverDisplay = null;
        for (Element children : boxList.children()) {
            if (children.isMouseOver(vector4f.x(), vector4f.y()) && (children instanceof SingleStatDisplay widget)) {
                InteractAbleWidget ableWidget = widget.getHoverWidget();
                if (ableWidget != null) {
                    hoverDisplay = ableWidget;
                }

            }
        }
        if (hoverDisplay != null && isMouseOver(mouseX, mouseY)) {
            float scale = 1.0f;
            hoverDisplay.setX((int) ((mouseX + 5) * (1 / scale)));
            hoverDisplay.setY((int) ((mouseY - hoverDisplay.getHeight() / 2 * scale) * (1 / scale)));
            hoverText.renderWidget(hoverDisplay, drawContext, mouseX, mouseY, delta);
        }
    }

    private <T extends InteractAbleWidget & SingleStatDisplay> void update() {
        List<ClickableWidget> widgets = new ArrayList<>();
        for (StatWidgetSupplier supplier : statWidgetSupplier) {
            List<T> statWidgets = supplier.currentList(original, compareTo);
            for (T statDisplay : statWidgets) {
                statDisplay.setHeight(statDisplay.getHeightDesired());
                statDisplay.setWidth(statDisplay.getWidthDesired());
                widgets.add(statDisplay);
            }
        }
        for (InteractAbleWidget statDisplay : statDisplays) {
            if (statDisplay instanceof SingleStatDisplay singleStatDisplay && (singleStatDisplay.shouldRender(original, compareTo))) {
                statDisplay.setHeight(singleStatDisplay.getHeightDesired());
                statDisplay.setWidth(singleStatDisplay.getWidthDesired());
                widgets.add(statDisplay);

            }
        }
        boxList.setWidgets(widgets, 0);
    }

    public interface StatWidgetSupplier {
        <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo);
    }

    public static <T extends InteractAbleWidget & SingleStatDisplay> void addStatDisplay(T statDisplay) {
        statDisplays.add(statDisplay);
    }

    public static void addStatDisplaySupplier(StatWidgetSupplier supplier) {
        statWidgetSupplier.add(supplier);
    }

    public void setOriginal(ItemStack original) {
        this.original = original;
        update();
    }

    public void setCompareTo(ItemStack compareTo) {
        this.compareTo = compareTo;
        update();
    }

    public interface TextGetter {
        Text resolve(ItemStack stack);
    }

    public interface MultiTextGetter {
        List<Text> resolve(ItemStack stack);
    }
}