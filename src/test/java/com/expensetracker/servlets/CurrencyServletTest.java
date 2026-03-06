package com.expensetracker.servlets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CurrencyServlet Tests")
class CurrencyServletTest {

    @Test
    @DisplayName("getSymbol returns ₹ for INR")
    void testINR() {
        assertEquals("₹", CurrencyServlet.getSymbol("INR"));
    }

    @Test
    @DisplayName("getSymbol returns $ for USD")
    void testUSD() {
        assertEquals("$", CurrencyServlet.getSymbol("USD"));
    }

    @Test
    @DisplayName("getSymbol returns € for EUR")
    void testEUR() {
        assertEquals("€", CurrencyServlet.getSymbol("EUR"));
    }

    @Test
    @DisplayName("getSymbol returns £ for GBP")
    void testGBP() {
        assertEquals("£", CurrencyServlet.getSymbol("GBP"));
    }

    @Test
    @DisplayName("getSymbol returns ¥ for JPY")
    void testJPY() {
        assertEquals("¥", CurrencyServlet.getSymbol("JPY"));
    }

    @Test
    @DisplayName("getSymbol returns A$ for AUD")
    void testAUD() {
        assertEquals("A$", CurrencyServlet.getSymbol("AUD"));
    }

    @Test
    @DisplayName("getSymbol returns C$ for CAD")
    void testCAD() {
        assertEquals("C$", CurrencyServlet.getSymbol("CAD"));
    }

    @Test
    @DisplayName("getSymbol returns CHF for CHF")
    void testCHF() {
        assertEquals("CHF", CurrencyServlet.getSymbol("CHF"));
    }

    @Test
    @DisplayName("getSymbol returns ¥ for CNY")
    void testCNY() {
        assertEquals("¥", CurrencyServlet.getSymbol("CNY"));
    }

    @Test
    @DisplayName("getSymbol returns ₿ for BTC")
    void testBTC() {
        assertEquals("₿", CurrencyServlet.getSymbol("BTC"));
    }

    @Test
    @DisplayName("getSymbol returns the code for unknown currency")
    void testUnknown() {
        assertEquals("XYZ", CurrencyServlet.getSymbol("XYZ"));
    }

    @Test
    @DisplayName("getSymbol handles null input")
    void testNull() {
        // Should return default or handle gracefully
        String result = CurrencyServlet.getSymbol(null);
        assertNotNull(result);
    }
}
