package de.ellpeck.actuallyadditions.common.tile;

import de.ellpeck.actuallyadditions.common.inventory.ContainerFilter;
import de.ellpeck.actuallyadditions.common.inventory.slot.SlotFilter;
import de.ellpeck.actuallyadditions.common.items.ItemDrill;
import de.ellpeck.actuallyadditions.common.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.common.util.ItemStackHandlerAA;
import de.ellpeck.actuallyadditions.common.util.StackUtil;
import de.ellpeck.actuallyadditions.common.util.compat.SlotlessableItemHandlerWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class TileEntityLaserRelayItemWhitelist extends TileEntityLaserRelayItem implements IButtonReactor {

    public FilterSettings leftFilter = new FilterSettings(12, true, true, false, false, 0, -1000);
    public FilterSettings rightFilter = new FilterSettings(12, true, true, false, false, 0, -2000);

    public TileEntityLaserRelayItemWhitelist() {
        super("laserRelayItemWhitelist");
    }

    @Override
    public int getPriority() {
        return super.getPriority() + 10;
    }

    @Override
    public boolean isWhitelisted(ItemStack stack, boolean output) {
        return output ? this.rightFilter.check(stack) : this.leftFilter.check(stack);
    }

    @Override
    public void writeSyncableNBT(NBTTagCompound compound, NBTType type) {
        super.writeSyncableNBT(compound, type);

        this.leftFilter.writeToNBT(compound, "LeftFilter");
        this.rightFilter.writeToNBT(compound, "RightFilter");
    }

    @Override
    public void readSyncableNBT(NBTTagCompound compound, NBTType type) {
        super.readSyncableNBT(compound, type);

        this.leftFilter.readFromNBT(compound, "LeftFilter");
        this.rightFilter.readFromNBT(compound, "RightFilter");
    }

    @Override
    public void onButtonPressed(int buttonID, EntityPlayer player) {
        this.leftFilter.onButtonPressed(buttonID);
        this.rightFilter.onButtonPressed(buttonID);
        if (buttonID == 2) {
            this.addWhitelistSmart(false);
        } else if (buttonID == 3) {
            this.addWhitelistSmart(true);
        }
    }

    private void addWhitelistSmart(boolean output) {
        for (SlotlessableItemHandlerWrapper handler : this.handlersAround.values()) {
            IItemHandler itemHandler = handler.getNormalHandler();
            if (itemHandler != null) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (StackUtil.isValid(stack)) {
                        this.addWhitelistSmart(output, stack);
                    }
                }
            }
        }
    }

    private void addWhitelistSmart(boolean output, ItemStack stack) {
        FilterSettings usedSettings = output ? this.rightFilter : this.leftFilter;
        ItemStack copy = stack.copy();
        copy.setCount(1);

        if (!FilterSettings.check(copy, usedSettings.filterInventory, true, usedSettings.respectMeta, usedSettings.respectNBT, usedSettings.respectMod, usedSettings.respectOredict)) {
            for (int k = 0; k < usedSettings.filterInventory.getSlots(); k++) {
                ItemStack slot = usedSettings.filterInventory.getStackInSlot(k);
                if (StackUtil.isValid(slot)) {
                    if (SlotFilter.isFilter(slot)) {
                        ItemStackHandlerAA inv = new ItemStackHandlerAA(ContainerFilter.SLOT_AMOUNT);
                        ItemDrill.loadSlotsFromNBT(inv, slot);

                        boolean did = false;
                        for (int j = 0; j < inv.getSlots(); j++) {
                            if (!StackUtil.isValid(inv.getStackInSlot(j))) {
                                inv.setStackInSlot(j, copy);
                                did = true;
                                break;
                            }
                        }

                        if (did) {
                            ItemDrill.writeSlotsToNBT(inv, slot);
                            break;
                        }
                    }
                } else {
                    usedSettings.filterInventory.setStackInSlot(k, copy);
                    break;
                }
            }
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!this.world.isRemote) {
            if ((this.leftFilter.needsUpdateSend() || this.rightFilter.needsUpdateSend()) && this.sendUpdateWithInterval()) {
                this.leftFilter.updateLasts();
                this.rightFilter.updateLasts();
            }
        }
    }
}
