package li.cil.oc.client.gui

import li.cil.oc.common.menu
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.text.ITextComponent

class Charger(state: menu.Charger, playerInventory: PlayerInventory, name: ITextComponent)
  extends DynamicGuiContainer(state, playerInventory, name) {
}
