package xiamomc.morph.client.graphics.capes.providers;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import xiamomc.morph.client.graphics.capes.ICapeProvider;
import xiamomc.morph.client.mixin.accessors.UtilAccessor;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * 修改自 <a href="https://github.com/Hibiii/Kappa">Hibiii/Kappa</a>
 * <br>
 * 十分感谢Orz
 */
public final class KappaCapeProvider implements ICapeProvider
{
	//单独给披风请求开一个Worker，避免阻塞MainWorker上的其他请求
	private static final ExecutorService capeService = ((UtilAccessor)new Util()).callCreateWorker("MorphClientCapeService");

	private ExecutorService getCapeExecutor()
	{
		return capeService;
	}

	@Override
	public void getCape(GameProfile profile, Consumer<Identifier> callback)
	{
		this.loadCape(profile, callback::accept);
	}

	// This loads the cape for one player, doesn't matter if it's the player or not.
	// Requires a callback, that receives the id for the cape
	public void loadCape(GameProfile player, CapeTextureAvailableCallback callback) {
		Runnable runnable = () -> {
			// Check if the player doesn't already have a cape.
			Identifier existingCape = capes.get(player.getName());
			if(existingCape != null) {
				callback.onTexAvail(existingCape);
				return;
			}
			if(!this.tryUrl(player, callback, "https://optifine.net/capes/" + player.getName() + ".png")) {
				this.tryUrl(player, callback, "http://s.optifine.net/capes/" + player.getName() + ".png");
			}
		};

		this.getCapeExecutor().execute(runnable);
	}

	public interface CapeTextureAvailableCallback {
		public void onTexAvail(Identifier id);
	}

	// This is a provider specific implementation.
	// Images are usually 46x22 or 92x44, and these work as expected (64x32, 128x64).
	// There are edge cages with sizes 184x88, 1024x512 and 2048x1024,
	// but these should work alright.
	private NativeImage uncrop(NativeImage in) {
		int srcHeight = in.getHeight(), srcWidth = in.getWidth();
		int zoom = (int) Math.ceil(in.getHeight() / 32f);
		NativeImage out = new NativeImage(64 * zoom, 32 * zoom, true);
		// NativeImage.copyFrom doesn't work! :(
		for (int x = 0; x < srcWidth; x++) {
			for (int y = 0; y < srcHeight; y++) {
				out.setColor(x, y, in.getColor(x, y));
			}
        }
		return out;
	}

	// This is where capes will be stored
	private static Map<String, Identifier> capes = new HashMap<String, Identifier>();

	// Try to load a cape from an URL.
	// If this fails, it'll return false, and let us try another url.
	private boolean tryUrl(GameProfile player, CapeTextureAvailableCallback callback, String urlFrom) {
		try {
			URL url = new URL(urlFrom);
			NativeImage tex = uncrop(NativeImage.read(url.openStream()));
			NativeImageBackedTexture nIBT = new NativeImageBackedTexture(tex);
			Identifier id = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("kappa" + player.getId().toString().replace("-", ""), nIBT);
			capes.put(player.getName(), id);
			callback.onTexAvail(id);
		}
		catch(FileNotFoundException e) {
			// Getting the cape was successful! But there's no cape, so don't retry.
			return true;
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}

	public KappaCapeProvider() { }
}
