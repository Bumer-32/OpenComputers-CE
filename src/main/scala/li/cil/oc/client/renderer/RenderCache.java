package li.cil.oc.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import li.cil.oc.OpenComputers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RenderCache implements MultiBufferSource {

    // ------------------------------------------------------------------ //
    // DrawEntry
    // ------------------------------------------------------------------ //

    public static class DrawEntry implements AutoCloseable {
        private final RenderType type;
        private final VertexBuffer vertexBuffer;

        public DrawEntry(RenderType type, BufferBuilder builder) {
            this.type = type;
            this.vertexBuffer = new VertexBuffer();
            builder.end();
            this.vertexBuffer.bind();
            this.vertexBuffer.upload(builder);
            VertexBuffer.unbind();
        }

        public RenderType type() { return type; }
        public VertexBuffer vertexBuffer() { return vertexBuffer; }

        @Override
        public void close() {
            vertexBuffer.close();
        }
    }

    private final List<DrawEntry> cached = new ArrayList<>();
    private RenderType activeType;
    private BufferBuilder activeBuilder;

    public RenderCache() {}

    public boolean isEmpty() { return cached.isEmpty(); }

    public void clear() {
        cached.forEach(DrawEntry::close);
        cached.clear();
    }

    private void flush(RenderType type) {
        if (type == activeType) {
            activeBuilder.end();
            cached.add(new DrawEntry(type, activeBuilder));
            activeType = null;
            activeBuilder = null;
        }
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        if (activeType != null) {
            if (activeType == type) return activeBuilder;
            flush(activeType);
        }
        activeType = type;
        activeBuilder = new BufferBuilder(256);
        activeBuilder.begin(type.mode(), type.format());
        return activeBuilder;
    }

    public void finish() {
        if (activeType != null) flush(activeType);
    }

    public void render(PoseStack poseStack) {
        if (isEmpty()) return;

        Matrix4f modelView = poseStack.last().pose();
        Matrix4f projection = RenderSystem.getProjectionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (DrawEntry entry : cached) {
            entry.type().setupRenderState();
            ShaderInstance shader = RenderSystem.getShader();

            if (shader == null) {
                shader = GameRenderer.getPositionColorTexShader();
                RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            }

            entry.vertexBuffer().bind();
            entry.vertexBuffer().drawWithShader(modelView, projection, shader);
            entry.type().clearRenderState();
        }

        VertexBuffer.unbind();
    }
}