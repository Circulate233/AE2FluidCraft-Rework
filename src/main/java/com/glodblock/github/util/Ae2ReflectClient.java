package com.glodblock.github.util;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.implementations.GuiExpandedProcessingPatternTerm;
import appeng.client.gui.implementations.GuiInterface;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPriority;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.sync.GuiBridge;
import appeng.fluids.client.gui.GuiFluidInterface;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.google.common.collect.ImmutableMap;
import com.mekeng.github.client.gui.GuiGasInterface;
import mezz.jei.Internal;
import mezz.jei.input.InputHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.Optional;

import java.lang.invoke.MethodHandle;

public class Ae2ReflectClient {

    private static final MethodHandle mGuiCraftAmount_addQty;
    private static final MethodHandle fGetAEBaseGui_stackSizeRenderer;
    private static final MethodHandle cItemEncodedPatternBakedModel;
    private static final MethodHandle fGetGuiPriority_originalGuiBtn;
    private static final MethodHandle fGetGuiCraftingStatus_originalGuiBtn;
    private static final MethodHandle fSetGuiCraftingStatus_myIcon;
    private static final MethodHandle fSetGuiPatternTerm_container;
    private static final MethodHandle fSetGuiMEMonitorable_monitorableContainer;
    private static final MethodHandle fSetGuiMEMonitorable_configSrc;
    private static final MethodHandle fGetGuiMEMonitorable_craftingStatusBtn;
    private static final MethodHandle fGetGuiInterface_priority;
    private static final MethodHandle fGetGuiFluidInterface_priority;
    private static final MethodHandle fSetGuiUpgradeable_cvb;
    private static final MethodHandle fGetGuiCraftAmount_next;
    private static final MethodHandle fGetGuiCraftAmount_amountToCraft;
    private static final MethodHandle fGetGuiCraftAmount_originalGuiBtn;
    private static final MethodHandle fGetGuiCraftAmount_originalGui;
    private static final MethodHandle[] fGetGuiCraftAmount_minus = new MethodHandle[4];
    private static final MethodHandle[] fGetGuiCraftAmount_plus = new MethodHandle[4];
    private static final MethodHandle fSetGuiCraftConfirm_ccc;
    private static final MethodHandle fGetGuiCraftConfirm_cancel;
    private static final MethodHandle fGetGuiGasInterface_priority;
    private static final MethodHandle fGetInternal_inputHandler;

