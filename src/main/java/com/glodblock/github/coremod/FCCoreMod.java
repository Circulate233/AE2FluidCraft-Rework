package com.glodblock.github.coremod;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@IFMLLoadingPlugin.Name("FluidCraftCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("com.glodblock.github.coremod")
public class FCCoreMod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final Map<String, BooleanSupplier> MIXIN_CONFIGS = new Object2ObjectLinkedOpenHashMap<>();

    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
        // NO-OP
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    private static void addMixinCFG(final String mixinConfig) {
        addMixinCFG(mixinConfig, () -> true);
    }

    private static void addMixinCFG(final String mixinConfig, @Nonnull final BooleanSupplier conditions) {
        MIXIN_CONFIGS.put(mixinConfig, conditions);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public List<String> getMixinConfigs() {
        return new ObjectArrayList<>(MIXIN_CONFIGS.keySet());
    }

    @Override
    public boolean shouldMixinConfigQueue(final String mixinConfig) {
        return MIXIN_CONFIGS.get(mixinConfig).getAsBoolean();
    }
}
