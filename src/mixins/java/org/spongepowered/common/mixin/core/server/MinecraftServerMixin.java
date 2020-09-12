/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.server;

import com.google.common.reflect.TypeToken;
import com.google.inject.Injector;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.Difficulty;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.bridge.command.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.applaunch.config.core.InheritableConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.config.inheritable.WorldConfig;
import org.spongepowered.common.event.resource.ResourceEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.relocate.co.aikar.timings.TimingsManager;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.service.server.SpongeServerScopedServiceProvider;

import java.io.File;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends RecursiveEventLoop<TickDelayedTask> implements SpongeServer, MinecraftServerBridge,
        CommandSourceProviderBridge {

    @Shadow @Final protected Thread serverThread;
    @Shadow @Final private PlayerProfileCache profileCache;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int tickCounter;
    @Shadow @Final private ResourcePackList<ResourcePackInfo> resourcePacks;
    @Shadow @Final private IReloadableResourceManager resourceManager;

    @Shadow public abstract CommandSource shadow$getCommandSource();
    @Shadow public abstract Iterable<ServerWorld> shadow$getWorlds();
    @Shadow public abstract boolean shadow$isDedicatedServer();
    @Shadow public abstract boolean shadow$isServerRunning();
    @Shadow public abstract PlayerList shadow$getPlayerList();

    @Nullable private SpongeServerScopedServiceProvider impl$serviceProvider;
    @Nullable private ResourcePack impl$resourcePack;
    private boolean impl$enableSaving = true;

    public MinecraftServerMixin(final String name) {
        super(name);
    }

    @Inject(method = "startServerThread", at = @At("HEAD"))
    private void impl$setThreadOnServerPhaseTracker(final CallbackInfo ci) {
        try {
            PhaseTracker.SERVER.setThread(this.serverThread);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not initialize the server PhaseTracker!");
        }
    }

    @Inject(method = "setResourcePack(Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD") )
    private void impl$createSpongeResourcePackWrapper(final String url, final String hash, final CallbackInfo ci) {
        if (url.length() == 0) {
            this.impl$resourcePack = null;
        } else {
            try {
                this.impl$resourcePack = SpongeResourcePack.create(url, hash);
            } catch (final URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "loadDataPacks(Ljava/io/File;Lnet/minecraft/world/storage/WorldInfo;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourcePackList;reloadPacksFromFinders()V"))
    private void impl$registerPackDiscoverers(File p_195560_1_, WorldInfo p_195560_2_, CallbackInfo ci) {
        ResourceEventFactory.registerPackDiscoverers(TypeToken.of(Server.class), this, this.resourcePacks);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$registerResourceReloadListeners(File anvilFileIn, Proxy serverProxyIn, DataFixer dataFixerIn, Commands commandManagerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn, IChunkStatusListenerFactory chunkStatusListenerFactoryIn, String folderNameIn, CallbackInfo ci) {
        ResourceEventFactory.registerResourceReloadListeners(TypeToken.of(Server.class), this, this.resourceManager);
    }

    @Override
    public ResourcePack bridge$getResourcePack() {
        return this.impl$resourcePack;
    }

    @Override
    public void bridge$setSaveEnabled(final boolean enabled) {
        this.impl$enableSaving = enabled;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void impl$onServerTickStart(final CallbackInfo ci) {
        TimingsManager.FULL_SERVER_TICK.startTiming();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void impl$tickServerScheduler(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        this.getScheduler().tick();
    }

    @Override
    public CommandSource bridge$getCommandSource(final Cause cause) {
        return this.shadow$getCommandSource();
    }

    // We want to save the username cache json, as we normally bypass it.
    @Inject(method = "save", at = @At("RETURN"))
    private void impl$saveUsernameCacheOnSave(
            final boolean suppressLog,
            final boolean flush,
            final boolean forced,
            final CallbackInfoReturnable<Boolean> cir) {
        ((PlayerProfileCacheBridge) this.profileCache).bridge$setCanSave(true);
        this.profileCache.save();
        ((PlayerProfileCacheBridge) this.profileCache).bridge$setCanSave(false);
    }

    /**
     * @author Zidane
     * @reason Apply our branding
     */
    @Overwrite
    public String getServerModName() {
        return "sponge";
    }

//    @Inject(method = "tick", at = @At(value = "RETURN"))
//    private void impl$completeTickCheckAnimation(CallbackInfo ci) {
//        final int lastAnimTick = SpongeCommonEventFactory.lastAnimationPacketTick;
//        final int lastPrimaryTick = SpongeCommonEventFactory.lastPrimaryPacketTick;
//        final int lastSecondaryTick = SpongeCommonEventFactory.lastSecondaryPacketTick;
//        if (SpongeCommonEventFactory.lastAnimationPlayer != null) {
//            final ServerPlayerEntity player = SpongeCommonEventFactory.lastAnimationPlayer.get();
//            if (player != null && lastAnimTick != 0 && lastAnimTick - lastPrimaryTick > 3 && lastAnimTick - lastSecondaryTick > 3) {
//                final BlockSnapshot blockSnapshot = BlockSnapshot.empty();
//
//                final RayTraceResult result = SpongeImplHooks.rayTraceEyes(player, SpongeImplHooks.getBlockReachDistance(player) + 1);
//
//                // Hit non-air block
//                if (result instanceof BlockRayTraceResult) {
//                    return;
//                }
//
//                if (!player.getHeldItemMainhand().isEmpty() && SpongeCommonEventFactory.callInteractItemEventPrimary(player, player.getHeldItemMainhand(), Hand.MAIN_HAND, null, blockSnapshot).isCancelled()) {
//                    SpongeCommonEventFactory.lastAnimationPacketTick = 0;
//                    SpongeCommonEventFactory.lastAnimationPlayer = null;
//                    return;
//                }
//
//                SpongeCommonEventFactory.callInteractBlockEventPrimary(player, player.getHeldItemMainhand(), Hand.MAIN_HAND, null);
//            }
//            SpongeCommonEventFactory.lastAnimationPlayer = null;
//        }
//        SpongeCommonEventFactory.lastAnimationPacketTick = 0;
//
//        TimingsManager.FULL_SERVER_TICK.stopTiming();
//    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 6000, ordinal = 0))
    private int getSaveTickInterval(final int tickInterval) {
        if (!this.shadow$isDedicatedServer()) {
            return tickInterval;
        } else if (!this.shadow$isServerRunning()) {
            // Don't autosave while server is stopping
            return this.tickCounter + 1;
        }

        final int autoPlayerSaveInterval = SpongeConfigs.getCommon().get().getWorld().getAutoPlayerSaveInterval();
        if (autoPlayerSaveInterval > 0 && (this.tickCounter % autoPlayerSaveInterval == 0)) {
            this.shadow$getPlayerList().saveAllPlayerData();
        }

        this.save(true, true, false);

        // force check to fail as we handle everything above
        return this.tickCounter + 1;
    }

    /**
     * @author Zidane - Minecraft 1.14.4
     * @reason To allow per-world auto-save tick intervals or disable auto-saving entirely
     */
    @Overwrite
    public boolean save(final boolean suppressLog, final boolean flush, final boolean forced) {
        if (!this.impl$enableSaving) {
            return false;
        }

        for (final ServerWorld world : this.shadow$getWorlds()) {
            final SerializationBehavior serializationBehavior = ((WorldInfoBridge) world.getWorldInfo()).bridge$getSerializationBehavior();
            final boolean save = serializationBehavior != SerializationBehaviors.NONE.get();
            boolean log = !suppressLog;

            if (save) {
                if (this.shadow$isDedicatedServer() && this.shadow$isServerRunning()) {
                    final InheritableConfigHandle<WorldConfig> adapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
                    final int autoSaveInterval = adapter.get().getWorld().getAutoSaveInterval();
                    if (log) {
                        log = adapter.get().getLogging().logWorldAutomaticSaving();
                    }
                    if (autoSaveInterval <= 0 || serializationBehavior != SerializationBehaviors.AUTOMATIC.get()) {
                        if (log) {
                            LOGGER.warn("Auto-saving has been disabled for world '{}'. No chunk data will be auto-saved - to re-enable auto-saving "
                                            + "set 'auto-save-interval' to a value greater than"
                                            + " zero in the corresponding serverWorld config.",
                                    ((org.spongepowered.api.world.server.ServerWorld) world).getKey());
                        }
                        continue;
                    }
                    if (this.tickCounter % autoSaveInterval != 0) {
                        continue;
                    }
                    if (log) {
                        LOGGER.info("Auto-saving chunks for world '{}'", ((org.spongepowered.api.world.server.ServerWorld) world).getKey());
                    }
                } else if (log) {
                    LOGGER.info("Saving chunks for world '{}'", ((org.spongepowered.api.world.server.ServerWorld) world).getKey());
                }

                ((ServerWorldBridge) world).bridge$save(null, flush, world.disableLevelSaving && !forced);
            }
        }

        return true;
    }

    /**
     * @author Zidane - Minecraft 1.14.4
     * @reason Set the difficulty without marking as custom
     */
    @Overwrite
    public void setDifficultyForAllWorlds(final Difficulty difficulty, final boolean forceDifficulty) {
        for (final ServerWorld world : this.shadow$getWorlds()) {
            ((SpongeServer) SpongeCommon.getServer()).getWorldManager().adjustWorldForDifficulty(world, difficulty, forceDifficulty);
        }
    }

    @Override
    public void bridge$initServices(final Game game, final Injector injector) {
        if (this.impl$serviceProvider == null) {
            this.impl$serviceProvider = new SpongeServerScopedServiceProvider(this, game, injector);
            this.impl$serviceProvider.init();
        }
    }

    @Override
    public SpongeServerScopedServiceProvider bridge$getServiceProvider() {
        return this.impl$serviceProvider;
    }
}
