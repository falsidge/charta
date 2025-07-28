package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.entity.IronLeashFenceKnotEntity;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import dev.lucaargolo.charta.utils.LeashableHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mob.class)
public abstract class EntityMixin {

//    @Shadow public abstract Level level();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"), method = "checkAndHandleImportantInteractions", cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void interactIronLead(Player player, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir, ItemStack itemstack) {
        Mob mob = ((Mob) (Object) this);

        if (mob.canBeLeashed(player)) {
            if (itemstack.is(ModItems.IRON_LEAD.get()) && !mob.isLeashed() && mob instanceof LeashableMixed mixed) {
                if (!mob.level().isClientSide()) {
                    mob.setLeashedTo(player, true);
                    mixed.charta_setIronLeash(true);
                }

                itemstack.shrink(1);
                cir.setReturnValue(InteractionResult.sidedSuccess(mob.level().isClientSide));
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "setLeashedTo", cancellable = true)
    private <E extends Entity> void doNotMixLeashTypes(Entity leashHolder, boolean broadcastPacket, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if(entity instanceof LeashableMixed mixed && mixed.charta_isIronLeash() && leashHolder instanceof LeashFenceKnotEntity && !(leashHolder instanceof IronLeashFenceKnotEntity)) {
            ci.cancel();
        }else if((!(entity instanceof LeashableMixed mixed) || !mixed.charta_isIronLeash()) && leashHolder instanceof IronLeashFenceKnotEntity) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "restoreLeashFromSave")
    private  <E extends Entity> void captureRestoreEntity(CallbackInfo ci) {
        LeashableHelper.capturedRestoreEntity = (Entity) (Object) this;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V", ordinal =1), method = "restoreLeashFromSave", index = 0)
    private Entity restoreIronLeashFromSave(Entity entity) {
        if(entity instanceof LeashFenceKnotEntity leashEntity && !(entity instanceof IronLeashFenceKnotEntity) && LeashableHelper.capturedRestoreEntity instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            Level level = leashEntity.level();
            BlockPos pos = leashEntity.getPos();
            leashEntity.kill();
            return IronLeashFenceKnotEntity.getOrCreateIronKnot(level, pos);
        }
        return entity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), method = "restoreLeashFromSave")
    private <E extends Entity> ItemLike restoreIronLeashFromSave(ItemLike item) {
        if(LeashableHelper.capturedRestoreEntity instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            mixed.charta_setIronLeash(false);
            return ModItems.IRON_LEAD.get();
        }
        return item;
    }

    @Inject(at = @At("TAIL"), method = "restoreLeashFromSave")
    private <E extends Entity> void removeCapturedEntity(CallbackInfo ci) {
        LeashableHelper.capturedRestoreEntity = null;
    }

    @Inject(at = @At("HEAD"), method = "dropLeash")
    private  <E extends Entity> void captureDropEntity(CallbackInfo ci) {
        LeashableHelper.capturedDropEntity =  (Entity) (Object) this;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), method = "dropLeash")
    private <E extends Entity> ItemLike dropIronLeash(ItemLike item) {
        if(LeashableHelper.capturedDropEntity instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            mixed.charta_setIronLeash(false);
            return ModItems.IRON_LEAD.get();
        }
        return item;
    }

    @Inject(at = @At("TAIL"), method = "dropLeash")
    private <E extends Entity> void removeCapturedEntity(boolean broadcastPacket, boolean dropLeash, CallbackInfo ci) {
        LeashableHelper.capturedDropEntity = null;
    }
}
