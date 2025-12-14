package com.glodblock.github.client;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.CPacketUseKeybind;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyBindings {

    private static final String KEY_CATEGORY = "key.ae2fc.category";

    private static final KeyBinding WIRELESS_FLUID_PATTERN_TERMINAL = new KeyBinding("key.ae2fc.open_wireless_fluid_pattern_terminal.desc", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, 0, KEY_CATEGORY);

    public static void init() {
        ClientRegistry.registerKeyBinding(WIRELESS_FLUID_PATTERN_TERMINAL);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    @SubscribeEvent
    public static void onKeyInputEvent(final InputEvent.KeyInputEvent event) {
        if (WIRELESS_FLUID_PATTERN_TERMINAL.isPressed()) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketUseKeybind());
        }
    }

}
