package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class SquidRoutePayloadProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            // Transaction request payload
            "route.transactionRequest.routeType" to "routeType",
            "route.transactionRequest.targetAddress" to "targetAddress",
            "route.transactionRequest.data" to "data",
            "route.transactionRequest.value" to "value",
            "route.transactionRequest.gasPrice" to "gasPrice",
            "route.transactionRequest.gasLimit" to "gasLimit",
            "route.transactionRequest.maxFeePerGas" to "maxFeePerGas",
            "route.transactionRequest.maxPriorityFeePerGas" to "maxPriorityFeePerGas"
        )
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, keyMap)
    }
}