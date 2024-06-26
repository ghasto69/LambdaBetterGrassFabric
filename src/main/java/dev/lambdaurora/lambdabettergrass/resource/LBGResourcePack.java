/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.google.common.collect.Sets;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LBGResourcePack implements ResourcePack {
	private static final Set<String> NAMESPACES = Sets.newHashSet(LambdaBetterGrass.NAMESPACE);

	private final Object2ObjectMap<String, Supplier<byte[]>> resources = new Object2ObjectOpenHashMap<>();
	private final LambdaBetterGrass mod;

	public LBGResourcePack(LambdaBetterGrass mod) {
		this.mod = mod;
	}

	public Identifier putImage(String name, NativeImage image) {
		final var id = Identifier.of(LambdaBetterGrass.NAMESPACE, "block/bettergrass/" + name);

		Supplier<byte[]> supplier = () -> {
			try {
				return image.getBytes();
			} catch (IOException e) {
				this.mod.warn("Could not put image {}.", name, e);
				return null;
			}
		};

		this.resources.put(String.format("assets/%s/textures/%s.png", id.getNamespace(), id.getPath()), supplier);

		return id;
	}

	@Override
	public @Nullable InputSupplier<InputStream> openRoot(String... path) {
		return openResource(String.join("/", path));
	}

	@Override
	public @Nullable InputSupplier<InputStream> open(ResourceType type, Identifier id) {
		return openResource(String.format("%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath()));
	}

	@Override
	public void findResources(ResourceType type, String namespace, String startingPath, ResultConsumer consumer) {
		String path = String.format("%s/%s/%s", type.getDirectory(), namespace, startingPath);

		this.resources.keySet().stream()
				.filter(string -> string.startsWith(path))
				.forEach(entry -> consumer.accept(fromPath(type, entry), openResource(entry)));
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return NAMESPACES;
	}

	@Override
	public ResourcePackInfo getInfo() {
		return new ResourcePackInfo("lbg_generated", Text.literal("LBG generated"), ResourcePackSource.BUILTIN, Optional.empty());
	}

	@Override
	public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) {
		return null;
	}

	@Override
	public void close() {
	}

	protected InputSupplier<InputStream> openResource(String path) {
		var supplier = this.resources.get(path);
		if (supplier == null) {
			return null;
		}

		byte[] bytes = supplier.get();
		if (bytes == null) {
			return null;
		}

		return () -> new ByteArrayInputStream(bytes);
	}

	private static @Nullable Identifier fromPath(ResourceType type, String path) {
		String[] split = path.substring((type.getDirectory() + "/").length()).split("/", 2);

		return Identifier.tryParse(split[0], split[1]);
	}

	public void dumpTo(Path of) {
		Path path = FabricLoader.getInstance().getGameDir().resolve(of);
		path.toFile().mkdirs();
		resources.forEach((id, res) -> {
            try {
				path.resolve(id).getParent().toFile().mkdirs();
                Files.write(path.resolve(id), res.get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
	}
}