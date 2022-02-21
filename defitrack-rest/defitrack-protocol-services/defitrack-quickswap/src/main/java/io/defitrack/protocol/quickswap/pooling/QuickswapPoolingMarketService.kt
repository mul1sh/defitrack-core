package io.defitrack.protocol.quickswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.token.ERC20Resource
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@EnableScheduling
class QuickswapPoolingMarketService(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
    private val erC20Resource: ERC20Resource
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets() = quickswapService.getPairs()
        .filter {
            it.reserveUSD > BigDecimal.valueOf(100000)
        }.map {

            val token0 = erC20Resource.getTokenInformation(getNetwork(), it.token0.id)
            val token1 = erC20Resource.getTokenInformation(getNetwork(), it.token1.id)

            val element = PoolingMarketElement(
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.id,
                id = "quickswap-polygon-${it.id}",
                name = "QUICKSWAP ${it.token0.symbol}-${it.token1.symbol}",
                token = listOf(
                    token0.toFungibleToken(),
                    token1.toFungibleToken(),
                ),
                apr = quickswapAPRService.getLPAPR(it.id),
                marketSize = it.reserveUSD
            )
            logger.debug("imported ${element.id}")
            element
        }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}