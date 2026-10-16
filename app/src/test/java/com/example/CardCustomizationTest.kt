package com.example

import com.example.model.WalletCustomizationEntity
import com.example.network.CardCustomizationRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CardCustomizationTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `test card customization request serialization and field names`() {
        val request = CardCustomizationRequest(
            designPreset = "midnight",
            primaryColor = "#112233",
            secondaryColor = "#445566",
            textColor = "#FFFFFF",
            accentColor = "#FF0000",
            artwork = "waves",
            cardStyle = "illustrated"
        )

        val jsonAdapter = moshi.adapter(CardCustomizationRequest::class.java)
        val json = jsonAdapter.toJson(request)

        assertNotNull(json)
        org.junit.Assert.assertTrue(json.contains("\"designPreset\":\"midnight\""))
        org.junit.Assert.assertTrue(json.contains("\"primaryColor\":\"#112233\""))
        org.junit.Assert.assertTrue(json.contains("\"secondaryColor\":\"#445566\""))
        org.junit.Assert.assertTrue(json.contains("\"textColor\":\"#FFFFFF\""))
        org.junit.Assert.assertTrue(json.contains("\"accentColor\":\"#FF0000\""))
        org.junit.Assert.assertTrue(json.contains("\"artwork\":\"waves\""))
        org.junit.Assert.assertTrue(json.contains("\"cardStyle\":\"illustrated\""))

        val parsed = jsonAdapter.fromJson(json)
        assertNotNull(parsed)
        assertEquals(request.designPreset, parsed?.designPreset)
        assertEquals(request.primaryColor, parsed?.primaryColor)
        assertEquals(request.secondaryColor, parsed?.secondaryColor)
        assertEquals(request.textColor, parsed?.textColor)
        assertEquals(request.accentColor, parsed?.accentColor)
        assertEquals(request.artwork, parsed?.artwork)
        assertEquals(request.cardStyle, parsed?.cardStyle)
    }

    @Test
    fun `test card customization partial request with omitted fields`() {
        val request = CardCustomizationRequest(
            designPreset = "sunset",
            primaryColor = "#ABCDEF"
        )

        val jsonAdapter = moshi.adapter(CardCustomizationRequest::class.java)
        val json = jsonAdapter.toJson(request)

        assertNotNull(json)
        org.junit.Assert.assertTrue(json.contains("\"designPreset\":\"sunset\""))
        org.junit.Assert.assertTrue(json.contains("\"primaryColor\":\"#ABCDEF\""))

        val parsed = jsonAdapter.fromJson(json)
        assertNotNull(parsed)
        assertEquals("sunset", parsed?.designPreset)
        assertEquals("#ABCDEF", parsed?.primaryColor)
        assertEquals(null, parsed?.secondaryColor)
    }

    @Test
    fun `test wallet customization entity mapping`() {
        val customization = WalletCustomizationEntity(
            walletId = "wallet-123",
            cardTheme = "emerald",
            primaryColor = "#123456",
            secondaryColor = "#654321",
            accentColor = "#abcdef",
            artwork = "geometric",
            cardStyle = "minimal",
            textColor = "#ffffff"
        )

        assertEquals("wallet-123", customization.walletId)
        assertEquals("emerald", customization.cardTheme)
        assertEquals("#123456", customization.primaryColor)
    }
}
