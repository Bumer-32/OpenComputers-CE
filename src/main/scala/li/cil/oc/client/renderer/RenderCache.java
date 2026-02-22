package li.cil.oc.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RenderCache implements MultiBufferSource {
    public static class DrawEntry {
        private final RenderType type;
        private final BufferBuilder.DrawState state;
        private final ByteBuffer data;

        public DrawEntry(RenderType type, BufferBuilder.DrawState state, ByteBuffer data, boolean copy) {
            this.type = type;
            this.state = state;
            if (copy) {
                ByteBuffer temp = ByteBuffer.allocateDirect(data.remaining());
                temp.order(data.order());
                temp.put(data);
                ((Buffer) temp).flip();
                this.data = temp;
            } else {
                this.data = data;
            }
        }

        public RenderType type() { return type; }
        public BufferBuilder.DrawState state() { return state; }
        public ByteBuffer data() { return data; }
    }

    private final List<DrawEntry> cached = new ArrayList<>();
    private RenderType activeType;
    private BufferBuilder activeBuilder;

    public RenderCache() {}

    public boolean isEmpty() { return cached.isEmpty(); }

    public void clear() {
        cached.clear();
    }

    private void flush(RenderType type) {
        if (type == activeType) {
            activeBuilder.end();
            Pair<BufferBuilder.DrawState, ByteBuffer> rendered = activeBuilder.popNextBuffer();

            if (rendered.getSecond().hasRemaining()) {
                cached.add(new DrawEntry(type, rendered.getFirst(), rendered.getSecond(), true));
            }
            activeType = null;
        }
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        if (activeType != null) {
            if (activeType == type) return activeBuilder;
            flush(activeType);
        }
        activeType = type;
        activeBuilder = Tesselator.getInstance().getBuilder();
        activeBuilder.begin(type.mode(), type.format());
        return activeBuilder;
    }

    public void finish() {
        if (activeType != null) flush(activeType);
    }

    public void render(PoseStack poseStack) {
        if (isEmpty()) return;

        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().last().pose().multiply(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        cached.forEach(entry -> {
            entry.type().setupRenderState();

            BufferBuilder.DrawState state = entry.state();
            VertexFormat format = state.format();
            long bufferAddress = MemoryUtil.memAddress(entry.data());
            int vertexSize = format.getVertexSize();
            List<VertexFormatElement> elements = format.getElements();

            for (int i = 0; i < elements.size(); ++i) {
                VertexFormatElement element = elements.get(i);
                element.setupBufferState(i, bufferAddress + format.getOffset(i), vertexSize);
            }

            GL11.glDrawArrays(
                    state.mode().asGLMode,
                    0,
                    state.vertexCount()
            );
            
            format.clearBufferState();
            entry.type().clearRenderState();
        });

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }
}