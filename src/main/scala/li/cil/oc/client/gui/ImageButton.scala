package li.cil.oc.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.{DefaultVertexFormat, Tesselator, VertexFormat}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class ImageButton(xPos: Int, yPos: Int, w: Int, h: Int,
                  handler: Button.OnPress,
                  val image: ResourceLocation = null,
                  text: Component = Component.empty(),
                  val canToggle: Boolean = false,
                  val textColor: Int = 0xE0E0E0,
                  val textDisabledColor: Int = 0xA0A0A0,
                  val textHoverColor: Int = 0xFFFFA0,
                  val textIndent: Int = -1,
                  val textureWidth: Int = -1,
                  val textureHeight: Int = -1) extends Button(xPos, yPos, w, h, text, handler, _ => Component.empty()) {

  var toggled = false
  var hoverOverride = false

  override def renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    if (visible) {
      val stack = graphics.pose()
      if (image != null) {
        RenderSystem.setShaderTexture(0, image)
      }
      RenderSystem.setShaderColor(1, 1, 1, 1)
      isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height

      val x0 = x.toFloat
      val x1 = (x + width).toFloat
      val y0 = y.toFloat
      val y1 = (y + height).toFloat

      val drawHover = hoverOverride || (isHovered && active)

      val t = Tesselator.getInstance
      val r = t.getBuilder

      if (image != null) {
        val (ru0, ru1, rv0, rv1) = if (textureWidth > 0 && textureHeight > 0) {
          val texW = textureWidth.toFloat
          val texH = textureHeight.toFloat
          val tu0 = if (toggled) w.toFloat / texW else 0f
          val tu1 = tu0 + w.toFloat / texW
          val tv0 = if (drawHover) h.toFloat / texH else 0f
          val tv1 = tv0 + h.toFloat / texH
          (tu0, tu1, tv0, tv1)
        } else {
          val u0 = if (toggled) 0.5f else 0f
          val u1 = u0 + (if (canToggle) 0.5f else 1f)
          val v0 = if (drawHover) 0.5f else 0f
          val v1 = v0 + 0.5f
          (u0, u1, v0, v1)
        }
        
        RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()

        r.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        r.vertex(stack.last.pose, x0, y0, 0).uv(ru0, rv0).endVertex() // 左上 (LT)
        r.vertex(stack.last.pose, x1, y0, 0).uv(ru1, rv0).endVertex() // 右上 (RT)
        r.vertex(stack.last.pose, x1, y1, 0).uv(ru1, rv1).endVertex() // 右下 (RB)
        r.vertex(stack.last.pose, x0, y1, 0).uv(ru0, rv1).endVertex() // 左下 (LB)
        t.end()
        RenderSystem.disableBlend()
      } else {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        val alpha = if (drawHover) 0.8f else 0.4f
        RenderSystem.setShader(() => GameRenderer.getPositionColorShader)
        r.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
        r.vertex(stack.last.pose, x0, y0, 0).color(1f, 1f, 1f, alpha).endVertex() // 左上
        r.vertex(stack.last.pose, x1, y0, 0).color(1f, 1f, 1f, alpha).endVertex() // 右上
        r.vertex(stack.last.pose, x1, y1, 0).color(1f, 1f, 1f, alpha).endVertex() // 右下
        r.vertex(stack.last.pose, x0, y1, 0).color(1f, 1f, 1f, alpha).endVertex() // 左下
        t.end()
        RenderSystem.disableBlend()
      }

      if (getMessage != Component.empty()) {
        val color =
          if (!active) textDisabledColor
          else if (hoverOverride || isHovered) textHoverColor
          else textColor
        val mc = Minecraft.getInstance
        if (textIndent >= 0) graphics.drawString(mc.font, getMessage, textIndent + x, y + (height - 8) / 2, color)
        else graphics.drawCenteredString(mc.font, getMessage, x + width / 2, y + (height - 8) / 2, color)
      }
    }
  }
}