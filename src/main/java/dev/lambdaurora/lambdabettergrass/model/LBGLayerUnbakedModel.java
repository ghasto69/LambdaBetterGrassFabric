/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.metadata.LBGCompiledLayerMetadata;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Represents the LambdaBetterGrass unbaked model for layer method.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public class LBGLayerUnbakedModel implements UnbakedModel {
	private final UnbakedModel baseModel;
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerUnbakedModel(UnbakedModel baseModel, List<LBGCompiledLayerMetadata> metadatas) {
		this.baseModel = baseModel;
		this.metadatas = metadatas;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		Set<Identifier> ids = new HashSet<>(this.baseModel.getModelDependencies());
		this.metadatas.forEach(metadata -> metadata.fetchModelDependencies(ids));
		return ids;
	}

	@Override
	public void setParents(Function<Identifier, UnbakedModel> models) {
		this.baseModel.setParents(models);
		this.metadatas.forEach(metadata -> metadata.resolveParents(models));
	}

	@Override
	public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter
			, ModelBakeSettings rotationContainer) {
		this.metadatas.forEach(metadata -> metadata.bake(baker, textureGetter, rotationContainer));
		return new LBGLayerBakedModel(Objects.requireNonNull(this.baseModel.bake(baker, textureGetter, rotationContainer)),
				this.metadatas
		);
	}
}
