package de.ellpeck.actuallyadditions.common.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TexturedButton extends GuiButton {

    public final List<String> textList = new ArrayList<>();
    private final ResourceLocation resLoc;
    public int texturePosX;
    public int texturePosY;

    public TexturedButton(ResourceLocation resLoc, int id, int x, int y, int texturePosX, int texturePosY, int width, int height) {
        this(resLoc, id, x, y, texturePosX, texturePosY, width, height, new ArrayList<String>());
    }

    public TexturedButton(ResourceLocation resLoc, int id, int x, int y, int texturePosX, int texturePosY, int width, int height, List<String> hoverTextList) {
        super(id, x, y, width, height, "");
        this.texturePosX = texturePosX;
        this.texturePosY = texturePosY;
        this.resLoc = resLoc;
        this.textList.addAll(hoverTextList);
    }

    @Override
    public void drawButton(Minecraft minecraft, int x, int y, float f) {
        if (this.visible) {
            minecraft.getTextureManager().bindTexture(this.resLoc);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            if (k == 0) {
                k = 1;
            }

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            this.drawTexturedModalRect(this.x, this.y, this.texturePosX, this.texturePosY - this.height + k * this.height, this.width, this.height);
            this.mouseDragged(minecraft, x, y);
        }
    }

    public void drawHover(int x, int y) {
        if (this.isMouseOver()) {
            Minecraft mc = Minecraft.getMinecraft();
            GuiUtils.drawHoveringText(this.textList, x, y, mc.displayWidth, mc.displayHeight, -1, mc.fontRenderer);
        }
    }
}
