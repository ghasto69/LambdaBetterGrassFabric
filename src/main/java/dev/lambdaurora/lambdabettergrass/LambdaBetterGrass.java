/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.metadata.LBGGrassState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourcePack;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourceReloader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Represents the LambdaBetterGrass mod.
 *
 * @author LambdAurora
 * @version 1.5.2
 * @since 1.0.0
 */
public class LambdaBetterGrass implements ClientModInitializer {
	public static final String NAMESPACE = "lambdabettergrass";
	public static final Logger LOGGER = LogUtils.getLogger();
	/* Default masks */
	public static final Identifier BETTER_GRASS_SIDE_CONNECT_MASK = id("bettergrass/mask/standard_block_side_connect.png");
	public static final Identifier BETTER_GRASS_SIDE_BLEND_UP_MASK = id("bettergrass/mask/grass_block_side_blend_up.png");
	public static final Identifier BETTER_GRASS_SIDE_ARCH_BLEND_MASK = id("bettergrass/mask/grass_block_side_arch_blend.png");

	@ApiStatus.Internal
	public static LambdaBetterGrass INSTANCE;
	public final LBGConfig config = new LBGConfig(this);
	private final ThreadLocal<Boolean> betterLayerDisabled = ThreadLocal.withInitial(() -> false);
	public final LBGResourceReloader resourceReloader = new LBGResourceReloader();
	public LBGResourcePack resourcePack;

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		this.log("Initializing LambdaBetterGrass...");
		this.config.load();

		ModContainer mod = FabricLoader.getInstance().getModContainer("lambdabettergrass").orElseThrow();

		if (!ResourceManagerHelper.registerBuiltinResourcePack(id("default"), mod, ResourcePackActivationType.DEFAULT_ENABLED)) {
			throw new IllegalStateException("Failed to register Resource Pack?");
		}
		ResourceManagerHelper.registerBuiltinResourcePack(id("32x"), mod, ResourcePackActivationType.NORMAL);
		LambdaBetterGrass.this.resourcePack = new LBGResourcePack(LambdaBetterGrass.this);

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return id("register_pack");
			}

			@Override
			public void reload(ResourceManager resourceManager) {
				if (LambdaBetterGrass.this.config.isDebug()) {
					LambdaBetterGrass.this.resourcePack.dumpTo(Path.of("debug/lbg_out"));
				}
			}
		});

		LBGState.registerType("grass", (id, block, resourceManager, json, deserializationContext) -> new LBGGrassState(id, resourceManager, json));
		LBGState.registerType("layer", LBGLayerState::new);

		ModelLoadingPlugin.register(pluginCtx -> {
			pluginCtx.modifyModelOnLoad().register(ModelModifier.WRAP_PHASE, (model, context) -> {
				if (context.topLevelId() instanceof ModelIdentifier modelId) {
					if (!modelId.getVariant().equals("inventory")) {
						var stateId = modelId.id();

						// Get cached states metadata.
						var state = LBGState.getMetadataState(stateId);

						// If states metadata found, search for corresponding metadata and if exists replace the model.
						if (state != null) {
							var newModel = state.getCustomUnbakedModel(modelId, model, context::getOrLoadModel);

							if (newModel != null) {
								return newModel;
							}
						}
					}
				}

				return model;
			});
		});
	}

	/**
	 * Prints a message to the terminal.
	 *
	 * @param info the message to print
	 */
	public void log(String info) {
		LOGGER.info("[LambdaBetterGrass] " + info);
	}

	/**
	 * Prints a warning message to the terminal.
	 *
	 * @param info the message to print
	 */
	public void warn(String info) {
		LOGGER.warn("[LambdaBetterGrass] " + info);
	}

	/**
	 * Prints a warning message to the terminal.
	 *
	 * @param info the message to print
	 */
	public void warn(String info, Object... objects) {
		LOGGER.warn("[LambdaBetterGrass] " + info, objects);
	}

	/**
	 * Returns whether the better layer feature is enabled or not.
	 *
	 * @return {@code true} if the better layer feature is enabled, otherwise {@code false}
	 */
	public boolean hasBetterLayer() {
		if (this.config.hasBetterLayer())
			return !this.betterLayerDisabled.get();
		return false;
	}

	/**
	 * {@return a LambdaBetterGrass Minecraft identifier}
	 *
	 * @param path the path
	 */
	public static Identifier id(@NotNull String path) {
		return Identifier.of(NAMESPACE, path);
	}

	/**
	 * {@return the LambdaBetterGrass mod instance}
	 */
	public static LambdaBetterGrass get() {
		return INSTANCE;
	}

	/**
	 * Pushes the force-disable of the better layer feature.
	 */
	public static void pushDisableBetterLayer() {
		get().betterLayerDisabled.set(true);
	}

	/**
	 * Pops the force-disable of the better layer feature.
	 */
	public static void popDisableBetterLayer() {
		get().betterLayerDisabled.remove();
	}
}
