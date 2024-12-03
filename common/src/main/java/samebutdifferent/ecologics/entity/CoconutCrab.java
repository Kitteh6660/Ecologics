package samebutdifferent.ecologics.entity;

import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import samebutdifferent.ecologics.registry.ModItems;
import samebutdifferent.ecologics.registry.ModSoundEvents;

public class CoconutCrab extends Animal implements NeutralMob {
    private static final EntityDataAccessor<Boolean> HAS_COCONUT = SynchedEntityData.defineId(CoconutCrab.class, EntityDataSerializers.BOOLEAN);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable private UUID persistentAngerTarget;

    public CoconutCrab(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pMob) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CrabAvoidGoal<>(this, Player.class, 8.0F, 2.0D, 2.0D));
        this.goalSelector.addGoal(2, new CrabMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new CrabHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CrabNearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return super.hurt(pSource, this.hasCoconut() ? pAmount / 2 : pAmount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getHealth() <= this.getMaxHealth() / 2 && this.hasCoconut()) {
            this.breakCoconut();
        }
        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
    }

    private void breakCoconut() {
        this.setHasCoconut(false);
        this.stopBeingAngry();
        this.playCoconutSmashSound();
        if (this.level().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).get()) {
	        ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(ModItems.COCONUT_SLICE, 2));
	        itementity.setDefaultPickUpDelay();
	        this.level().addFreshEntity(itementity);
        }
    }

    /*@Override
    public boolean canBreatheUnderwater() {
        return true;
    }*/

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    /*@Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return this.getBbHeight() * 0.5F;
    }*/

    public void setHasCoconut(boolean hasCoconut) {
        this.entityData.set(HAS_COCONUT, hasCoconut);
    }

    public boolean hasCoconut() {
        return this.entityData.get(HAS_COCONUT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_COCONUT, true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("Coconut", this.hasCoconut());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setHasCoconut(pCompound.getBoolean("Coconut"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.COCONUT_CRAB_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSoundEvents.COCONUT_CRAB_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.COCONUT_CRAB_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    protected void playCoconutSmashSound() {
        this.playSound(ModSoundEvents.COCONUT_SMASH, 0.7F, 1.0F);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int pTime) {
        this.remainingPersistentAngerTime = pTime;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    static class CrabMeleeAttackGoal extends MeleeAttackGoal {
        private final CoconutCrab crab;

        public CrabMeleeAttackGoal(CoconutCrab pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
            this.crab = pMob;
        }

        @Override
        public boolean canUse() {
            return crab.hasCoconut() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return crab.hasCoconut() && super.canContinueToUse();
        }

        /*@Override
        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return 4.0f + attackTarget.getBbWidth();
        }*/
    }

    static class CrabHurtByTargetGoal extends HurtByTargetGoal {
        private final CoconutCrab crab;

        public CrabHurtByTargetGoal(CoconutCrab pMob) {
            super(pMob);
            this.crab = pMob;
        }

        @Override
        public boolean canUse() {
            return crab.hasCoconut() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return crab.hasCoconut() && super.canContinueToUse();
        }
    }

    static class CrabAvoidGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final CoconutCrab crab;

        public CrabAvoidGoal(CoconutCrab pMob, Class<T> pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier) {
            super(pMob, pEntityClassToAvoid, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier, EntitySelector.NO_SPECTATORS::test);
            this.crab = pMob;
        }

        @Override
        public boolean canUse() {
            return !this.crab.hasCoconut() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.crab.hasCoconut() && super.canContinueToUse();
        }
    }

    static class CrabNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final CoconutCrab crab;

        public CrabNearestAttackableTargetGoal(CoconutCrab pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate) {
            super(pMob, pTargetType, pRandomInterval, pMustSee, pMustReach, pTargetPredicate);
            this.crab = pMob;
        }

        @Override
        public boolean canUse() {
            return this.crab.hasCoconut() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.crab.hasCoconut() && super.canContinueToUse();
        }
    }
}
