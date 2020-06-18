package me.steven.indrev.blocks.nuclear

import me.steven.indrev.blockentities.generators.NuclearReactorProxyBlockEntity
import me.steven.indrev.blocks.ProxyBlock
import me.steven.indrev.blocks.nuclear.NuclearCoreSide.Companion.offset
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class NuclearReactorPart(settings: Settings) : Block(settings), BlockEntityProvider, InventoryProvider, ProxyBlock {

    init {
        this.defaultState = stateManager.defaultState.with(CORE_DIRECTION, NuclearCoreSide.UNKNOWN)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(CORE_DIRECTION)
    }

    override fun onUse(state: BlockState, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        val corePos = pos?.offset(state[CORE_DIRECTION] ?: return super.onUse(state, world, pos, player, hand, hit))
        val coreBlockState = world?.getBlockState(corePos)
        if (coreBlockState?.block is NuclearReactorCore)
            return coreBlockState.block.onUse(coreBlockState, world, corePos, player, hand, hit)
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun getBlockEntityPos(state: BlockState, blockPos: BlockPos): BlockPos {
        return blockPos.offset(state[CORE_DIRECTION])
    }

    override fun createBlockEntity(view: BlockView?): BlockEntity? = NuclearReactorProxyBlockEntity()

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory {
        val blockEntity = world?.getBlockEntity(getBlockEntityPos(state!!, pos!!))
        if (blockEntity !is InventoryProvider) throw IllegalArgumentException("tried to retrieve an inventory from an invalid block entity")
        return blockEntity.getInventory(state, world, pos)
    }

    companion object {
        val CORE_DIRECTION: EnumProperty<NuclearCoreSide> = EnumProperty.of("core_direction", NuclearCoreSide::class.java)
    }
}