package io.defitrack.protocol.aave.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingService
import io.defitrack.lending.domain.LendingElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AavePolygonService
import io.defitrack.token.FungibleToken
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AavePolygonLendingService(
    private val aavePolygonService: AavePolygonService
) : LendingService {

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON

    override suspend fun getLendings(address: String): List<LendingElement> {
        return aavePolygonService.getUserReserves(address).mapNotNull {
            if (it.currentATokenBalance > BigInteger.ZERO) {
                LendingElement(
                    id = "polygon-aave-${it.reserve.symbol}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.lendingRate,
                    amount = it.currentATokenBalance,
                    name = it.reserve.name,
                    token = FungibleToken(
                        it.reserve.name,
                        it.reserve.decimals,
                        it.reserve.symbol
                    )
                )
            } else null
        }
    }
}