package li.cil.oc.client.gui

import li.cil.oc.common.menu
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent

class Tablet(state: menu.Tablet, playerInventory: PlayerInventory, name: ITextComponent)
  extends DynamicGuiContainer(state, playerInventory, name)
  with traits.LockedHotbar[menu.Tablet] {

  override def lockedStack = inventoryContainer.stack
}
