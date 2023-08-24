package exchange.dydx.abacus.state.app.adaptors

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.utils.iMapOf

class V4TransactionErrors {
    companion object {
        private val errorMap = iMapOf(
            2 to "TRANSACTIONERROR.ORDER.2_DOES_NOT_EXIST_IN_MEMSTORE",
            3 to "TRANSACTIONERROR.ORDER.3_INVALID_SIDE",
            4 to "TRANSACTIONERROR.ORDER.4_INVALID_QUANTUM",
            5 to "TRANSACTIONERROR.ORDER.5_INVALID_GOODTILBLOCK",
            6 to "TRANSACTIONERROR.ORDER.6_INVALID_SUBTICKS",
            7 to "TRANSACTIONERROR.ORDER.7_ALREDAY_CANCELED",
            8 to "TRANSACTIONERROR.ORDER.8_COLLATERALIZATION_CHECK_FAILED",
            9 to "TRANSACTIONERROR.ORDER.9_INVALID_CLOBPAIR_ID",
            10 to "TRANSACTIONERROR.ORDER.10_CANCEL_EXISTS_WITH_GREATER_OR_EQUAL_GOODTILBLOCK",
            11 to "TRANSACTIONERROR.ORDER.11_GOODTILBLOCK_TOO_LOW",
            12 to "TRANSACTIONERROR.ORDER.12_GOODTILBLOCK_TOO_FAR",
            13 to "TRANSACTIONERROR.ORDER.13_INVALID_MSGPLACEORDER",
            14 to "TRANSACTIONERROR.ORDER.14_INVALID_MSGPROPOSEDMATCHORDERS",
            15 to "TRANSACTIONERROR.ORDER.15_FILLED_AMOUNT_CANNOT_BE_UNCHANGED",
            16 to "TRANSACTIONERROR.ORDER.16_FILLED_AMOUNT_CANNOT_DECREASE",
            17 to "TRANSACTIONERROR.ORDER.17_CANNOT_PRUNE_NON_EXISTENT_FILL",
            18 to "TRANSACTIONERROR.ORDER.18_CANNOT_OPEN_MORE_ORDERS",
            19 to "TRANSACTIONERROR.ORDER.19_FILLAMOUNT_NOT_DIVISIBLE_BY_STEPBASEQUANTUMS",
            20 to "TRANSACTIONERROR.ORDER.20_NO_ASSOCIATED_CLOB_PAIRS",
            21 to "TRANSACTIONERROR.ORDER.21_DUPLICATE_ORDER_ID",
            22 to "TRANSACTIONERROR.ORDER.22_MISMATCH_CLOBPAIR_AND_PERPETUAL_ID",
            23 to "TRANSACTIONERROR.ORDER.23_NEGATIVE_FEE",
            24 to "TRANSACTIONERROR.ORDER.24_FEE_TRANSFER_FAILED",
            25 to "TRANSACTIONERROR.ORDER.25_ORDER_FULLY_FILLED",
            26 to "TRANSACTIONERROR.ORDER.26_PRICE_PREMIUM_WITH_NON_PERPETUAL_CLOB_PAIR",
            27 to "TRANSACTIONERROR.ORDER.27_MAXABSPREMIUMVOTEPPM_INT32_OVERFLOW",
            28 to "TRANSACTIONERROR.ORDER.28_INDEX_IS_ZERO",
            29 to "TRANSACTIONERROR.ORDER.29_INVALID_CLOBPAIR_PARAMETER",
            1000 to "TRANSACTIONERROR.LIQUIDATION.1000_INVALID_LIQUIDATIONSCONFIG",
            1001 to "TRANSACTIONERROR.LIQUIDATION.1001_NO_PERPETUAL_POSITION_TO_LIQUIDATE",
            1002 to "TRANSACTIONERROR.LIQUIDATION.1002_SUBACCOUNT_NOT_LIQUIDABLE",
            1003 to "TRANSACTIONERROR.LIQUIDATION.1003_NO_OPEN_PERPETUAL_POSITION",
            1004 to "TRANSACTIONERROR.LIQUIDATION.1004_INVALID_SIZE",
            1005 to "TRANSACTIONERROR.LIQUIDATION.1005_WRONG_SIDE",
            1006 to "TRANSACTIONERROR.LIQUIDATION.1006_TOTAL_FILLS_EXCEEDS_SIZE",
            1007 to "TRANSACTIONERROR.LIQUIDATION.1007_DOES_NOT_CONTAIN_FILLS",
            1008 to "TRANSACTIONERROR.LIQUIDATION.1008_PREVIOUSLY_LIQUIDATED_IN_CURRENT_BLOCK",
            1009 to "TRANSACTIONERROR.LIQUIDATION.1009_ORDER_SIZE_TOO_SMALL",
            1010 to "TRANSACTIONERROR.LIQUIDATION.1010_ORDER_SIZE_TOO_LARGE",
            1011 to "TRANSACTIONERROR.LIQUIDATION.1011_ORDER_SIZE_TOO_LARGE_SUBACCOUNT",
            1012 to "TRANSACTIONERROR.LIQUIDATION.1012_ORDER_SIZE_TOO_LARGE_INSURANCE_PAYOUT",
            1013 to "TRANSACTIONERROR.LIQUIDATION.1013_INSUFFICIENT_INSURANCE_FUND",
            1014 to "TRANSACTIONERROR.LIQUIDATION.1014_INVALID_PERPETUAL_POSITION_SIZE_DELTA",
            1015 to "TRANSACTIONERROR.LIQUIDATION.1015_INVALID_DELTA_BASE",
            2000 to "TRANSACTIONERROR.ORDER.2000_CANNOT_FULLY_FILL_FOK",
            2001 to "TRANSACTIONERROR.ORDER.2001_REDUCEONLY_INCREASING_SIZE",
            2002 to "TRANSACTIONERROR.ORDER.2002_REDUCEONLY_CHANGING_SIDE",
            2003 to "TRANSACTIONERROR.ORDER.2003_POSTONLY_CROSS_MAKER_ORDERS",
            3000 to "TRANSACTIONERROR.ORDER.3000_INVALID_ORDER_FLAGS",
            3001 to "TRANSACTIONERROR.ORDER.3001_INVALID_ORDER_GOODTILBLOCKTIME",
            3002 to "TRANSACTIONERROR.ORDER.3002_LONGTERM_ORDER_REQUIRING_IMMEDIATE_EXECUTION",
            3003 to "TRANSACTIONERROR.ORDER.3003_GOODTILBLOCKTIME_TOO_LOW",
            3004 to "TRANSACTIONERROR.ORDER.3004_GOODTILBLOCKTIME_TOO_FAR",
            3005 to "TRANSACTIONERROR.ORDER.3005_EXISTING_STATEFUL_ORDER",
            3006 to "TRANSACTIONERROR.ORDER.3006_STATEFUL_ORDER_DOES_NOT_EXIST",
            4000 to "TRANSACTIONERROR.ORDER.4000_INVALID_MSGPROPOSEDOPERATIONS",
            4001 to "TRANSACTIONERROR.ORDER.4001_INVALID_MATCH_ORDER",
            4002 to "TRANSACTIONERROR.ORDER.4002_SHORT_TERM_ORDER_NOT_IN_SAME_BLOCK",
            4003 to "TRANSACTIONERROR.ORDER.4003_ZERO_FILL_AMOUNT",
            4004 to "TRANSACTIONERROR.ORDER.4004_INVALID_OPERATION_QUEUE",
            9000 to "TRANSACTIONERROR.UNIMPLEMENTED.9000_UNIMPLEMENTED_ASSET_ORDERS",
            9001 to "TRANSACTIONERROR.UNIMPLEMENTED.9001_UNIMPLEMENTED_NON_USDC_UPDATING",
            9002 to "TRANSACTIONERROR.UNIMPLEMENTED.9002_UNIMPLEMENTED",
        )

        fun error(code: Int?, message: String?): ParsingError? {
            return if (code != null) {
                if (code != 0) {
                    val stringKey = errorMap[code]
                    ParsingError(
                        ParsingErrorType.BackendError,
                        message ?: "Unknown error",
                        stringKey
                    )
                } else null
            } else {
                ParsingError(ParsingErrorType.BackendError, message ?: "Unknown error", null)
            }
        }
    }
}