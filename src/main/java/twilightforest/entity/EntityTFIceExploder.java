package twilightforest.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFBlocks;

public class EntityTFIceExploder extends EntityMob {

	private static final float EXPLOSION_RADIUS = 1;


	public EntityTFIceExploder(World par1World) {
		super(par1World);
		
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true, false, null));
        this.setSize(0.8F, 1.8F);
	}

	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
    }

	@Override
    protected Item getDropItem()
    {
        return Items.SNOWBALL;
    }

    @Override
    public void onLivingUpdate()
    {
    	super.onLivingUpdate();
    	// make snow particles
    	for (int i = 0; i < 3; i++) {
	    	float px = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;
	    	float py = this.getEyeHeight() + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.5F;
	    	float pz = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;
	    	
			TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowguardian", this.lastTickPosX + px, this.lastTickPosY + py, this.lastTickPosZ + pz, 0, 0, 0);
    	}

    }

    @Override
    protected String getAmbientSound()
    {
    	return TwilightForestMod.ID + ":mob.ice.noise";
    }

	@Override
	protected String getHurtSound()
    {
    	return TwilightForestMod.ID + ":mob.ice.hurt";
    }

	@Override
    protected String getDeathSound()
    {
    	return TwilightForestMod.ID + ":mob.ice.death";
    }

	@Override
    public float getEyeHeight()
    {
        return this.height * 0.6F;
    }

	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getSourceOfDamage() instanceof EntityPlayer) {
			((EntityPlayer)par1DamageSource.getSourceOfDamage()).addStat(TFAchievementPage.twilightHunter);
		}
	}
	
    @Override
	protected void onDeathUpdate() {
        ++this.deathTime;

        if (this.deathTime == 60)
        {
            int i;
            
            
            boolean flag = this.worldObj.getGameRules().getBoolean("mobGriefing");
            this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, (float)EntityTFIceExploder.EXPLOSION_RADIUS, flag);

            if (flag) {
            	this.detonate();
            }

            if (!this.worldObj.isRemote && (this.recentlyHit > 0 || this.isPlayer()) && this.canDropLoot() && this.worldObj.getGameRules().getBoolean("doMobLoot"))
            {
                i = this.getExperiencePoints(this.attackingPlayer);

                while (i > 0)
                {
                    int j = EntityXPOrb.getXPSplit(i);
                    i -= j;
                    this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, j));
                }
            }

            this.setDead();

            for (i = 0; i < 20; ++i)
            {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d2, d0, d1);
            }
        }
	}


	private void detonate() {
		int range = 4;
		
		int sx = MathHelper.floor_double(this.posX);
		int sy = MathHelper.floor_double(this.posY);
		int sz = MathHelper.floor_double(this.posZ);
		
		for (int dx = -range; dx <= range; dx++) {
			for (int dy = -range; dy <= range; dy++) {
				for (int dz = -range; dz <= range; dz++) {
					double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
					
					float randRange = range + (rand.nextFloat() - rand.nextFloat()) * 2F;
					
					if (distance < randRange) {
						this.transformBlock(sx + dx, sy + dy, sz + dz);
					}
				}
			}
		}
	}


	private void transformBlock(int x, int y, int z) {
		Block block = this.worldObj.getBlock(x, y, z);
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		
		// check if we should even explode this
		if (block.getExplosionResistance(this) < 8F && block.getBlockHardness(worldObj, x, y, z) >= 0) {
			
			int blockColor = 16777215;
			
			//TODO: use a better check than exception handling to determine if we have access to client-side methods or not
			try {
				// figure out color
				blockColor = block.colorMultiplier(worldObj, x, y, z);
			} catch (NoSuchMethodError e) {
				// fine, we're on a server
			}

			if (blockColor == 16777215) {
				blockColor = block.getMapColor(meta).colorValue;
			}

			// do appropriate transformation
			if (this.shouldTransformGlass(block, x, y, z)) {
				this.worldObj.setBlock(x, y, z, Blocks.STAINED_GLASS, this.getMetaForColor(blockColor), 3);
			} else if (this.shouldTransformClay(block, x, y, z)) {
				this.worldObj.setBlock(x, y, z, Blocks.STAINED_HARDENED_CLAY, this.getMetaForColor(blockColor), 3);
			}
		}
	}


	private boolean shouldTransformClay(Block block, int x, int y, int z) {
		return block.isNormalCube(this.worldObj, x, y, z);
	}


	private boolean shouldTransformGlass(Block block, int x, int y, int z) {
		return block != Blocks.AIR && this.isBlockNormalBounds(block, x, y, z) && (!block.getMaterial().isOpaque() || block.isLeaves(this.worldObj, x, y, z) || block == Blocks.ICE || block == TFBlocks.auroraBlock);
	}


	private boolean isBlockNormalBounds(Block block, int x, int y, int z) {
		return block.getBlockBoundsMaxX() == 1.0F &&  block.getBlockBoundsMaxY() == 1.0F &&  block.getBlockBoundsMaxZ() == 1.0F && block.getBlockBoundsMinX() == 0.0F && block.getBlockBoundsMinY() == 0.0F && block.getBlockBoundsMinZ() == 0.0F;
	}


	private int getMetaForColor(int blockColor) {
		int red = (blockColor >> 16) & 255; 
		int green = (blockColor >> 8) & 255; 
		int blue = blockColor & 255; 
		
		
		int bestColor = 0;
		int bestDifference = 1024;
		
		for (int i = 0; i < 15; i++) {
			int iColor = Blocks.WOOL.getMapColor(i).colorValue;
			
			int iRed = (iColor >> 16) & 255; 
			int iGreen = (iColor >> 8) & 255; 
			int iBlue = iColor & 255; 
			
			int difference = Math.abs(red - iRed) + Math.abs(green - iGreen) + Math.abs(blue - iBlue);
			
			if (difference < bestDifference) {
				bestColor = i;
				bestDifference = difference;
			}
		}
		
		return bestColor;
	}
	
    @Override
    public int getMaxSpawnedInChunk()
    {
        return 8;
    }
}
