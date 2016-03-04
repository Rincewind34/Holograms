package com.sainttx.holograms.nms.v1_8_R1;

import com.sainttx.holograms.api.HologramLine;
import com.sainttx.holograms.api.entity.Nameable;
import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.DamageSource;
import net.minecraft.server.v1_8_R1.EntityArmorStand;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R1.Vec3D;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;

import java.lang.reflect.Field;

public class EntityHologram extends EntityArmorStand implements Nameable {

    private boolean lockTick;
    private HologramLine parentPiece;

    public EntityHologram(World world, HologramLine parentPiece) {
        super(world);
        super.a(new NullBoundingBox()); // Forces the bounding box
        setInvisible(true);
        setSmall(true);
        setArms(false);
        setGravity(true);
        setBasePlate(true);
        this.parentPiece = parentPiece;
        try {
            setPrivateField(EntityArmorStand.class, this, "bg", Integer.MAX_VALUE);
        } catch (Exception e) {
            // There's still the overridden method.
        }
        setLockTick(true);
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }

    @Override
    public boolean c(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public void e(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }


    @Override
    public boolean isInvulnerable(DamageSource source) {
        /*
         * The field Entity.invulnerable is private.
		 * It's only used while saving NBTTags, but since the entity would be killed
		 * on chunk unload, we prefer to override isInvulnerable().
		 */
        return true;
    }

    @Override
    public void setName(String text) {
        if (text != null && text.length() > 300) {
            text = text.substring(0, 300);
        }
        super.setCustomName(text);
        super.setCustomNameVisible(text != null && !text.isEmpty());
    }

    @Override
    public String getName() {
        return super.getCustomName();
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        // Locks the custom name.
    }

    @Override
    public boolean a(EntityHuman human, Vec3D vec3d) {
        // Prevent stand being equipped
        return true;
    }

    @Override
    public boolean d(int i, ItemStack item) {
        // Prevent stand being equipped
        return false;
    }

    @Override
    public void setEquipment(int i, ItemStack item) {
        // Prevent stand being equipped
    }

    @Override
    public void a(AxisAlignedBB boundingBox) {
        // Do not change it!
    }

    @Override
    public int getId() {

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length > 2 && elements[2] != null && elements[2].getFileName().equals("EntityTrackerEntry.java") && elements[2].getLineNumber() > 137 && elements[2].getLineNumber() < 147) {
            // Then this method is being called when creating a new packet, we return a fake ID!
            return -1;
        }

        return super.getId();
    }

    @Override
    public void s_() {
        if (!lockTick) {
            super.s_();
        }
    }

    @Override
    public void makeSound(String sound, float f1, float f2) {
        // Remove sounds.
    }

    public void setLockTick(boolean lock) {
        lockTick = lock;
    }

    @Override
    public void remove() {
        die();
    }

    @Override
    public void die() {
        setLockTick(false);
        super.die();
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (super.bukkitEntity == null) {
            this.bukkitEntity = new CraftHologram(this.world.getServer(), this);
        }
        return this.bukkitEntity;
    }

    public void setLocationNMS(double x, double y, double z) {
        super.setPosition(x, y, z);

        // Send a packet near to update the position.
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(this);

        for (Object obj : this.world.players) {
            if (obj instanceof EntityPlayer) {
                EntityPlayer nmsPlayer = (EntityPlayer) obj;

                double distanceSquared = Math.pow(nmsPlayer.locX - this.locX, 2) + Math.pow(nmsPlayer.locZ - this.locZ, 2);
                if (distanceSquared < 8192 && nmsPlayer.playerConnection != null) {
                    nmsPlayer.playerConnection.sendPacket(teleportPacket);
                }
            }
        }
    }

    @Override
    public HologramLine getHologramLine() {
        return parentPiece;
    }

    public void setPrivateField(Class<?> clazz, Object handle, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(handle, value);
    }
}
