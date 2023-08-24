package exchange.dydx.abacus.validation

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.modal.trade

open class ValidationsTests : V3BaseTests() {
    override fun setup() {
        test({
            loadValidationsMarkets()
        }, null)

        test({
            loadMarketsConfigurations()
        }, null)

        test({
            loadValidationsAccounts()
        }, null)

        test({
            loadUser()
        }, null)

        test({
            loadValidationsOrderbook()
        }, null)
    }

    private fun loadValidationsMarkets(): StateResponse {
        return perp.socket(mock.socketUrl, mock.validationsMock.marketsSubscribed, 0, null)
    }

    private fun loadValidationsAccounts(): StateResponse {
        return perp.socket(mock.socketUrl, mock.validationsMock.accountsSubscribed, 0, null)
    }

    private fun loadValidationsOrderbook(): StateResponse {
        return perp.socket(mock.socketUrl, mock.validationsMock.orderbookSubscribed, 0, null)
    }

    override fun reset() {
        super.reset()
        test({
            perp.trade(null, null, 0)
        }, null)

    }

}