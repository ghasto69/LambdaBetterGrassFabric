/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LifecycledResourceManagerImpl.class)
public class LifecycledResourceManagerImplMixin {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void reloadResources(ResourceType type, List<ResourcePack> packs, CallbackInfo ci) {
        if (type == ResourceType.CLIENT_RESOURCES) {
            // For some reason, this cannot happen in ReloadableResourceManager. You should keep an eye for if this changes.
            LambdaBetterGrass.get().resourceReloader.reload((ResourceManager) this);
        }
    }
}
