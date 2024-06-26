/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourcePack;
import net.minecraft.resource.*;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin {
    @Shadow @Final private ResourceType type;

    @Inject(
            method = "reload",
            at = @At("HEAD")
    )
    private void onReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> a, CallbackInfoReturnable<ResourceReload> cir, @Local(argsOnly = true) LocalRef<List<ResourcePack>> packs) {
        if (this.type == ResourceType.CLIENT_RESOURCES) {
            LambdaBetterGrass mod = LambdaBetterGrass.get();

            mod.log("Inject generated resource pack.");


            if (!(packs.get() instanceof ArrayList<ResourcePack>)) {
                packs.set(new ArrayList<>(packs.get()));
            }

            packs.get().add(mod.resourcePack = new LBGResourcePack(mod));
            LambdaBetterGrass.get().resourceReloader.reload((ResourceManager) this);

        }
    }
}