package xiamomc.morph.client.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiamomc.morph.client.graphics.transforms.Transformer;
import xiamomc.morph.client.utilties.MinecraftClientMixinUtils;
import xiamomc.morph.client.utilties.Screens;

import java.io.File;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
    @Shadow @Final private YggdrasilAuthenticationService authenticationService;

    @Shadow @Final public File runDirectory;

    @Shadow @Nullable public Screen currentScreen;

    @Inject(method = "render", at = @At("RETURN"))
    private void featherMorph$onClientRender(boolean tick, CallbackInfo ci)
    {
        Transformer.onClientRenderEnd(MinecraftClient.getInstance());
    }

    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void featherMorph$onJoinServer(ClientWorld world, CallbackInfo ci)
    {
        MinecraftClientMixinUtils.setApiService(this.authenticationService, this.runDirectory);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void featherMorph$onSetScreen(Screen screenNext, CallbackInfo ci)
    {
        Screens.getInstance().onChange(this.currentScreen, screenNext);
    }
}
