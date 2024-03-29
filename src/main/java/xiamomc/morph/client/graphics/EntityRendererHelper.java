package xiamomc.morph.client.graphics;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiamomc.morph.client.MorphClient;
import xiamomc.morph.client.graphics.color.ColorUtils;
import xiamomc.morph.client.graphics.color.MaterialColors;
import xiamomc.morph.client.syncers.ClientDisguiseSyncer;

import java.util.Map;

public class EntityRendererHelper
{
    public EntityRendererHelper()
    {
        instance = this;
    }

    public static EntityRendererHelper instance;

    public static boolean doRenderRealName = false;

    private final int textColor = MaterialColors.Orange500.getColor();
    public final int textColorTransparent = ColorUtils.forOpacity(MaterialColors.Orange500, 0).getColor();

    @Nullable
    public final Map.Entry<Integer, String> getEntry(Integer id)
    {
        return MorphClient.getInstance().morphManager.playerMap.entrySet().stream()
                .filter(set -> id.equals(set.getKey()))
                .findFirst().orElse(null);
    }

    public final void renderRevealNameIfPossible(EntityRenderDispatcher dispatcher,
                                           Entity renderingEntity, TextRenderer textRenderer,
                                           MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                           @Nullable CallbackInfo ci, int light)
    {
        if (!doRenderRealName) return;

        Integer id = renderingEntity.getId();

        var entrySet = getEntry(id);
        if (entrySet == null) return;

        String text = entrySet.getValue();
        if (text.equals(renderingEntity.getName().getString())) return;

        var syncer = ClientDisguiseSyncer.getCurrentInstance();
        if (syncer != null && renderingEntity != ClientDisguiseSyncer.getCurrentInstance().getDisguiseInstance())
            renderingEntity.ignoreCameraFrustum = true;

        renderLabelOnTop(matrices, vertexConsumers, textRenderer, renderingEntity, dispatcher, text);
    }

    public void renderLabelOnTop(MatrixStack matrices, VertexConsumerProvider vertexConsumers, TextRenderer textRenderer, Entity entity, EntityRenderDispatcher dispatcher, String text)
    {
        matrices.push();

        var exOffset = (entity.hasCustomName() || entity instanceof OtherClientPlayerEntity) ? 0.25f : -0.25f;

        matrices.translate(0, entity.getNameLabelHeight() +exOffset, 0);
        matrices.multiply(dispatcher.getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);

        float clientBackgroundOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int finalColor = (int)(clientBackgroundOpacity * 255.0f) << 24;

        var positionMatrix = matrices.peek().getPositionMatrix();
        var x = textRenderer.getWidth(text) / -2f;

        //文字
        textRenderer.draw(text, x, 0,
                textColor, false,
                positionMatrix, vertexConsumers,
                TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);

        //背景
        textRenderer.draw(text, x, 0,
                textColorTransparent, false,
                positionMatrix, vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH, finalColor, LightmapTextureManager.MAX_LIGHT_COORDINATE);

        matrices.pop();
    }
}
