package li.cil.oc.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.Textures
import li.cil.oc.common
import li.cil.oc.common.menu.ComponentSlot
import li.cil.oc.common.menu.AbstractMenu
import li.cil.oc.integration.util.ItemSearch
import li.cil.oc.util.RenderState
import li.cil.oc.util.StackOption
import li.cil.oc.util.StackOption._
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.{AbstractContainerMenu, Slot}
import net.minecraft.network.chat.Component

abstract class DynamicGuiContainer[C <: AbstractContainerMenu](container: C, inv: Inventory, title: Component)
  extends CustomGuiContainer(container, inv, title) {

  protected var hoveredStackNEI: StackOption = EmptyStack

  override protected def init(): Unit = {
    super.init()
    inventoryLabelY = imageHeight - 96 + 2
  }

  // 1.20.1: PoseStack → GuiGraphics に統一
  protected def drawSecondaryForegroundLayer(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int): Unit = {}

  override protected def renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int): Unit = {
    super.renderLabels(guiGraphics, mouseX, mouseY)
    RenderState.pushAttrib()

    drawSecondaryForegroundLayer(guiGraphics, mouseX, mouseY)

    for (slot <- 0 until menu.slots.size()) {
      drawSlotHighlight(guiGraphics, menu.getSlot(slot))
    }

    RenderState.popAttrib()
  }

  protected def drawSecondaryBackgroundLayer(guiGraphics: GuiGraphics): Unit = {}

  override protected def renderBg(guiGraphics: GuiGraphics, dt: Float, mouseX: Int, mouseY: Int): Unit = {
    RenderSystem.setShaderColor(1, 1, 1, 1)
    guiGraphics.blit(Textures.GUI.Background, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    drawSecondaryBackgroundLayer(guiGraphics)

    RenderState.makeItBlend()
    RenderSystem.setShader(() => GameRenderer.getPositionColorShader)

    drawInventorySlots(guiGraphics)
  }

  protected def drawInventorySlots(guiGraphics: GuiGraphics): Unit = {
    val stack = guiGraphics.pose()
    stack.pushPose()
    stack.translate(leftPos, topPos, 0)
    RenderSystem.disableDepthTest()
    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    for (slot <- 0 until menu.slots.size()) {
      drawSlotInventory(guiGraphics, menu.getSlot(slot))
    }
    RenderSystem.enableDepthTest()
    stack.popPose()
    RenderState.makeItBlend()
  }

  override def render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, dt: Float): Unit = {
    hoveredStackNEI = ItemSearch.hoveredStack(this, mouseX, mouseY)
    super.render(guiGraphics, mouseX, mouseY, dt)
  }

  protected def drawSlotInventory(guiGraphics: GuiGraphics, slot: Slot): Unit = {
    RenderSystem.enableBlend()
    slot match {
      case component: ComponentSlot if component.slot == common.Slot.None || component.tier == common.Tier.None =>
        if (!slot.hasItem && slot.x >= 0 && slot.y >= 0 && component.tierIcon != null) {
          drawDisabledSlot(guiGraphics, component)
        }
      case _ =>
        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(0, 0, 1)
        if (!isInPlayerInventory(slot)) {
          drawSlotBackground(guiGraphics, slot.x - 1, slot.y - 1)
        }
        slot match {
          case component: ComponentSlot if !slot.hasItem =>
            if (component.tierIcon != null) {
              guiGraphics.blit(component.tierIcon, slot.x, slot.y, 0, 0, 16, 16, 16, 16)
            }
            if (component.hasBackground) {
              guiGraphics.blit(component.getBackgroundLocation, slot.x, slot.y, 0, 0, 16, 16, 16, 16)
            }
          case _ =>
        }
        guiGraphics.pose().popPose()
    }
    RenderSystem.disableBlend()
  }

  protected def drawSlotHighlight(guiGraphics: GuiGraphics, slot: Slot): Unit = {
    if (minecraft.player.containerMenu.getCarried.isEmpty) slot match {
      case component: ComponentSlot if component.slot == common.Slot.None || component.tier == common.Tier.None => // Ignore.
      case _ =>
        val currentIsInPlayerInventory = isInPlayerInventory(slot)
        val drawHighlight = hoveredSlot match {
          case hovered: Slot =>
            val hoveredIsInPlayerInventory = isInPlayerInventory(hovered)
            (currentIsInPlayerInventory != hoveredIsInPlayerInventory) &&
              ((currentIsInPlayerInventory && slot.hasItem && isSelectiveSlot(hovered) && hovered.mayPlace(slot.getItem)) ||
                (hoveredIsInPlayerInventory && hovered.hasItem && isSelectiveSlot(slot) && slot.mayPlace(hovered.getItem)))
          case _ => hoveredStackNEI match {
            case SomeStack(s) => !currentIsInPlayerInventory && isSelectiveSlot(slot) && slot.mayPlace(s)
            case _ => false
          }
        }
        if (drawHighlight) {
          guiGraphics.pose().pushPose()
          guiGraphics.pose().translate(0, 0, 100)
          guiGraphics.fillGradient(
            slot.x, slot.y,
            slot.x + 16, slot.y + 16,
            0x80FFFFFF, 0x80FFFFFF)
          guiGraphics.pose().popPose()
        }
    }
  }

  private def isSelectiveSlot(slot: Slot): Boolean = slot match {
    case component: ComponentSlot => component.slot != common.Slot.Any && component.slot != common.Slot.Tool
    case _ => false
  }

  protected def drawDisabledSlot(guiGraphics: GuiGraphics, slot: ComponentSlot): Unit = {
    RenderSystem.setShaderColor(1, 1, 1, 1)
    guiGraphics.blit(slot.tierIcon, slot.x, slot.y, 0, 0, 16, 16, 16, 16)
  }

  protected def drawSlotBackground(guiGraphics: GuiGraphics, x: Int, y: Int): Unit = {
    RenderSystem.setShaderColor(1, 1, 1, 1)
    guiGraphics.blit(Textures.GUI.Slot, x, y, 0, 0, 18, 18)
  }

  private def isInPlayerInventory(slot: Slot): Boolean = container match {
    case player: AbstractMenu => slot.container == player.playerInventory
    case _ => false
  }
}