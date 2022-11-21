package xiamo.morph.client.graphics;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class DrawableText implements Drawable
{
    private static final Text defaultText = Text.literal("");
    private static final TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

    private Text text = defaultText;

    private int screenX;
    private int screenY;

    public int getScreenX()
    {
        return screenX;
    }

    public void setScreenX(int x)
    {
        this.screenX = x;
    }

    public int getScreenY()
    {
        return screenY;
    }

    public void setScreenY(int y)
    {
        this.screenY = y;
    }

    private int width;
    private int height;

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int w)
    {
        this.width = w;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int h)
    {
        this.height = h;
    }

    public void setText(Text text)
    {
        this.text = text;
    }

    public void setText(String text)
    {
        this.text = Text.literal(text);
    }

    public Text getText()
    {
        return text;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        var in = renderer.draw(matrices, text, this.screenX, this.screenY, 0x00ffffff);

        renderer.draw(matrices, text, this.screenX - in - 5, this.screenY, 0xffffffff);
    }
}
