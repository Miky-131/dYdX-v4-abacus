package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol


internal class DepositValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TransferValidatorProtocol {
    override fun validateTransfer(
        wallet: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        transfer: IMap<String, Any>,
        restricted: Boolean
    ): IList<Any>? {
        return null
//        val balance = parser.asDecimal(parser.value(wallet, "balance"))
//        val size = parser.asDecimal(parser.value(transfer, "size.size"))
//        return if (size != null) {
//            if (balance == null) {
//                listOf(
//                    error(
//                        parser,
//                        "WARNING",
//                        "UNKNOWN_WALLET_BALANCE",
//                        null,
//                        null,
//                        "DEPOSIT_MODAL_TITLE.UNKNOWN_WALLET_BALANCE",
//                        "APP.DEPOSIT_MODAL.UNKNOWN_WALLET_BALANCE"
//                    )
//                )
//            } else if (size > balance) {
//                listOf(
//                    error(
//                        parser,
//                        "ERROR",
//                        "AMOUNT_LARGER_THANK_WALLET_BALANCE",
//                        listOf("size.size"),
//                        "APP.TRADE.MODIFY_SIZE_FIELD",
//                        "DEPOSIT_MODAL_TITLE.AMOUNT_LARGER_THANK_WALLET_BALANCE",
//                        "APP.DEPOSIT_MODAL.AMOUNT_LARGER_THANK_WALLET_BALANCE"
//                    )
//                )
//            } else null
//        } else null
    }
}