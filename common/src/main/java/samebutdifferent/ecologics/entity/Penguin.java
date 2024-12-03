package samebutdifferent.ecologics.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import samebutdifferent.ecologics.registry.ModEntityTypes;
import samebutdifferent.ecologics.registry.ModItems;
import samebutdifferent.ecologics.registry.ModSoundEvents;
import samebutdifferent.ecologics.registry.ModTags;

public class Penguin extends Animal {
    private static final EntityDataAccessor<Boolean> PREGNANT = SynchedEntityData.defineId(Penguin.class, EntityDataSerializers.BOOLEAN);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(ModTags.ItemTags.PENGUIN_TEMPT_ITEMS);
    private float slideAnimationProgress;
    private float lastSlideAnimationProgress;
    private float swimAnimationProgress;
    private float lastSwimAnimationProgress;
    private int ticksSinceEaten;

    public Penguin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.4F, 1.0F, true);
        this.lookControl = new PenguinLookControl(this, 20);
        this.setCanPickUpLoot(true);
    }

    // ATTRIBUTES & GOALS

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 15.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_DAMAGE, 2.0D).add(Attributes.STEP_HEIGHT, 1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new PenguinSearchForItemsGoal(this));
        this.goalSelector.addGoal(6, new PenguinMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new PenguinRandomSwimmingGoal(this, 1.0D, 60));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new PenguinAttackTargetGoal<>(this, AbstractFish.class, 10, true, false, living -> living.getType().is(ModTags.EntityTypeTags.PENGUIN_HUNT_TARGETS)));
    }

    public static boolean checkPenguinSpawnRules(EntityType<Penguin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(ModTags.BlockTags.PENGUINS_SPAWNABLE_ON) && Penguin.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    // MOVEMENT

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new PenguinPathNavigation(this, pLevel);
    }

    @Override
    public int getMaxAirSupply() {
        return 2400;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), pTravelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(pTravelVector);
        }

    }
    
    // BREEDING

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return ModEntityTypes.PENGUIN.create(level);
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return FOOD_ITEMS.test(pStack);
    }

    public boolean isPregnant() {
        return this.entityData.get(PREGNANT);
    }

    public void setPregnant(boolean isPregnant) {
        this.entityData.set(PREGNANT, isPregnant);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PREGNANT, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("IsPregnant", this.isPregnant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setPregnant(pCompound.getBoolean("IsPregnant"));
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal otherParent) {
        ServerPlayer serverplayer = this.getLoveCause();
        if (serverplayer == null && otherParent.getLoveCause() != null) {
            serverplayer = otherParent.getLoveCause();
        }

        if (serverplayer != null) {
            serverplayer.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer, this, otherParent, null);
        }

        this.setAge(6000);
        otherParent.setAge(6000);
        this.resetLove();
        otherParent.resetLove();
        level.broadcastEntityEvent(this, (byte)18);
        this.setPregnant(true);
        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            level.addFreshEntity(new ExperienceOrb(level, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        if (!this.isBaby() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.spawnAtLocation(ModItems.PENGUIN_FEATHER, 1);
        }
    }

    @Override
    public void aiStep() {
        if (this.isPregnant()) {
            if (this.random.nextInt(3000) == 0) {
                if (!this.level().isClientSide()) {
                    ServerLevel level = (ServerLevel) this.level();
                    this.setPregnant(false);
                    Penguin penguin = ModEntityTypes.PENGUIN.create(level);
                    penguin.setBaby(true);
                    penguin.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                    level.addFreshEntityWithPassengers(penguin);
                    level.broadcastEntityEvent(this, (byte) 18);
                }
            }
        }
        if (level().getGameTime() % 80L == 0L) {
            if (this.level().getEntitiesOfClass(Penguin.class, this.getBoundingBox().inflate(20.0D)).size() > 4) {
                for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0D))) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, true));
                }
            }
        }
        if (!this.level().isClientSide() && this.isAlive() && this.isEffectiveAi()) {
            ++this.ticksSinceEaten;
            ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (this.canEat(stack)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack finishedStack = stack.finishUsingItem(this.level(), this);
                    if (!finishedStack.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, finishedStack);
                    }
                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1f) {
                    this.playSound(this.getEatingSound(stack), 1.0f, 1.0f);
                    this.level().broadcastEntityEvent(this, (byte)45);
                }
            }
        }
        super.aiStep();
    }

    private boolean canEat(ItemStack itemStack) {
        return itemStack.get(DataComponents.FOOD) != null && this.getTarget() == null && this.onGround();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 45) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                for (int i = 0; i < 8; ++i) {
                    Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0).xRot(-this.getXRot() * ((float)Math.PI / 180)).yRot(-this.getYRot() * ((float)Math.PI / 180));
                    this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack), this.getX() + this.getLookAngle().x / 2.0, this.getY(), this.getZ() + this.getLookAngle().z / 2.0, vec3.x, vec3.y + 0.05, vec3.z);
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // FISH

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        EquipmentSlot equipmentslot = this.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(equipmentslot).isEmpty() || this.isBaby()) {
            return false;
        } else {
            return equipmentslot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
        }
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return itemstack.isEmpty() && pStack.is(ModTags.ItemTags.PENGUIN_TEMPT_ITEMS) && !this.isBaby();
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        if (this.canHoldItem(itemstack)) {
            int count = itemstack.getCount();
            if (count > 1) {
                this.dropItemStack(itemstack.split(count - 1));
            }

            this.onItemPickup(pItemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack.split(1));
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
            this.ticksSinceEaten = 0;
        }
    }

    private void dropItemStack(ItemStack pStack) {
        ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), pStack);
        this.level().addFreshEntity(itementity);
    }

    // SOUNDS

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.PENGUIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSoundEvents.PENGUIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.PENGUIN_DEATH;
    }

    // ANIMATION
    private boolean babyIsNearAdult() {
        if (this.isBaby()) {
            for(Penguin penguin : this.level().getEntitiesOfClass(Penguin.class, this.getBoundingBox().inflate(2.0D, 5.0D, 2.0D))) {
                return !penguin.isBaby();
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.slideAnimationProgress != this.lastSlideAnimationProgress || this.swimAnimationProgress != this.lastSwimAnimationProgress) {
                this.refreshDimensions();
            }
        }
        this.updateSwimmingAnimation();
        this.updateSlidingAnimation();
    }

    public boolean canSlide() {
        return this.level().getBlockState(this.blockPosition().below()).is(Blocks.ICE) && !this.isInLove() && !this.isPregnant() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
    }

    private void updateSlidingAnimation() {
        this.lastSlideAnimationProgress = this.slideAnimationProgress;
        this.slideAnimationProgress = this.canSlide() ? Math.min(1.0f, this.slideAnimationProgress + 0.15f) : Math.max(0.0f, this.slideAnimationProgress - 0.15f);
    }

    public float getSlidingAnimationProgress(float ticks) {
        return Mth.lerp(ticks, this.lastSlideAnimationProgress, this.slideAnimationProgress);
    }

    private void updateSwimmingAnimation() {
        this.lastSwimAnimationProgress = this.swimAnimationProgress;
        this.swimAnimationProgress = this.isInWater() ? Math.min(1.0f, this.swimAnimationProgress + 0.15f) : Math.max(0.0f, this.swimAnimationProgress - 0.15f);
    }

    public float getSwimmingAnimationProgress(float ticks) {
        return Mth.lerp(ticks, this.lastSwimAnimationProgress, this.swimAnimationProgress);
    }

    @Override
    public void customServerAiStep() {
        if (this.swimAnimationProgress > 0) {
            this.setPose(Pose.SWIMMING);
        } else if(this.slideAnimationProgress > 0) {
            this.setPose(Pose.CROUCHING);
        } else {
            this.setPose(Pose.STANDING);
        }
    }

    /*@Override
    public EntityDimensions getDimensions(Pose pose) {
        float progress = this.slideAnimationProgress > 0 ? slideAnimationProgress : this.swimAnimationProgress > 0 ? swimAnimationProgress : 0.0f;
        if (progress > 0) {
            return super.getDimensions(pose).scale(this.isBaby() ? 1.0f + progress : 1.0f + progress * 0.3F, 1.0f - progress / 2);
        }
        return super.getDimensions(pose).scale(1.0f, this.isBaby() ? 1.4f : 1.0f);
    }*/

    @Override
    public int getMaxHeadYRot() {
        return 40;
    }

    static class PenguinPathNavigation extends WaterBoundPathNavigation {

        public PenguinPathNavigation(Penguin penguin, Level level) {
            super(penguin, level);
        }

        protected boolean canUpdatePath() {
            return true;
        }

        protected PathFinder createPathFinder(int p_149222_) {
            this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
            return new PathFinder(this.nodeEvaluator, p_149222_);
        }

        public boolean isStableDestination(BlockPos p_149224_) {
            return !this.level.getBlockState(p_149224_.below()).isAir();
        }
    }

    static class PenguinLookControl extends LookControl {
        private final int maxYRotFromCenter;

        public PenguinLookControl(Mob mob, int maxYRotFromCenter) {
            super(mob);
            this.maxYRotFromCenter = maxYRotFromCenter;
        }

        @Override
        public void tick() {
            if (this.lookAtCooldown > 0) {
                --this.lookAtCooldown;
                this.getYRotD().ifPresent(yHeadRot -> {
                    this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, yHeadRot, this.yMaxRotSpeed);
                });
                this.getXRotD().ifPresent(xRot -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), xRot, this.xMaxRotAngle)));
            } else {
                if (this.mob.getNavigation().isDone()) {
                    this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), 0.0f, 5.0f));
                }
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, this.yMaxRotSpeed);
            }
            float f = Mth.wrapDegrees(this.mob.yHeadRot - this.mob.yBodyRot);
            if (f < (float)(-this.maxYRotFromCenter)) {
                this.mob.yBodyRot -= 4.0f;
            } else if (f > (float)this.maxYRotFromCenter) {
                this.mob.yBodyRot += 4.0f;
            }
        }
    }

    static class PenguinAttackTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final Penguin penguin;

        public PenguinAttackTargetGoal(Penguin pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pPredicate) {
            super(pMob, pTargetType, pRandomInterval, pMustSee, pMustReach, pPredicate);
            this.penguin = pMob;
        }

        @Override
        public boolean canUse() {
            if (penguin.isBaby() || penguin.isPregnant() || !penguin.getMainHandItem().isEmpty()) {
                return false;
            } else {
                return super.canUse();
            }
        }
    }

    static class PenguinRandomSwimmingGoal extends RandomSwimmingGoal {
        private final Penguin penguin;

        public PenguinRandomSwimmingGoal(Penguin pMob, double pSpeedModifier, int pInterval) {
            super(pMob, pSpeedModifier, pInterval);
            this.penguin = pMob;
        }

        @Override
        public boolean canUse() {
            if (penguin.isBaby() || penguin.isPregnant() || !penguin.getMainHandItem().isEmpty()) {
                return false;
            } else {
                return super.canUse();
            }
        }
    }

    static class PenguinSearchForItemsGoal extends Goal {
        private final Penguin penguin;

        public PenguinSearchForItemsGoal(Penguin penguin) {
            this.setFlags(EnumSet.of(Flag.MOVE));
            this.penguin = penguin;
        }

        @Override
        public boolean canUse() {
            if (!penguin.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || penguin.isBaby() || penguin.isPregnant()) {
                return false;
            } else {
                List<ItemEntity> list = penguin.level().getEntitiesOfClass(ItemEntity.class, penguin.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), itemEntity -> itemEntity.getItem().is(ModTags.ItemTags.PENGUIN_TEMPT_ITEMS));
                return !list.isEmpty() && penguin.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
            }
        }
        
        @Override
        public void tick() {
            List<ItemEntity> list = penguin.level().getEntitiesOfClass(ItemEntity.class, penguin.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), itemEntity -> itemEntity.getItem().is(ModTags.ItemTags.PENGUIN_TEMPT_ITEMS));
            ItemStack itemstack = penguin.getItemBySlot(EquipmentSlot.MAINHAND);
            if (itemstack.isEmpty() && !list.isEmpty()) {
                penguin.getNavigation().moveTo(list.get(0), 1.0F);
            }

        }
        
        @Override
        public void start() {
            List<ItemEntity> list = penguin.level().getEntitiesOfClass(ItemEntity.class, penguin.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), itemEntity -> itemEntity.getItem().is(ModTags.ItemTags.PENGUIN_TEMPT_ITEMS));
            if (!list.isEmpty()) {
                penguin.getNavigation().moveTo(list.get(0), 1.0F);
            }

        }
    }

    static class PenguinMeleeAttackGoal extends MeleeAttackGoal {
        private final Penguin penguin;

        public PenguinMeleeAttackGoal(Penguin pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
            this.penguin = pMob;
        }

        @Override
        public boolean canUse() {
            if (!penguin.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || penguin.isBaby() || penguin.isPregnant()) {
                return false;
            } else {
                return super.canUse();
            }
        }
    }
}
