package io.codechef.protocol.aave.borrowing

import io.codechef.common.network.Network
import io.codechef.defitrack.borrowing.BorrowService
import io.codechef.defitrack.borrowing.domain.BorrowElement
import io.codechef.protocol.Protocol
import io.codechef.protocol.aave.AavePolygonService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

@Service
class AavePolygonBorrowingService(
    private val aavePolygonService: AavePolygonService,
) : BorrowService {

    override fun getBorrows(address: String): List<BorrowElement> {
        return aavePolygonService.getUserReserves(address).mapNotNull {

            if ((it.currentStableDebt > BigInteger.ONE || it.currentVariableDebt > BigInteger.ONE)) {
                BorrowElement(
                    id = UUID.randomUUID().toString(),
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.borrowRate,
                    amount = (it.currentStableDebt + it.currentVariableDebt).toBigDecimal()
                        .divide(
                            BigDecimal.TEN.pow(it.reserve.decimals), 2, RoundingMode.HALF_UP
                        ).toPlainString(),
                    name = it.reserve.name,
                    symbol = it.reserve.symbol,
                    tokenUrl = "https://etherscan.io/address/tokens/${it.reserve.underlyingAsset}",
                )
            } else null
        }
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON
}