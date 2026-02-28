package li.cil.oc

import li.cil.oc.common.init.Items
import li.cil.oc.common.item.RedstoneCard
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.{CreativeModeTab, ItemStack}
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.registries.{DeferredRegister, RegistryObject}
import li.cil.oc.common.Tier
import li.cil.oc.integration.opencomputers.ModOpenComputers
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

object CreativeTab {
  val CREATIVE_TABS: DeferredRegister[CreativeModeTab] =
    DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OpenComputers.ID)

  val MAIN: RegistryObject[CreativeModeTab] = CREATIVE_TABS.register("main", () =>
    CreativeModeTab.builder()
      .title(Component.translatable(s"itemGroup.${OpenComputers.Name}"))
      .icon(() => api.Items.get(Constants.BlockName.CaseTier1).createItemStack(1))
      .build()
  )

  @SubscribeEvent
  def onBuildContents(event: BuildCreativeModeTabContentsEvent): Unit = {
    if (event.getTabKey == MAIN.getKey) {
      val itemsToAdd: NonNullList[ItemStack] = NonNullList.create()
      Items.decorateCreativeTab(itemsToAdd)

      itemsToAdd.forEach(stack => event.accept(stack))

      Items.descriptors.get(Constants.ItemName.RedstoneCardTier2).foreach { info =>
        if (ModOpenComputers.hasRedstoneCardT2) event.accept(info.createItemStack(1))
      }
    }
  }
}
