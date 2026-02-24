package li.cil.oc.server.loot;

import li.cil.oc.OpenComputers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LootFunctions {
    public static final ResourceLocation DYN_ITEM_DATA = new ResourceLocation(OpenComputers.ID(), "item_data");
    public static final ResourceLocation DYN_VOLATILE_CONTENTS = new ResourceLocation(OpenComputers.ID(), "volatile_contents");

    private static final DeferredRegister<LootItemFunctionType> DR =
            DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, OpenComputers.ID());

    public static final RegistryObject<LootItemFunctionType> SET_COLOR =
            DR.register("set_color", () -> new LootItemFunctionType(new SetColor.Serializer()));

    public static final RegistryObject<LootItemFunctionType> COPY_COLOR =
            DR.register("copy_color", () -> new LootItemFunctionType(new CopyColor.Serializer()));

    public static void init(IEventBus bus) {
        DR.register(bus);
    }

    private LootFunctions() {}
}