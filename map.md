1.16 (旧),1.18.2 (Mojmap / 新FQN)
World,net.minecraft.world.level.Level
IBlockReader,net.minecraft.world.level.BlockGetter
BlockPos,net.minecraft.core.BlockPos
Direction,net.minecraft.core.Direction
Vector3d,net.minecraft.world.phys.Vec3
ResourceLocation,net.minecraft.resources.ResourceLocation
NonNullList,net.minecraft.core.NonNullList
AbstractBlock.Properties,net.minecraft.world.level.block.state.BlockBehaviour$Properties
BlockState,net.minecraft.world.level.block.state.BlockState
StateContainer,net.minecraft.world.level.block.state.StateDefinition
Item.Properties,net.minecraft.world.item.Item$Properties
group(CreativeTab),tab(CreativeTab) (Properties内のメソッド)
maxStackSize(n),stacksTo(n) (Properties内のメソッド)
setRegistryName(...),廃止 (DeferredRegister または Event経由での登録へ)
setUnlocalizedName(...),廃止 (getDescriptionId またはコンストラクタへ)
ToolType,廃止 (JSONによるタグシステム tags/items/tools へ移行)
MatrixStack,com.mojang.blaze3d.vertex.PoseStack
Screen,net.minecraft.client.gui.screens.Screen
AbstractGui,net.minecraft.client.gui.GuiComponent
TextFieldWidget,net.minecraft.client.gui.components.EditBox
Button.IPressable,net.minecraft.client.gui.components.Button$OnPress
ITextComponent,net.minecraft.network.chat.Component
StringTextComponent,net.minecraft.network.chat.TextComponent
StringTextComponent.EMPTY,TextComponent.EMPTY
Rectangle2d,net.minecraft.client.renderer.Rect2i
INestedGuiEventHandler,net.minecraft.client.gui.components.events.ContainerEventHandler
KeyBinding,net.minecraft.client.KeyMapping
addButton(widget),addRenderableWidget(widget)
passEvents,廃止
"RenderSystem.color3f(r,g,b)","RenderSystem.setShaderColor(r,g,b,1.0f)"
"RenderSystem.color4f(r,g,b,a)","RenderSystem.setShaderColor(r,g,b,a)"
textureManager.bind(loc),"RenderSystem.setShaderTexture(0, loc)"
Tessellator,com.mojang.blaze3d.vertex.Tesselator (※あっきーさん環境: L1つ)
DefaultVertexFormats,com.mojang.blaze3d.vertex.DefaultVertexFormat (※あっきーさん環境: sなし)
GL11.GL_QUADS,com.mojang.blaze3d.vertex.VertexFormat$Mode.QUADS
getBlitOffset,getBlitOffset() (メソッド呼び出しへ)
ActionResultType,net.minecraft.world.InteractionResult
Hand,net.minecraft.world.InteractionHand
BlockRayTraceResult,net.minecraft.world.phys.BlockHitResult
BlockItemUseContext,net.minecraft.world.item.context.UseOnContext
"mouseScrolled(x, y, d)","mouseScrolled(x, y, d)