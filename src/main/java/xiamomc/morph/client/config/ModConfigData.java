package xiamomc.morph.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import xiamomc.morph.client.MorphClient;
import xiamomc.morph.client.graphics.transforms.easings.Easing;

@Config(name = "morphclient")
public class ModConfigData implements ConfigData
{
    public boolean alwaysShowPreviewInInventory = false;

    public boolean allowClientView = true;

    public boolean verbosePackets = false;

    public boolean displayDisguiseOnHud = true;

    public boolean changeCameraHeight = false;

    public Easing easing = Easing.OutQuint;

    public int duration = 450;

    public float scrollSpeed = 1f;

    public boolean displayGrantRevokeToast = true;
    public boolean displayQuerySetToast = false;
    public boolean displayToastProgress = false;

    public boolean clientViewVisible()
    {
        return MorphClient.getInstance().morphManager.selfVisibleEnabled.get() && allowClientView;
    }
}
