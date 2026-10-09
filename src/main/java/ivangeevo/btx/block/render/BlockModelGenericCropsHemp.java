package ivangeevo.btx.block.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

@Environment(EnvType.CLIENT)
public class BlockModelGenericCropsHemp<T extends BlockLogic> extends BlockModelGeneric<T> {
    public final StaticBlockModel[] models = new StaticBlockModel[8];

    public BlockModelGenericCropsHemp(@NotNull Block<T> block) {
        super(block, BlockModelDispatcher.loadDataModel("btx:block/hemp/hemp_0"));

        for(int i = 0; i < this.models.length; ++i) {
            this.models[i] = BlockModelDispatcher.loadDataModel("btx:block/hemp/hemp_" + i).asModel();
        }

    }

    public @NotNull StaticBlockModel getModelFromData(int data) {
        return this.models[data % this.models.length];
    }
}
