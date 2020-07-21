package uk.co.kring.mc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class Red extends ComparatorBlock {
    public Red(Properties properties) {
        super(properties);
    }

    @Override
    protected int getDelay(BlockState state) {
        return super.getDelay(state);
    }

    @Override
    protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return super.getActiveSignal(worldIn, pos, state);
    }

    @Override
    protected boolean shouldBePowered(World worldIn, BlockPos pos, BlockState state) {
        return super.shouldBePowered(worldIn, pos, state);
    }

    @Override
    protected int calculateInputStrength(World worldIn, BlockPos pos, BlockState state) {
        return super.calculateInputStrength(worldIn, pos, state);
    }

    @Override
    protected void updateState(World worldIn, BlockPos pos, BlockState state) {
        super.updateState(worldIn, pos, state);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
    }

    @Override
    protected int getPowerOnSides(IWorldReader worldIn, BlockPos pos, BlockState state) {
        return super.getPowerOnSides(worldIn, pos, state);
    }

    @Override
    protected int getPowerOnSide(IWorldReader worldIn, BlockPos pos, Direction side) {
        return super.getPowerOnSide(worldIn, pos, side);
    }

    @Override
    protected void notifyNeighbors(World worldIn, BlockPos pos, BlockState state) {
        super.notifyNeighbors(worldIn, pos, state);
    }

    @Override
    protected boolean isAlternateInput(BlockState state) {
        return super.isAlternateInput(state);
    }
}
