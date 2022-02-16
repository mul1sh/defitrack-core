package io.defitrack.protocol.convex.contract

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CrvRewardContract(
    solidityBasedContractAccessor: EvmContractAccessor,
                        abi: String,
                        address: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    fun balanceOfMethod(address: String): Function {
        return createFunction(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )
    }

    fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun earned(address: String): BigInteger {
        return read(
            "earned",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}