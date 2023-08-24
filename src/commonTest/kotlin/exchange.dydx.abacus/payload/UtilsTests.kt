package exchange.dydx.abacus.payload

import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.Rounder
import io.ktor.http.*
import numberOfDecimals
import tickDecimals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UtilsTests {
    @Test
    fun testRounder() {
        assertEquals(93.34223, Rounder.round(93.34223, 0.00001))
        assertEquals(93.3422, Rounder.round(93.34223, 0.0001))
        assertEquals(93.342, Rounder.round(93.34223, 0.001))
        assertEquals(93.34, Rounder.round(93.34223, 0.01))
        assertEquals(93.3, Rounder.round(93.34223, 0.1))
        assertEquals(93.0, Rounder.round(93.34223, 1.0))
        assertEquals(90.0, Rounder.round(93.34223, 10.0))
        assertEquals(0.0, Rounder.round(93.34223, 100.0))

        assertEquals(11.0, Rounder.round(11.0, 0.00001))
        assertEquals(11.0, Rounder.round(11.0, 0.001))
        assertEquals(11.0, Rounder.round(11.0, 0.01))
        assertEquals(11.0, Rounder.round(11.0, 0.1))
        assertEquals(11.0, Rounder.round(11.0, 1.0))
        assertEquals(10.0, Rounder.round(11.0, 10.0))
        assertEquals(0.0, Rounder.round(11.0, 100.0))

        assertEquals(1934.0, Rounder.round(1934.0, 0.00001))
        assertEquals(1934.0, Rounder.round(1934.0, 0.0001))
        assertEquals(1934.0, Rounder.round(1934.0, 0.001))
        assertEquals(1934.0, Rounder.round(1934.0, 0.01))
        assertEquals(1934.0, Rounder.round(1934.0, 0.1))
        assertEquals(1934.0, Rounder.round(1934.0, 1.0))
        assertEquals(1930.0, Rounder.round(1934.0, 10.0))
        assertEquals(1900.0, Rounder.round(1934.0, 100.0))
        assertEquals(1000.0, Rounder.round(1934.0, 1000.0))
        assertEquals(0.0, Rounder.round(1934.0, 10000.0))

        assertEquals(1934.0, Rounder.round(1934.0, 0.125))
        assertEquals(1934.0, Rounder.round(1934.0, 0.25))
        assertEquals(1934.0, Rounder.round(1934.0, 0.5))
        assertEquals(1934.0, Rounder.round(1934.0, 0.2))
        assertEquals(1933.75, Rounder.round(1934.0, 1.25))
        assertEquals(1934.0, Rounder.round(1934.0, 2.0))
        assertEquals(1932.5, Rounder.round(1934.0, 2.5))
        assertEquals(1930.0, Rounder.round(1934.0, 5.0))
        assertEquals(1925.0, Rounder.round(1934.0, 25.0))
        assertEquals(1900.0, Rounder.round(1934.0, 50.0))

        assertEquals(34.0, Rounder.round(34.23, 1.0))
        assertEquals(12.75, Rounder.round(12.87, 0.25))
        assertEquals(12.8, Rounder.round(12.87, 0.2))
        assertEquals(12.8, Rounder.round(12.87, 0.1))
        assertEquals(12.87, Rounder.round(12.87, 0.01))
        assertEquals(12.87, Rounder.round(12.87, 0.001))
        assertEquals(23.0, Rounder.round(23.0, 0.001))
        assertEquals(34.23, Rounder.round(34.23, 0.001))
        assertEquals(93.342, Rounder.round(93.34223, 0.001))
        assertEquals(72.34, Rounder.round(72.34, 0.001))
        assertEquals(12.0, Rounder.round(12.87, 1.0))
        assertEquals(12000.0, Rounder.round(12232.87, 1000.0))


        assertEquals(-93.34223, Rounder.round(-93.34223, 0.00001))
        assertEquals(-93.3422, Rounder.round(-93.34223, 0.0001))
        assertEquals(-93.342, Rounder.round(-93.34223, 0.001))
        assertEquals(-93.34, Rounder.round(-93.34223, 0.01))
        assertEquals(-93.3, Rounder.round(-93.34223, 0.1))
        assertEquals(-93.0, Rounder.round(-93.34223, 1.0))
        assertEquals(-90.0, Rounder.round(-93.34223, 10.0))
        assertEquals(0.0, Rounder.round(-93.34223, 100.0))

        assertEquals(-11.0, Rounder.round(-11.0, 0.00001))
        assertEquals(-11.0, Rounder.round(-11.0, 0.001))
        assertEquals(-11.0, Rounder.round(-11.0, 0.01))
        assertEquals(-11.0, Rounder.round(-11.0, 0.1))
        assertEquals(-11.0, Rounder.round(-11.0, 1.0))
        assertEquals(-10.0, Rounder.round(-11.0, 10.0))
        assertEquals(0.0, Rounder.round(-11.0, 100.0))

        assertEquals(-1934.0, Rounder.round(-1934.0, 0.00001))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.0001))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.001))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.01))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.1))
        assertEquals(-1934.0, Rounder.round(-1934.0, 1.0))
        assertEquals(-1930.0, Rounder.round(-1934.0, 10.0))
        assertEquals(-1900.0, Rounder.round(-1934.0, 100.0))
        assertEquals(-1000.0, Rounder.round(-1934.0, 1000.0))
        assertEquals(0.0, Rounder.round(-1934.0, 10000.0))

        assertEquals(-1934.0, Rounder.round(-1934.0, 0.125))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.25))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.5))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.2))
        assertEquals(-1933.75, Rounder.round(-1934.0, 1.25))
        assertEquals(-1934.0, Rounder.round(-1934.0, 2.0))
        assertEquals(-1932.5, Rounder.round(-1934.0, 2.5))
        assertEquals(-1930.0, Rounder.round(-1934.0, 5.0))
        assertEquals(-1925.0, Rounder.round(-1934.0, 25.0))
        assertEquals(-1900.0, Rounder.round(-1934.0, 50.0))

        assertEquals(-34.0, Rounder.round(-34.23, 1.0))
        assertEquals(-12.75, Rounder.round(-12.87, 0.25))
        assertEquals(-12.8, Rounder.round(-12.87, 0.2))
        assertEquals(-12.8, Rounder.round(-12.87, 0.1))
        assertEquals(-12.87, Rounder.round(-12.87, 0.01))
        assertEquals(-12.87, Rounder.round(-12.87, 0.001))
        assertEquals(-23.0, Rounder.round(-23.0, 0.001))
        assertEquals(-34.23, Rounder.round(-34.23, 0.001))
        assertEquals(-93.342, Rounder.round(-93.34223, 0.001))
        assertEquals(-72.34, Rounder.round(-72.34, 0.001))
        assertEquals(-12.0, Rounder.round(-12.87, 1.0))
        assertEquals(-12000.0, Rounder.round(-12232.87, 1000.0))

        assertEquals(12232.87, Rounder.round(12232.87, 0.0))

        val url = URLBuilder("wss://api.dydx.exchange/v3/ws?x=1").build()
        assertNotNull(url)
        assertEquals(url.protocol.name, "wss")
        assertEquals(url.host, "api.dydx.exchange")
        assertEquals(url.encodedPath, "/v3/ws")

        assertEquals(2, 5.31.numberOfDecimals())
        assertEquals(0, 500.0.numberOfDecimals())
        assertEquals(0, 5.0.numberOfDecimals())
        assertEquals(3, 0.001.numberOfDecimals())
        assertEquals(3, 0.005.numberOfDecimals())
        assertEquals(3, 0.025.numberOfDecimals())
        assertEquals(10, 0.0000000025.numberOfDecimals())
        assertEquals(0, 1000000000.0.numberOfDecimals())

        val parser = Parser()
        assertEquals(Numeric.decimal.ONE, 1.0.tickDecimals())
        assertEquals(parser.asDecimal(10.0), 10.0.tickDecimals())
        assertEquals(parser.asDecimal(1.0), 25.0.tickDecimals())
        assertEquals(parser.asDecimal(10.0), 250.0.tickDecimals())
        assertEquals(parser.asDecimal(0.1), 0.1.tickDecimals())
        assertEquals(parser.asDecimal(0.1), 0.5.tickDecimals())
        assertEquals(parser.asDecimal(0.01), 0.25.tickDecimals())

        assertEquals(93.34223, Rounder.round(93.34223, 0.00001))
        assertEquals(93.3422, Rounder.round(93.34223, 0.0001))
        assertEquals(93.342, Rounder.round(93.34223, 0.001))
        assertEquals(93.34, Rounder.round(93.34223, 0.01))
        assertEquals(93.3, Rounder.round(93.34223, 0.1))
        assertEquals(93.0, Rounder.round(93.34223, 1.0))
        assertEquals(90.0, Rounder.round(93.34223, 10.0))
        assertEquals(0.0, Rounder.round(93.34223, 100.0))

        assertEquals(11.0, Rounder.round(11.0, 0.00001))
        assertEquals(11.0, Rounder.round(11.0, 0.001))
        assertEquals(11.0, Rounder.round(11.0, 0.01))
        assertEquals(11.0, Rounder.round(11.0, 0.1))
        assertEquals(11.0, Rounder.round(11.0, 1.0))
        assertEquals(10.0, Rounder.round(11.0, 10.0))
        assertEquals(0.0, Rounder.round(11.0, 100.0))

        assertEquals(1934.0, Rounder.round(1934.0, 0.00001))
        assertEquals(1934.0, Rounder.round(1934.0, 0.0001))
        assertEquals(1934.0, Rounder.round(1934.0, 0.001))
        assertEquals(1934.0, Rounder.round(1934.0, 0.01))
        assertEquals(1934.0, Rounder.round(1934.0, 0.1))
        assertEquals(1934.0, Rounder.round(1934.0, 1.0))
        assertEquals(1930.0, Rounder.round(1934.0, 10.0))
        assertEquals(1900.0, Rounder.round(1934.0, 100.0))
        assertEquals(1000.0, Rounder.round(1934.0, 1000.0))
        assertEquals(0.0, Rounder.round(1934.0, 10000.0))

        assertEquals(1934.0, Rounder.round(1934.0, 0.125))
        assertEquals(1934.0, Rounder.round(1934.0, 0.25))
        assertEquals(1934.0, Rounder.round(1934.0, 0.5))
        assertEquals(1934.0, Rounder.round(1934.0, 0.2))
        assertEquals(1933.75, Rounder.round(1934.0, 1.25))
        assertEquals(1934.0, Rounder.round(1934.0, 2.0))
        assertEquals(1932.5, Rounder.round(1934.0, 2.5))
        assertEquals(1930.0, Rounder.round(1934.0, 5.0))
        assertEquals(1925.0, Rounder.round(1934.0, 25.0))
        assertEquals(1900.0, Rounder.round(1934.0, 50.0))

        assertEquals(34.0, Rounder.round(34.23, 1.0))
        assertEquals(12.75, Rounder.round(12.87, 0.25))
        assertEquals(12.8, Rounder.round(12.87, 0.2))
        assertEquals(12.8, Rounder.round(12.87, 0.1))
        assertEquals(12.87, Rounder.round(12.87, 0.01))
        assertEquals(12.87, Rounder.round(12.87, 0.001))
        assertEquals(23.0, Rounder.round(23.0, 0.001))
        assertEquals(34.23, Rounder.round(34.23, 0.001))
        assertEquals(93.342, Rounder.round(93.34223, 0.001))
        assertEquals(72.34, Rounder.round(72.34, 0.001))
        assertEquals(12.0, Rounder.round(12.87, 1.0))
        assertEquals(12000.0, Rounder.round(12232.87, 1000.0))


        assertEquals(-93.34223, Rounder.round(-93.34223, 0.00001))
        assertEquals(-93.3422, Rounder.round(-93.34223, 0.0001))
        assertEquals(-93.342, Rounder.round(-93.34223, 0.001))
        assertEquals(-93.34, Rounder.round(-93.34223, 0.01))
        assertEquals(-93.3, Rounder.round(-93.34223, 0.1))
        assertEquals(-93.0, Rounder.round(-93.34223, 1.0))
        assertEquals(-90.0, Rounder.round(-93.34223, 10.0))
        assertEquals(0.0, Rounder.round(-93.34223, 100.0))

        assertEquals(-11.0, Rounder.round(-11.0, 0.00001))
        assertEquals(-11.0, Rounder.round(-11.0, 0.001))
        assertEquals(-11.0, Rounder.round(-11.0, 0.01))
        assertEquals(-11.0, Rounder.round(-11.0, 0.1))
        assertEquals(-11.0, Rounder.round(-11.0, 1.0))
        assertEquals(-10.0, Rounder.round(-11.0, 10.0))
        assertEquals(0.0, Rounder.round(-11.0, 100.0))

        assertEquals(-1934.0, Rounder.round(-1934.0, 0.00001))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.0001))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.001))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.01))
        assertEquals(-1934.0, Rounder.round(-1934.0, 0.1))
        assertEquals(-1934.0, Rounder.round(-1934.0, 1.0))
        assertEquals(-1930.0, Rounder.round(-1934.0, 10.0))
        assertEquals(-1900.0, Rounder.round(-1934.0, 100.0))
        assertEquals(-1000.0, Rounder.round(-1934.0, 1000.0))
        assertEquals(0.0, Rounder.round(-1934.0, 10000.0))


        // ---


        assertEquals(93.34223, Rounder.quickRound(93.34223, 0.00001, 5))
        assertEquals(93.3422, Rounder.quickRound(93.34223, 0.0001, 4))
        assertEquals(93.342, Rounder.quickRound(93.34223, 0.001, 3))
        assertEquals(93.34, Rounder.quickRound(93.34223, 0.01, 2))
        assertEquals(93.3, Rounder.quickRound(93.34223, 0.1, 1))
        assertEquals(93.0, Rounder.quickRound(93.34223, 1.0, 0))
        assertEquals(90.0, Rounder.quickRound(93.34223, 10.0, 0))
        assertEquals(100.0, Rounder.quickRound(93.34223, 100.0, 0))

        assertEquals(11.0, Rounder.quickRound(11.0, 0.00001, 5))
        assertEquals(11.0, Rounder.quickRound(11.0, 0.001, 3))
        assertEquals(11.0, Rounder.quickRound(11.0, 0.01, 2))
        assertEquals(11.0, Rounder.quickRound(11.0, 0.1, 1))
        assertEquals(11.0, Rounder.quickRound(11.0, 1.0, 0))
        assertEquals(10.0, Rounder.quickRound(11.0, 10.0, 0))
        assertEquals(0.0, Rounder.quickRound(11.0, 100.0, 0))

        assertEquals(1934.0, Rounder.quickRound(1934.0, 0.00001, 5))
        assertEquals(1934.0, Rounder.quickRound(1934.0, 0.0001, 4))
        assertEquals(1934.0, Rounder.quickRound(1934.0, 0.001, 3))
        assertEquals(1934.0, Rounder.quickRound(1934.0, 0.01, 2))
        assertEquals(1934.0, Rounder.quickRound(1934.0, 0.1, 1))
        assertEquals(1934.0, Rounder.quickRound(1934.0, 1.0, 0))
        assertEquals(1930.0, Rounder.quickRound(1934.0, 10.0, 0))
        assertEquals(1900.0, Rounder.quickRound(1934.0, 100.0, 0))
        assertEquals(2000.0, Rounder.quickRound(1934.0, 1000.0, 0))
        assertEquals(0.0, Rounder.quickRound(1934.0, 10000.0, 0))

        assertEquals(34.0, Rounder.quickRound(34.23, 1.0, 0))
        assertEquals(12.9, Rounder.quickRound(12.87, 0.1, 1))
        assertEquals(12.87, Rounder.quickRound(12.87, 0.01, 2))
        assertEquals(12.87, Rounder.quickRound(12.87, 0.001, 3))
        assertEquals(23.0, Rounder.quickRound(23.0, 0.001, 3))
        assertEquals(34.23, Rounder.quickRound(34.23, 0.001, 3))
        assertEquals(93.342, Rounder.quickRound(93.34223, 0.001, 3))
        assertEquals(72.34, Rounder.quickRound(72.34, 0.001, 3))
        assertEquals(13.0, Rounder.quickRound(12.87, 1.0, 0))
        assertEquals(12000.0, Rounder.quickRound(12232.87, 1000.0, 0))


        assertEquals(-93.34223, Rounder.quickRound(-93.34223, 0.00001, 5))
        assertEquals(-93.3422, Rounder.quickRound(-93.34223, 0.0001, 4))
        assertEquals(-93.342, Rounder.quickRound(-93.34223, 0.001, 3))
        assertEquals(-93.34, Rounder.quickRound(-93.34223, 0.01, 2))
        assertEquals(-93.3, Rounder.quickRound(-93.34223, 0.1, 1))
        assertEquals(-93.0, Rounder.quickRound(-93.34223, 1.0, 0))
        assertEquals(-90.0, Rounder.quickRound(-93.34223, 10.0, 0))
        assertEquals(-100.0, Rounder.quickRound(-93.34223, 100.0, 0))

        assertEquals(-11.0, Rounder.quickRound(-11.0, 0.00001, 5))
        assertEquals(-11.0, Rounder.quickRound(-11.0, 0.001, 3))
        assertEquals(-11.0, Rounder.quickRound(-11.0, 0.01, 2))
        assertEquals(-11.0, Rounder.quickRound(-11.0, 0.1, 1))
        assertEquals(-11.0, Rounder.quickRound(-11.0, 1.0, 0))
        assertEquals(-10.0, Rounder.quickRound(-11.0, 10.0, 0))
        assertEquals(0.0, Rounder.quickRound(-11.0, 100.0, 0))

        assertEquals(-1934.0, Rounder.quickRound(-1934.0, 0.00001, 5))
        assertEquals(-1934.0, Rounder.quickRound(-1934.0, 0.0001, 4))
        assertEquals(-1934.0, Rounder.quickRound(-1934.0, 0.001, 3))
        assertEquals(-1934.0, Rounder.quickRound(-1934.0, 0.01, 2))
        assertEquals(-1934.0, Rounder.quickRound(-1934.0, 0.1, 1))
        assertEquals(-1934.0, Rounder.quickRound(-1934.0, 1.0, 0))
        assertEquals(-1930.0, Rounder.quickRound(-1934.0, 10.0, 0))
        assertEquals(-1900.0, Rounder.quickRound(-1934.0, 100.0, 0))
        assertEquals(-2000.0, Rounder.quickRound(-1934.0, 1000.0, 0))
        assertEquals(0.0, Rounder.quickRound(-1934.0, 10000.0, 0))
    }
}