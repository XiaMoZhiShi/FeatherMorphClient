package xiamomc.morph.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.client.syncers.DisguiseSyncer;
import xiamomc.morph.network.commands.S2C.clientrender.*;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisguiseInstanceTracker extends MorphClientObject
{
    //region

    public static DisguiseInstanceTracker getInstance()
    {
        return instance;
    }

    private static DisguiseInstanceTracker instance;

    public DisguiseInstanceTracker()
    {
        instance = this;
    }

    //endregion

    @Resolved
    private ClientMorphManager manager;

    //region CommandHandling

    private final Map<Integer, String> trackingDisguises = new Object2ObjectArrayMap<>();

    public Map<Integer, String> getTrackingDisguises()
    {
        return new Object2ObjectArrayMap<>(trackingDisguises);
    }

    public void onSyncCommand(S2CRenderMapSyncCommand s2CRenderMapSyncCommand)
    {
        this.reset();

        var map = s2CRenderMapSyncCommand.getMap();
        trackingDisguises.putAll(map);
        map.forEach(this::addSyncerIfNotExist);
    }

    public void onAddCommand(S2CRenderMapAddCommand s2CRenderMapAddCommand)
    {
        if (!s2CRenderMapAddCommand.isValid()) return;

        var networkId = s2CRenderMapAddCommand.getPlayerNetworkId();

        var prevSyncer = getSyncerFor(networkId);
        if (prevSyncer != null)
            removeSyncer(prevSyncer);

        trackingDisguises.put(networkId, s2CRenderMapAddCommand.getMobId());
        addSyncerIfNotExist(networkId, s2CRenderMapAddCommand.getMobId());
    }

    public void onRemoveCommand(S2CRenderMapRemoveCommand s2CRenderMapRemoveCommand)
    {
        if (!s2CRenderMapRemoveCommand.isValid()) return;

        var id = s2CRenderMapRemoveCommand.getPlayerNetworkId();
        trackingDisguises.remove(id);

        var syncer = idSyncerMap.getOrDefault(id, null);
        if (syncer != null)
            this.removeSyncer(syncer);
    }

    public void onClearCommand(S2CRenderMapClearCommand s2CRenderMapClearCommand)
    {
        this.reset();
    }

    public void onMetaCommand(S2CRenderMapMetaCommand metaCommand)
    {
        var meta = metaCommand.getArgumentAt(0);

        if (meta == null)
        {
            logger.warn("Received S2CRenderMapMetaCommand with no meta! Not Processing...");
            logger.warn("Packet: " + metaCommand.buildCommand());
            return;
        }

        var networkId = meta.networkId;
        if (networkId == -1)
        {
            logger.warn("Received S2CRenderMapMetaCommand with -1 network id! Not Processing...");
            return;
        }

        var newMeta = ConvertedMeta.of(meta);
        var currentMeta = getMetaFor(networkId);

        if (newMeta != null)
            currentMeta.mergeFrom(newMeta);

        currentMeta.outdated = true;
        idMetaMap.put(networkId, currentMeta);
    }

    //endregion

    public void reset()
    {
        trackingDisguises.clear();

        var mapCopy = new Object2ObjectArrayMap<>(idSyncerMap);
        mapCopy.forEach((id, syncer) -> this.removeSyncer(syncer));

        idMetaMap.clear();
    }

    //region Meta Tracking

    private final Map<Integer, ConvertedMeta> idMetaMap = new HashMap<>();

    public ConvertedMeta getMetaFor(Entity entity)
    {
        return getMetaFor(entity.getId());
    }

    @NotNull
    public ConvertedMeta getMetaFor(int networkId)
    {
        var meta = idMetaMap.getOrDefault(networkId, null);

        return meta == null ? new ConvertedMeta() : meta;
    }

    //endregion

    //region Syncer Tracking

    private final Map<Integer, DisguiseSyncer> idSyncerMap = new Object2ObjectArrayMap<>();

    public List<DisguiseSyncer> getAllSyncer()
    {
        return new ObjectArrayList<>(idSyncerMap.values());
    }

    public void removeSyncer(DisguiseSyncer targetSyncer)
    {
        targetSyncer.dispose();
        var optional = idSyncerMap.entrySet().stream()
                .filter(e -> e.getValue().equals(targetSyncer))
                .findFirst();

        logger.debug("Removing syncer" + targetSyncer + " :: get " + optional);

        if (optional.isPresent())
        {
            idSyncerMap.remove(optional.get().getKey());
        }
        else
        {
            logger.warn("Trying to remove an DisguiseSyncer that is not in the list?!");
            Thread.dumpStack();
        }
    }

    @Nullable
    public DisguiseSyncer getSyncerFor(AbstractClientPlayerEntity player)
    {
        return idSyncerMap.getOrDefault(player.getId(), null);
    }

    @Nullable
    public DisguiseSyncer getSyncerFor(int networkId)
    {
        return idSyncerMap.getOrDefault(networkId, null);
    }

    //endregion

    @Nullable
    public DisguiseSyncer addSyncerIfNotExist(int networkId, String did)
    {
        if (idSyncerMap.containsKey(networkId)) return idSyncerMap.get(networkId);

        var world = MinecraftClient.getInstance().world;
        var entity = world.getEntityById(networkId);

        if (!(entity instanceof AbstractClientPlayerEntity player)) return null;

        var syncer = manager.getSyncerFor(player, did, networkId);
        idSyncerMap.put(networkId, syncer);

        return syncer;
    }

/*

    @Nullable
    public DisguiseSyncer refreshSyncer(Entity entity)
    {
        if (!(entity instanceof AbstractClientPlayerEntity player)) return null;

        var networkId = entity.getId();

        var tracking = trackingDisguises.getOrDefault(networkId, "no");
        if (tracking.equals("no")) return null;

        var prevSyncer = idSyncerMap.getOrDefault(networkId, null);

        if (prevSyncer != null)
            this.removeSyncer(prevSyncer);

        var syncer = manager.getSyncerFor(player, tracking, player.getId());
        idSyncerMap.put(networkId, syncer);

        return syncer;
    }

*/

    @Nullable
    public DisguiseSyncer addSyncerIfNotExist(Entity entity)
    {
        var networkId = entity.getId();
        if (idSyncerMap.containsKey(networkId)) return idSyncerMap.get(networkId);

        var tracking = trackingDisguises.getOrDefault(networkId, "no");

        if (tracking.equals("no")) return null;
        if (!(entity instanceof AbstractClientPlayerEntity player)) return null;

        var syncer = manager.getSyncerFor(player, tracking, player.getId());
        idSyncerMap.put(networkId, syncer);

        return syncer;
    }
}
