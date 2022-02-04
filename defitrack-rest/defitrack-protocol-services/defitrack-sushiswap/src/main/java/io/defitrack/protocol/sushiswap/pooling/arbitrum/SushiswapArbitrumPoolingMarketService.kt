package io.defitrack.protocol.sushiswap.pooling.arbitrum

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingMarketService
import org.springframework.stereotype.Component

@Component
class SushiswapArbitrumPoolingMarketService(
    sushiServices: List<SushiswapService>,
) : DefaultSushiPoolingMarketService(sushiServices) {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}