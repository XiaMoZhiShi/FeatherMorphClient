package xiamomc.morph.client.syncers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.client.EntityCache;

public class OtherClientDisguiseSyncer extends DisguiseSyncer
{
    public OtherClientDisguiseSyncer(AbstractClientPlayerEntity clientPlayer, String morphId, int networkId)
    {
        super(clientPlayer, morphId, networkId);
    }

    @Override
    protected void syncPosition()
    {
        if (disguiseInstance == null) return;

        var playerPos = bindingPlayer.getPos();

        //暂时先这样
        disguiseInstance.setPosition(playerPos);
    }

    @Override
    protected void onDispose()
    {
        if (disguiseInstance != null)
            bindingPlayer.setPosition(disguiseInstance.getPos());
    }

    private EntityCache localCache;

    @Override
    protected @NotNull EntityCache getEntityCache()
    {
        if (localCache == null) localCache = new EntityCache();
        else if (localCache.disposed() && !this.disposed())
        {
            logger.warn("A non-disposed DisguiseSyncer '%s' has a disposed EntityCache?!");
            logger.warn("Creating a new instance now...");
            Thread.dumpStack();

            localCache = new EntityCache();
        }

        return localCache;
    }

    @Override
    public void syncTick()
    {
        if (disguiseInstance == null || disposed()) return;

        baseSync();
        syncPosition();

        // syncer可能会在baseSync后被处理
        if (disposed())
            return;

        if (disguiseInstance.isGlowing() != bindingPlayer.isGlowing())
            disguiseInstance.setGlowing(bindingPlayer.isGlowing());

        if (!dimensionsRefreshed)
        {
            bindingPlayer.calculateDimensions();
            dimensionsRefreshed = true;
        }
    }

    private boolean dimensionsRefreshed;

    @Override
    public void syncDraw()
    {
        syncYawPitch();
    }

    @Override
    protected void initialSync()
    {
    }
}
