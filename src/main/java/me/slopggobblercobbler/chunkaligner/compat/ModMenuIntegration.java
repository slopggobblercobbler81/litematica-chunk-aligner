package me.slopggobblercobbler.chunkaligner.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.slopggobblercobbler.chunkaligner.AlignerConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("title.litematica_chunk_aligner.config"));

            builder.setSavingRunnable(() -> AlignerConfig.getInstance().saveConfig());

            ConfigCategory category = builder.getOrCreateCategory(Text.translatable("category.litematica_chunk_aligner.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            category.addEntry(entryBuilder.startBooleanToggle(
                            Text.translatable("option.litematica_chunk_aligner.enabled"),
                            AlignerConfig.getInstance().isEnabled())
                    .setDefaultValue(true)
                    .setSaveConsumer(AlignerConfig.getInstance()::setEnabled)
                    .build());

            return builder.build();
        };
    }
}