    static {
        try {
            mGuiCraftAmount_addQty = Ae2Reflect.reflectMethodHandle(GuiCraftAmount.class, "addQty", int.class);
            fGetAEBaseGui_stackSizeRenderer = Ae2Reflect.reflectFieldGetter(AEBaseGui.class, "stackSizeRenderer");
            cItemEncodedPatternBakedModel = Ae2Reflect.reflectConstructor(Class.forName("appeng.client.render.crafting.ItemEncodedPatternBakedModel"), IBakedModel.class, ImmutableMap.class);
            fGetGuiPriority_originalGuiBtn = Ae2Reflect.reflectFieldGetter(GuiPriority.class, "originalGuiBtn");
            fGetGuiCraftingStatus_originalGuiBtn = Ae2Reflect.reflectFieldGetter(GuiCraftingStatus.class, "originalGuiBtn");
            fSetGuiCraftingStatus_myIcon = Ae2Reflect.reflectFieldSetter(GuiCraftingStatus.class, "myIcon");
            fSetGuiPatternTerm_container = Ae2Reflect.reflectFieldSetter(GuiPatternTerm.class, "container");
            fSetGuiMEMonitorable_monitorableContainer = Ae2Reflect.reflectFieldSetter(GuiMEMonitorable.class, "monitorableContainer");
            fSetGuiMEMonitorable_configSrc = Ae2Reflect.reflectFieldSetter(GuiMEMonitorable.class, "configSrc");
            fGetGuiMEMonitorable_craftingStatusBtn = Ae2Reflect.reflectFieldGetter(GuiMEMonitorable.class, "craftingStatusBtn");
            fGetGuiInterface_priority = Ae2Reflect.reflectFieldGetter(GuiInterface.class, "priority");
            fGetGuiFluidInterface_priority = Ae2Reflect.reflectFieldGetter(GuiFluidInterface.class, "priority");
            fSetGuiUpgradeable_cvb = Ae2Reflect.reflectFieldSetter(GuiUpgradeable.class, "cvb");
            fGetGuiCraftAmount_next = Ae2Reflect.reflectFieldGetter(GuiCraftAmount.class, "next");
            fGetGuiCraftAmount_amountToCraft = Ae2Reflect.reflectFieldGetter(GuiCraftAmount.class, "amountToCraft");
            fGetGuiCraftAmount_originalGuiBtn = Ae2Reflect.reflectFieldGetter(GuiCraftAmount.class, "originalGuiBtn");
            fGetGuiCraftAmount_originalGui = Ae2Reflect.reflectFieldGetter(GuiCraftAmount.class, "originalGui");
            for (int i = 1, j = 0; i <= 1000; i *= 10, j++) {
                fGetGuiCraftAmount_minus[j] = Ae2Reflect.reflectFieldGetter(GuiCraftAmount.class, "minus" + i);
                fGetGuiCraftAmount_plus[j] = Ae2Reflect.reflectFieldGetter(GuiCraftAmount.class, "plus" + i);
            }
            fSetGuiCraftConfirm_ccc = Ae2Reflect.reflectFieldSetter(GuiCraftConfirm.class, "ccc");
            fGetGuiCraftConfirm_cancel = Ae2Reflect.reflectFieldGetter(GuiCraftConfirm.class, "cancel");
            if (ModAndClassUtil.GAS) {
                fGetGuiGasInterface_priority = Ae2Reflect.reflectFieldGetter(GuiGasInterface.class, "priority");
            } else {
                fGetGuiGasInterface_priority = null;
            }
            if (ModAndClassUtil.JEI) {
                fGetInternal_inputHandler = Ae2Reflect.reflectFieldGetter(Internal.class, "inputHandler");
            } else {
                fGetInternal_inputHandler = null;
            }
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static StackSizeRenderer getStackSizeRenderer(final AEBaseGui gui) {
        return Ae2Reflect.readField(gui, fGetAEBaseGui_stackSizeRenderer);
    }

    public static IBakedModel bakeEncodedPatternModel(final IBakedModel baseModel,
                                                      final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
        try {
            return (IBakedModel) cItemEncodedPatternBakedModel.invoke(baseModel, transforms);
        } catch (final Throwable e) {
            throw new IllegalStateException("Failed to invoke constructor: " + cItemEncodedPatternBakedModel, e);
        }
    }

    public static GuiTabButton getOriginalGuiButton(final GuiPriority gui) {
        return Ae2Reflect.readField(gui, fGetGuiPriority_originalGuiBtn);
    }

    public static GuiTabButton getOriginalGuiButton(final GuiCraftingStatus gui) {
        return Ae2Reflect.readField(gui, fGetGuiCraftingStatus_originalGuiBtn);
    }

    public static void setIconItem(final GuiCraftingStatus gui, final ItemStack icon) {
        Ae2Reflect.writeField(gui, fSetGuiCraftingStatus_myIcon, icon);
    }

    public static void setGuiContainer(final GuiPatternTerm instance, final ContainerPatternEncoder container) {
        Ae2Reflect.writeField(instance, fSetGuiPatternTerm_container, container);
        Ae2Reflect.writeField(instance, fSetGuiMEMonitorable_monitorableContainer, container);
        Ae2Reflect.writeField(instance, fSetGuiMEMonitorable_configSrc, container.getConfigManager());
    }

    public static void setGuiExContainer(final GuiExpandedProcessingPatternTerm instance, final ContainerExtendedFluidPatternTerminal container) {
        Ae2Reflect.writeField(instance, fSetGuiMEMonitorable_monitorableContainer, container);
        Ae2Reflect.writeField(instance, fSetGuiMEMonitorable_configSrc, container.getConfigManager());
    }

    public static GuiTabButton getCraftingStatusButton(final GuiMEMonitorable gui) {
        return Ae2Reflect.readField(gui, fGetGuiMEMonitorable_craftingStatusBtn);
    }

    public static GuiTabButton getPriorityButton(final GuiInterface gui) {
        return Ae2Reflect.readField(gui, fGetGuiInterface_priority);
    }

    public static GuiTabButton getPriorityButton(final GuiFluidInterface gui) {
        return Ae2Reflect.readField(gui, fGetGuiFluidInterface_priority);
    }

    public static GuiTabButton getPriorityButtonGas(final Object gui) {
        return Ae2Reflect.readField(gui, fGetGuiGasInterface_priority);
    }

    public static void setInterfaceContainer(final GuiUpgradeable instance, final ContainerUpgradeable container) {
        Ae2Reflect.writeField(instance, fSetGuiUpgradeable_cvb, container);
    }

    public static GuiButton getGuiCraftAmountNextButton(final GuiCraftAmount gui) {
        return Ae2Reflect.readField(gui, fGetGuiCraftAmount_next);
    }

    public static GuiTextField getGuiCraftAmountTextBox(final GuiCraftAmount gui) {
        return Ae2Reflect.readField(gui, fGetGuiCraftAmount_amountToCraft);
    }

    public static GuiButton getGuiCraftAmountAddButton(final GuiCraftAmount gui, final int index) {
        return index < 0 ?
            Ae2Reflect.readField(gui, fGetGuiCraftAmount_minus[-index - 1]) :
            Ae2Reflect.readField(gui, fGetGuiCraftAmount_plus[index - 1]);
    }

    public static void setGuiCraftAmountAddQty(final GuiCraftAmount gui, final int amount) {
        try {
            mGuiCraftAmount_addQty.invoke(gui, amount);
        } catch (final Throwable e) {
            throw new IllegalStateException("Failed to invoke method: " + mGuiCraftAmount_addQty, e);
        }
    }

    public static GuiTabButton getGuiCraftAmountBackButton(final GuiCraftAmount gui) {
        return Ae2Reflect.readField(gui, fGetGuiCraftAmount_originalGuiBtn);
    }

    public static GuiBridge getGuiCraftAmountOriginalGui(final GuiCraftAmount gui) {
        return Ae2Reflect.readField(gui, fGetGuiCraftAmount_originalGui);
    }

    public static void writeCraftConfirmContainer(final GuiCraftConfirm gui, final ContainerCraftConfirm ccc) {
        Ae2Reflect.writeField(gui, fSetGuiCraftConfirm_ccc, ccc);
    }

    public static GuiButton getCraftConfirmBackButton(final GuiCraftConfirm gui) {
        return Ae2Reflect.readField(gui, fGetGuiCraftConfirm_cancel);
    }

    @Optional.Method(modid = "jei")
    public static InputHandler getInputHandler() {
        return Ae2Reflect.readField(null, fGetInternal_inputHandler);
    }
}
