package io.defitrack.protocol.idex

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class IdexFarmingMarketService(
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val idexService: IdexService
) : StakingMarketService() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    val minichefABI by lazy {
        abiResource.getABI("idex/IdexFarm.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        return idexService.idexFarm().map {
            IdexFarmContract(
                polygonContractAccessor,
                minichefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength).mapNotNull { poolId ->
                try {
                    val farm = toStakingMarketElement(chef, poolId)
                    logger.info("imported ${farm.id}")
                    farm
                } catch (ex: Exception) {
                    logger.debug("something went wrong trying to import idex pool", ex)
                    null
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private fun toStakingMarketElement(
        chef: IdexFarmContract,
        poolId: Int
    ): StakingMarketElement {
        val rewardPerBlock = chef.rewardPerBlock.toBigDecimal().times(BigDecimal(43200)).times(BigDecimal(365))
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken)
        return StakingMarketElement(
            id = "idex-${chef.address}-${poolId}",
            network = getNetwork(),
            name = stakedtoken.name + " Farm",
            protocol = getProtocol(),
            token = StakedToken(
                name = stakedtoken.name,
                symbol = stakedtoken.symbol,
                address = stakedtoken.address,
                network = getNetwork(),
                decimals = stakedtoken.decimals,
                type = stakedtoken.type
            ),
            rewardToken = RewardToken(
                name = rewardToken.name,
                symbol = rewardToken.symbol,
                decimals = rewardToken.decimals
            ),
            contractAddress = chef.address,
            vaultType = "idex-farm",
            marketSize = 0.0,
            rate = 0.0
        )
    }
}