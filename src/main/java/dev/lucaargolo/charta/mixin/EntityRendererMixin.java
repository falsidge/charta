package dev.lucaargolo.charta.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lucaargolo.charta.client.ModRenderType;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeashKnotRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @Unique
    private Entity charta_capturedLeashable;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public <E extends Entity> void captureLeashable(LeashFenceKnotEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        charta_capturedLeashable = entity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), method = "render(Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public RenderType getIronLeash(RenderType renderType) {
        if(charta_capturedLeashable instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            return ModRenderType.ironLeash();
        }
        return renderType;
    }


}
