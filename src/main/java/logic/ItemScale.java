package logic;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.powerscale.Config;
import org.slf4j.Logger;

public class ItemScale {
    static final Logger LOGGER = LogUtils.getLogger();
    private static Config.ItemModifier[] getModifiersForItem(Identifier item, Identifier dimension) {
        // TODO
        return new Config.ItemModifier[]{
                new Config.ItemModifier("generic.attack_damage", 3F)
        };
    }

    private static void applyModifiersForItemStack(Identifier itemId, ItemStack itemStack, Config.ItemModifier[] modifiers) {
        for (Config.ItemModifier modifier: modifiers) {
            try {
                System.out.println("Applying A extra attack damage to " + itemId);
                var attribute = Registry.ATTRIBUTE.get(new Identifier(modifier.attribute));
                if(!attribute.equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
                    continue;
                }
                System.out.println("Applying extra B attack damage to " + itemId);
                var attributeModifiers = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND); // TODO - Slot???
                var attributeModifierCollection = attributeModifiers.get(attribute);
                for (EntityAttributeModifier attributeModifier: attributeModifierCollection) {
                    System.out.println("Applying extra C attack damage to " + itemId);
                    itemStack.addAttributeModifier(
                            attribute,
                            new EntityAttributeModifier(
                                    attributeModifier.getName(),
                                    attributeModifier.getValue() * modifier.value,
                                    attributeModifier.getOperation()
                            ),
                            EquipmentSlot.MAINHAND
                    );
                }
            } catch (Exception e) {
                LOGGER.error("Failed to apply modifier to: " + itemId + " modifier:" + modifier);
                LOGGER.error("Reason: " + e.getMessage());
            }
        }
//        itemStack.addAttributeModifier(
//                EntityAttributes.GENERIC_ATTACK_DAMAGE,
//                new EntityAttributeModifier(
//                        "asd",
//                        20,
//                        EntityAttributeModifier.Operation.ADDITION),
//                EquipmentSlot.MAINHAND
//        );
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            System.out.println("LootTableEvents A " + id);
            // var lootTable = lootManager.getTable(id);
            LootFunction function = new LootFunction() {
                @Override
                public LootFunctionType getType() {
                    return LootFunctionTypes.SET_ATTRIBUTES;
                }

                @Override
                public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
                    var itemId = Registry.ITEM.getId(itemStack.getItem());
                    var dimensionId = lootContext.getWorld().getRegistryKey().getValue();
                    System.out.println("Checking apply for: " + itemId + " in dimension: " + dimensionId);
                    if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof ArmorItem) {
//                        var itemId = Registry.ITEM.getId(itemStack.getItem());
//                        var dimensionId = lootContext.getWorld().getRegistryKey().getValue();
                        applyModifiersForItemStack(itemId, itemStack, getModifiersForItem(itemId, dimensionId));
                    }
                    return itemStack;
                }
            };
            tableBuilder.apply(function);
        });
    }
}
