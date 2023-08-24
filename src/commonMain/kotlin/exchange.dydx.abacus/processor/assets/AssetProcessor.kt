package exchange.dydx.abacus.processor.assets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

@Suppress("UNCHECKED_CAST")
internal class AssetProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val assetKeyMap = iMapOf(
        "string" to iMapOf(
            "baseAsset" to "symbol"
        )
    )

    private val assetConfigurationsResourcesKeyMap = iMapOf(
        "string" to iMapOf(
            "websiteLink" to "websiteLink",
            "whitepaperLink" to "whitepaperLink",
            "coinMarketCapsLink" to "coinMarketCapsLink",
            "imageUrl" to "imageUrl",
            "primaryDescriptionKey" to "primaryDescriptionKey",
            "secondaryDescriptionKey" to "secondaryDescriptionKey"
        )
    )

    private val assetConfigurationsKeyMap = iMapOf(
        "string" to iMapOf(
            "baseSymbol" to "symbol",
            "name" to "name"
        ),
        "double" to iMapOf(
            "circulatingSupply" to "circulatingSupply"
        ),
        "strings" to iMapOf(
            "tags" to "tags"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val received = transform(existing, payload, assetKeyMap)
        val symbol = received["symbol"]
        if (symbol != null) {
            received["id"] = symbol
        }
        return received
    }

    internal fun receivedConfigurations(
        asset: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val received = transform(asset, payload, assetConfigurationsKeyMap)
        val symbol = received["symbol"]
        if (symbol != null) {
            received["id"] = symbol
        }
        val resources = transform(
            parser.asMap(asset?.get("resources")),
            payload,
            assetConfigurationsResourcesKeyMap
        )
        received["resources"] = resources

        return received
    }
}