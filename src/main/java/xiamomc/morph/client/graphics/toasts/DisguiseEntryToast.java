package xiamomc.morph.client.graphics.toasts;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import xiamomc.morph.client.graphics.EntityDisplay;
import xiamomc.morph.client.graphics.color.MaterialColors;
import xiamomc.pluginbase.Annotations.Initializer;

public class DisguiseEntryToast extends LinedToast
{
    private final String rawIdentifier;

    private final boolean isGrant;

    public DisguiseEntryToast(String rawIdentifier, boolean isGrant)
    {
        if (isGrant) grantInstance = this;
        else lostInstance = this;

        this.rawIdentifier = rawIdentifier;
        this.isGrant = isGrant;

        this.entityDisplay = new EntityDisplay(rawIdentifier);
        entityDisplay.x = 512;

        entityDisplay.postEntitySetup = () -> trimDisplay(entityDisplay.getDisplayName());
    }

    @Initializer
    private void load()
    {
        entityDisplay.x = Math.round((float) this.getWidth() / 8);
        entityDisplay.y = this.getHeight() / 2 + 7;

        if (rawIdentifier.equals("minecraft:horse"))
        {
            entityDisplay.x -= 1;
            entityDisplay.y += 2;
        }
        else if (rawIdentifier.equals("minecraft:axolotl"))
        {
            entityDisplay.x -= 2;
        }

        title = Text.translatable("text.morphclient.toast.disguise_%s".formatted(isGrant ? "grant" : "lost"));
        this.setLineColor(isGrant ? MaterialColors.Green500 : MaterialColors.Amber500);
    }

    private void trimDisplay(StringVisitable text)
    {
        this.addSchedule(() ->
        {
            var targetMultiplier = 0.65;
            var toDisplay = textRenderer.trimToWidth(text, (int)Math.round(this.getWidth() * targetMultiplier));
            var trimmed = !toDisplay.getString().equals(text.getString());

            this.display = Text.literal(toDisplay.getString() + (trimmed ? "..." : ""));
        });
    }

    private Text display;

    private final EntityDisplay entityDisplay;

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private static DisguiseEntryToast grantInstance;
    private static DisguiseEntryToast lostInstance;

    @Override
    protected void postBackgroundDrawing(MatrixStack matrices, ToastManager manager, long startTime)
    {
        super.postBackgroundDrawing(matrices, manager, startTime);

        // Push a new entry to allow us to do some tricks
        matrices.push();

        // Draw entity
        // Make entity display more pixel-perfect
        matrices.translate(0, 0.5, 0);
        entityDisplay.render(matrices, -30, 0);

        // Pop back
        matrices.pop();

        description = display == null ? entityDisplay.getDisplayName() : display;
    }
}
