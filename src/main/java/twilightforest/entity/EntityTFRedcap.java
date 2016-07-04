package twilightforest.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.entity.ai.EntityAITFRedcapLightTNT;
import twilightforest.entity.ai.EntityAITFRedcapShy;


public class EntityTFRedcap extends EntityMob {
	

    public static ItemStack heldPick = new ItemStack(Items.IRON_PICKAXE, 1);
    public static ItemStack heldTNT = new ItemStack(Blocks.TNT, 1);
    public static ItemStack heldFlint = new ItemStack(Items.FLINT_AND_STEEL, 1);
    
	private boolean shy;
	
	private int tntLeft = 0;

	public EntityTFRedcap(World world)
    {
        super(world);
        //texture = TwilightForestMod.MODEL_DIR + "redcap.png";
        //moveSpeed = 0.28F;
        setSize(0.9F, 1.4F);

        shy = true;
        
        this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityTNTPrimed.class, 2.0F, 1.0F, 2.0F));
		this.tasks.addTask(2, new EntityAITFRedcapShy(this, 1.0F));
		this.tasks.addTask(3, new EntityAITFRedcapLightTNT(this, 1.0F)); // light TNT
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true, false, null));
        
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, heldPick);
        this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));

        this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.2F);
        this.setDropChance(EntityEquipmentSlot.FEET, 0.2F);
    }
    
    public EntityTFRedcap(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
    }

    @Override
	protected String getLivingSound()
    {
        return TwilightForestMod.ID + ":mob.redcap.redcap";
    }

    @Override
	protected String getHurtSound()
    {
        return TwilightForestMod.ID + ":mob.redcap.hurt";
    }

    @Override
	protected String getDeathSound()
    {
        return TwilightForestMod.ID + ":mob.redcap.die";
    }

    @Override
	protected Item getDropItem()
    {
        return Items.COAL;
    }
    
    public boolean isShy() 
    {
    	return shy && this.recentlyHit <= 0;
    }
    
    public int getTntLeft() {
		return tntLeft;
	}

	public void setTntLeft(int tntLeft) {
		this.tntLeft = tntLeft;
	}
	
	public ItemStack getPick()
	{
		return heldPick;
	}

	@Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("TNTLeft", this.getTntLeft());
    }

	@Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setTntLeft(par1NBTTagCompound.getInteger("TNTLeft"));
    }

	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getSourceOfDamage() instanceof EntityPlayer) {
			((EntityPlayer)par1DamageSource.getSourceOfDamage()).addStat(TFAchievementPage.twilightHunter);
			// are we in a level 1 hill?
			int chunkX = MathHelper.floor_double(posX) >> 4;
			int chunkZ = MathHelper.floor_double(posZ) >> 4;
			if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hill1) {
				// award level 1 hill cheevo
				((EntityPlayer)par1DamageSource.getSourceOfDamage()).addStat(TFAchievementPage.twilightHill1);
			}

		}
	}

}
