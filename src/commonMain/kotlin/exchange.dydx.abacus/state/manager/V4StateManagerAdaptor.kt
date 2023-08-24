package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.DYDXChainTransactionsProtocol
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.ApiState
import exchange.dydx.abacus.state.app.ApiStatus
import exchange.dydx.abacus.state.app.NetworkState
import exchange.dydx.abacus.state.app.NetworkStatus
import exchange.dydx.abacus.state.app.V4Environment
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.state.modal.onChainAccountBalances
import exchange.dydx.abacus.state.modal.onChainFeeTiers
import exchange.dydx.abacus.state.modal.onChainUserFeeTier
import exchange.dydx.abacus.state.modal.onChainUserStats
import exchange.dydx.abacus.state.modal.squidChains
import exchange.dydx.abacus.state.modal.squidTokens
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import io.ktor.client.utils.EmptyContent.status
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.math.max

class V4StateManagerAdaptor(
    ioImplementations: IOImplementations,
    uiImplementations: UIImplementations,
    environment: V4Environment,
    override var configs: V4StateManagerConfigs,
    stateNotification: StateNotificationProtocol?,
    dataNotification: DataNotificationProtocol?,
) : StateManagerAdaptor(
    ioImplementations, uiImplementations, environment, configs, stateNotification, dataNotification
) {
    private var validatorConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetValidatorConnected(value)
            }
        }

    private val heightPollingDuration = 10.0
    private var heightTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private val chainPollingDuration = 10.0
    private var chainTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private val userStatsPollingDuration = 60.0
    private var userStatsTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private val accountBalancePollingDuration = 10.0
    private var accountBalancesTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    internal var indexerState = NetworkState()
    internal var validatorState = NetworkState()
    private var apiState: ApiState? = null
        set(value) {
            if (field !== value) {
                field = value
                stateNotification?.apiStateChanged(field)
                dataNotification?.apiStateChanged(field)
            }
        }


    private val MAX_NUM_BLOCK_DELAY = 10

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        if (socketConnected) {
            val market = market
            if (market != null) {
                marketCandlesSubscription(market, resolution = candlesResolution, true)
            }
        }
    }

    override fun didSetMarket(market: String?, oldValue: String?) {
        super.didSetMarket(market, oldValue)
        if (market != oldValue) {
            if (oldValue != null) {
                marketCandlesSubscription(oldValue, resolution = candlesResolution, false)
            }
            if (market != null) {
                if (socketConnected) {
                    marketCandlesSubscription(market, resolution = candlesResolution, true)
                }
            }
        }
    }

    override fun didSetCandlesResolution(oldValue: String) {
        super.didSetCandlesResolution(oldValue)
        val market = market
        if (market != null && socketConnected) {
            marketCandlesSubscription(market, oldValue, false)
            marketCandlesSubscription(market, candlesResolution, true)
        }
    }

    @Throws(Exception::class)
    fun marketCandlesSubscription(market: String, resolution: String, subscribe: Boolean = true) {
        val channel = configs.candlesChannel() ?: throw Exception("candlesChannel is null")
        socket(socketAction(subscribe), channel, iMapOf("id" to "$market/$resolution"))
    }

    override fun subaccountChannelParams(
        accountAddress: String,
        subaccountNumber: Int,
    ): IMap<String, Any> {
        return iMapOf("id" to "$accountAddress/$subaccountNumber")
    }

    override fun faucetBody(amount: Double): String? {
        return if (accountAddress != null) {
            val params = iMapOf(
                "address" to accountAddress,
                "subaccountNumber" to subaccountNumber,
                "amount" to amount
            )
            jsonEncoder.encode(params)
        } else null
    }

    override fun socketConnectedSubaccountNumber(id: String?): Int {
        return if (id != null) {
            val parts = id.split("/")
            if (parts.size == 2) {
                parts[1].toIntOrNull() ?: 0
            } else 0
        } else 0
    }

    override fun subaccountsUrl(): String? {
        val url = configs.privateApiUrl("subaccounts")
        return if (url != null) {
            "$url/$accountAddress"
        } else null
    }

    override fun subaccountParams(): IMap<String, String>? {
        val accountAddress = accountAddress
        val subaccountNumber = subaccountNumber
        return if (accountAddress != null) iMapOf(
            "address" to accountAddress,
            "subaccountNumber" to "$subaccountNumber",
        );
        else null
    }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        if (readyToConnect) {
            retrieveTransferChains()
            retrieveTransferTokens()

            bestEffortConnectChain()
        } else {
            validatorConnected = false
            heightTimer = null
        }
    }

    private fun bestEffortConnectChain() {
        findOptimalNode { url ->
            if (url != null) {
                connectChain(url) { successful ->
                    validatorConnected = successful
                }
            } else {
                reconnectChain()
            }
        }
    }

    private fun didSetValidatorConnected(validatorConnected: Boolean) {
        if (validatorConnected) {
            getFeeTiers()
            if (subaccount != null) {
                getUserFeeTier()
                pollUserStats()
                getTransfers()
            }
            if (accountAddress != null) {
                pollAccountBalances()
            }

            val timer = ioImplementations.timer ?: CoroutineTimer.instance
            heightTimer = timer.schedule(0.0, heightPollingDuration) {
                if (readyToConnect) {
                    retrieveIndexerHeight()
                    retrieveValidatorHeight()
                    true
                } else {
                    false
                }
            }
        } else {
            reconnectChain()
        }
    }

    private fun reconnectChain() {
        // Create a timer, to try to connect the chain again
        // Do not repeat. This timer is recreated in bestEffortConnectChain if needed
        val timer = ioImplementations.timer ?: CoroutineTimer.instance
        chainTimer = timer.schedule(chainPollingDuration, null) {
            if (readyToConnect) {
                bestEffortConnectChain()
            }
            false
        }
    }

    private fun getFeeTiers() {
        getOnChain(QueryType.FeeTiers, null) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainFeeTiers(response), oldState)
        }
    }

    @Throws(Exception::class)
    private fun getOnChain(
        type: QueryType,
        paramsInJson: String?,
        callback: (response: String) -> Unit,
    ) {
        val query = ioImplementations.chain
        if (query === null) {
            throw Exception("chain query is null")
        }
        query.get(type, paramsInJson) { response ->
            // Parse the response
            if (response != null) {
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    callback(response)
                }
            }
        }
    }

    override fun didSetAccountAddress(accountAddress: String?, oldValue: String?) {
        super.didSetAccountAddress(accountAddress, oldValue)

        if (accountAddress != null) {
            retrieveSubaccounts()
            pollAccountBalances()
        } else {
            accountBalancesTimer = null
        }
    }

    private fun pollAccountBalances() {
        val timer = ioImplementations.timer ?: CoroutineTimer.instance
        accountBalancesTimer = timer.schedule(0.0, accountBalancePollingDuration) {
            if (validatorConnected && accountAddress != null) {
                getAccountBalances()
                true
            } else {
                false
            }
        }
    }

    private fun getAccountBalances() {
        getOnChain(QueryType.GetAccountBalances, "") { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainAccountBalances(response), oldState)
        }
    }

    override fun didSetSubaccount(subaccount: Subaccount?, oldValue: Subaccount?) {
        super.didSetSubaccount(subaccount, oldValue)
        if (validatorConnected && subaccount != null) {
            getUserFeeTier()
            pollUserStats()
            getTransfers()
        } else {
            userStatsTimer = null
        }
    }

    private fun getUserFeeTier() {
        val params = iMapOf("address" to accountAddress)
        val paramsInJson = jsonEncoder.encode(params)
        getOnChain(QueryType.UserFeeTier, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUserFeeTier(response), oldState)
        }
    }

    private fun pollUserStats() {
        val timer = ioImplementations.timer ?: CoroutineTimer.instance
        userStatsTimer = timer.schedule(0.0, userStatsPollingDuration) {
            if (validatorConnected && subaccount != null) {
                getUserStats()
                true
            } else {
                false
            }
        }
    }

    private fun getUserStats() {
        val params = iMapOf("address" to accountAddress)
        val paramsInJson = jsonEncoder.encode(params)
        getOnChain(QueryType.UserStats, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUserStats(response), oldState)
        }
    }

    private fun getTransfers() {
        getOnChain(QueryType.Transfers, null) { response ->
//            stateMachine.parseOnChainUserFeeTier(response)
        }
    }

    private fun findOptimalNode(callback: (node: String?) -> Unit) {
        val endpointUrls = configs.validatorUrls()
        if (endpointUrls != null && endpointUrls.size > 1) {
            val param = iMapOf(
                "endpointUrls" to endpointUrls,
                "chainId" to environment.dydxChainId,
            )
            val json = jsonEncoder.encode(param)
            ioImplementations.chain?.get(QueryType.OptimalNode, json) { result ->
                if (result != null) {
                    /*
                    response = {
                        "url": "https://...",
                     */
                    val map = Json.parseToJsonElement(result).jsonObject.toIMap()
                    val node = parser.asString(map["url"])
                    callback(node)
                } else {
                    // Not handled by client yet
                    callback(endpointUrls.firstOrNull())
                }
            }
        } else {
            val first = parser.asString(endpointUrls?.firstOrNull())
            callback(first)
        }
    }

    private fun connectChain(validatorUrl: String, callback: (successful: Boolean) -> Unit) {
        val indexerUrl = environment.URIs.indexers?.firstOrNull()?.api ?: return
        val websocketUrl = configs.websocketUrl() ?: return
        val chainId = environment.dydxChainId ?: return
        val faucetUrl = configs.faucetUrl()
        ioImplementations.chain?.connectNetwork(
            indexerUrl,
            websocketUrl,
            validatorUrl,
            chainId,
            faucetUrl
        ) { response ->
            ioImplementations.threading?.async(ThreadingType.abacus) {
                if (response != null) {
                    val json = Json.parseToJsonElement(response).jsonObject.toIMap()
                    ioImplementations.threading?.async(ThreadingType.main) {
                        callback(json["error"] == null)
                    }
                } else {
                    ioImplementations.threading?.async(ThreadingType.main) {
                        callback(false)
                    }
                }
            }
        }
    }


    private fun retrieveIndexerHeight() {
        val url = configs.publicApiUrl("height")
        if (url != null) {
            indexerState.requestTime = Clock.System.now()
            get(url, null, null, false) { response, httpCode ->
                if (success(httpCode) && response != null) {
                    val json = Json.parseToJsonElement(response).jsonObject.toIMap()
                    val height = parser.asInt(json["height"])
                    val time = parser.asDatetime(json["time"])
                    indexerState.block = height
                    indexerState.time = time
                    updateApiState()
                }
            }
        }
    }

    private fun retrieveValidatorHeight() {
        validatorState.requestTime = Clock.System.now()
        getOnChain(QueryType.Height, null) { response ->
            parseHeight(response)
        }
    }

    private fun retrieveTransferChains() {
        val oldState = stateMachine.state
        val url = configs.squidChains()
        if (url != null) {
            get(url, null, null, false) { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.squidChains(response), oldState)
                }
            }
        }
    }

    private fun retrieveTransferTokens() {
        val oldState = stateMachine.state
        val url = configs.squidToken()
        if (url != null) {
            get(url, null, null, false) { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.squidTokens(response), oldState)
                }
            }
        }
    }

    private fun parseHeight(response: String) {
        val json = Json.parseToJsonElement(response).jsonObject.toIMap()
        if (json["error"] != null) {
            validatorState.status = NetworkStatus.UNKNOWN
        } else {
            val header = parser.asMap(parser.value(json, "header"))
            val height = parser.asInt(header?.get("height"))
            val time = parser.asDatetime(header?.get("time"))
            validatorState.block = height
            validatorState.time = time
            updateApiState()
        }
    }

    private fun updateApiState() {
        ioImplementations.threading?.async(ThreadingType.main) {
            apiState = apiState(apiState, indexerState, validatorState)
        }
    }

    private fun apiState(
        apiState: ApiState?,
        indexerState: NetworkState,
        validatorState: NetworkState,
    ): ApiState {
        var status = apiState?.status ?: ApiStatus.UNKNOWN
        var haltedBlock = apiState?.haltedBlock
        var blockDiff: Int? = null
        when (validatorState.status) {
            NetworkStatus.NORMAL -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL, NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.block
                    }
                }
            }

            NetworkStatus.UNKNOWN -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.UNKNOWN
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.block
                    }
                }
            }

            NetworkStatus.UNREACHABLE -> {
                status = ApiStatus.VALIDATOR_DOWN
                haltedBlock = null
            }

            NetworkStatus.HALTED -> {
                status = ApiStatus.VALIDATOR_HALTED
                haltedBlock = validatorState.block
            }
        }
        if (status == ApiStatus.NORMAL) {
            val indexerBlock = indexerState.block
            val validatorBlock = validatorState.block
            if (indexerBlock != null && validatorBlock != null) {
                val diff = validatorBlock - indexerBlock
                if (diff > MAX_NUM_BLOCK_DELAY) {
                    status = ApiStatus.INDEXER_TRAILING
                    blockDiff = diff
                    haltedBlock = null
                }
            }
        }
        val block = if (validatorState.block != null) {
            if (indexerState.block != null) {
                max(validatorState.block!!, indexerState.block!!)
            } else validatorState.block
        } else indexerState.block
        if (apiState?.status != status ||
            apiState.height != block ||
            apiState.haltedBlock != haltedBlock ||
            apiState.trailingBlocks != blockDiff
        ) {
            return ApiState(status, block, haltedBlock, blockDiff)
        }
        return apiState
    }

    override fun sparklinesParams(): IMap<String, String>? {
        return iMapOf("timePeriod" to "ONE_DAY")
    }

    override fun shouldBatchMarketsChannelData(): Boolean {
        return true
    }

    override fun shouldBatchMarketTradesChannelData(): Boolean {
        return true
    }

    @Throws(Exception::class)
    fun transaction(
        type: TransactionType,
        paramsInJson: String?,
        callback: (response: String) -> Unit,
    ) {
        val transactionsImplementation =
            (ioImplementations.chain as? DYDXChainTransactionsProtocol)
        if (transactionsImplementation === null) {
            throw Exception("chain is not DYDXChainTransactionsProtocol")
        }
        transactionsImplementation.transaction(type, paramsInJson) { response ->
            if (response != null) {
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    callback(response)
                }
            }
        }
    }

    override fun commitPlaceOrder(callback: TransactionCallback) {
        val payload = placeOrderPayload()
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)

        transaction(TransactionType.PlaceOrder, string) { response ->
            val error = parseTransactionResponse(response)
            if (error == null) {
                lastOrderClientId = clientId
            }
            send(error, callback)
        }
    }

    override fun commitClosePosition(callback: TransactionCallback) {
        val payload = closePositionPayload()
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)

        transaction(TransactionType.PlaceOrder, string) { response ->
            val error = parseTransactionResponse(response)
            if (error == null) {
                lastOrderClientId = clientId
            }
            send(error, callback)
        }
    }

    override fun commitTransfer(callback: TransactionCallback) {
        val type = stateMachine.state?.input?.transfer?.type
        when (type) {
            TransferType.deposit -> {
                commitDeposit(callback)
            }

            TransferType.withdrawal -> {
                commitWithdrawal(callback)
            }

            TransferType.transferOut -> {
                commitTransferOut(callback)
            }

            else -> {}
        }
    }

    private fun commitDeposit(callback: TransactionCallback) {
        val string = Json.encodeToString(depositPayload())

        transaction(TransactionType.Deposit, string) { response ->
            val error = parseTransactionResponse(response)
            send(error, callback)
        }
    }

    private fun commitWithdrawal(callback: TransactionCallback) {
        val string = Json.encodeToString(withdrawPayload())

        transaction(TransactionType.Withdraw, string) { response ->
            val error = parseTransactionResponse(response)
            send(error, callback)
        }
    }

    private fun commitTransferOut(callback: TransactionCallback) {
        val string = Json.encodeToString(subaccountTransferPayload())

        transaction(TransactionType.PlaceOrder, string) { response ->
            val error = parseTransactionResponse(response)
            send(error, callback)
        }
    }

    override fun faucet(amount: Double, callback: TransactionCallback) {
        val string = Json.encodeToString(faucetPayload(subaccountNumber, amount))

        transaction(TransactionType.Faucet, string) { response ->
            val error = parseFaucetResponse(response)
            send(error, callback)
        }
    }

    private fun parseFaucetResponse(response: String): ParsingError? {
        val result =
            Json.parseToJsonElement(response).jsonObject.toIMap()
        val status = parser.asInt(result.get("status"))
        if (status == 202) {
            return null
        } else if (status != null) {
            return V4TransactionErrors.error(null, "API error: $status")
        } else {
            val resultError = parser.asMap(result.get("error"))
            val message = parser.asString(resultError?.get("message"))
            return V4TransactionErrors.error(null, message ?: "Unknown error")
        }
    }

    override fun cancelOrder(orderId: String, callback: TransactionCallback) {
        val string = Json.encodeToString(cancelOrderPayload(orderId))

        transaction(TransactionType.CancelOrder, string) { response ->
            val error = parseTransactionResponse(response)
            send(error, callback)
        }
    }

    override fun parseTransactionResponse(response: String?): ParsingError? {
        return if (response == null) {
            V4TransactionErrors.error(null, "Unknown error")
        } else {
            val result = Json.parseToJsonElement(response).jsonObject.toIMap()
            val error = parser.asMap(result["error"])
            if (error != null) {
                val message = parser.asString(error["message"])
                val code = parser.asInt(error["code"])
                return V4TransactionErrors.error(code, message)
            } else {
                null
            }
        }
    }

    private fun send(error: ParsingError?, callback: TransactionCallback) {
        if (error != null) {
            callback(false, error)
        } else {
            callback(true, null)
        }
    }

    override fun didUpdateStateForTransfer(data: String?, type: TransferInputField?) {
        super.didUpdateStateForTransfer(data, type)

        val state = stateMachine.state
        if (state?.input?.transfer?.type == TransferType.deposit) {
            if (type == TransferInputField.size) {
                retrieveDepositRoute(state)
            }
        } else if (state?.input?.transfer?.type == TransferType.withdrawal) {
            if (type == TransferInputField.usdcSize ||
                type == TransferInputField.address ||
                type == TransferInputField.chain ||
                type == TransferInputField.token
            ) {
                if (state.input.transfer.size?.usdcSize ?: 0.0 > 0.0) {
                    try {
                        simulateWithdrawal { gasFee ->
                            if (gasFee != null) {
                                retrieveWithdrawalRoute(gasFee)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        retrieveWithdrawalRoute(0.0)
                    }
                }
            }
        }
    }

    override fun transferStatus(hash: String, fromChainId: String?, toChainId: String?) {
        super.transferStatus(hash, fromChainId, toChainId)

        fetchTransferStatus(hash, fromChainId, toChainId)
    }
}