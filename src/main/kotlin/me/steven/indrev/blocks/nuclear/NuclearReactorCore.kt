package me.steven.indrev.blocks.nuclear

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Property
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class NuclearReactorCore(
    settings: Settings,
    screenHandlerType: ExtendedScreenHandlerType<*>?,
    blockEntityProvider: () -> MachineBlockEntity
) :
    MachineBlock(settings, Tier.MK4, screenHandlerType, blockEntityProvider) {

    init {
        this.defaultState = stateManager.defaultState
            .with(NORTH, false)
            .with(SOUTH, false)
            .with(EAST, false)
            .with(WEST, false)
            .with(UP, false)
            .with(DOWN, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(
            NORTH,
            SOUTH,
            EAST,
            WEST,
            UP,
            DOWN
        )
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        if (isFormed(state))
            return super.onUse(state, world, pos, player, hand, hit)
        return ActionResult.PASS
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        var state = super.getPlacementState(ctx)
        Direction.values().forEach { direction ->
            val partPos = ctx?.blockPos?.offset(direction)
            val partState = ctx?.world?.getBlockState(partPos)
            val isPart = isStatePart(partState, direction.opposite)
            if (isPart)
                ctx?.world?.setBlockState(partPos, partState?.with(NuclearReactorPart.CORE_DIRECTION, NuclearCoreSide.fromMinecraft(direction.opposite)))
            state = state?.with(getProperty(direction), isPart)
        }
        return state
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        facing: Direction,
        neighborState: BlockState?,
        world: WorldAccess?,
        pos: BlockPos?,
        neighborPos: BlockPos?
    ): BlockState {
        val isPart = isStatePart(neighborState, facing.opposite)
        if (isPart)
            world?.setBlockState(neighborPos, neighborState?.with(NuclearReactorPart.CORE_DIRECTION, NuclearCoreSide.fromMinecraft(facing.opposite)), 3)
        return state.with(getProperty(facing), isPart)
    }

    fun isStatePart(blockState: BlockState?, direction: Direction): Boolean {
        val block = blockState?.block
        if (block !is NuclearReactorPart) return false
        val corePart = blockState.get(NuclearReactorPart.CORE_DIRECTION)
        return corePart == NuclearCoreSide.UNKNOWN || corePart == NuclearCoreSide.fromMinecraft(direction)
    }

    fun isFormed(state: BlockState?): Boolean = state != null && state[NORTH] && state[SOUTH] && state[EAST] && state[WEST] && state[UP] && state[DOWN]

    companion object {
        val NORTH: BooleanProperty = BooleanProperty.of("north")
        val SOUTH: BooleanProperty = BooleanProperty.of("south")
        val EAST: BooleanProperty = BooleanProperty.of("east")
        val WEST: BooleanProperty = BooleanProperty.of("west")
        val UP: BooleanProperty = BooleanProperty.of("up")
        val DOWN: BooleanProperty = BooleanProperty.of("down")

        fun getProperty(facing: Direction): Property<Boolean> {
            return when (facing) {
                Direction.EAST -> EAST
                Direction.WEST -> WEST
                Direction.NORTH -> NORTH
                Direction.SOUTH -> SOUTH
                Direction.UP -> UP
                Direction.DOWN -> DOWN
                else -> EAST
            }
        }
    }
}