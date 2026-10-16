package com.example

import com.example.model.WalletEntity
import com.example.network.ApiResponse
import com.example.network.ServerWallet
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WalletMoshiParsingTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `test server wallet parsing with null balance does not throw exception`() {
        val json = """
            {
              "id": "wallet-website-1",
              "bankName": "Website Bank",
              "cardType": "checking",
              "cardNumber": "1234",
              "cardHolderName": "John Doe",
              "balance": null,
              "creditLimit": null
            }
        """.trimIndent()

        val adapter = moshi.adapter(ServerWallet::class.java)
        val serverWallet = adapter.fromJson(json)

        assertNotNull(serverWallet)
        assertEquals("wallet-website-1", serverWallet?.id)
        assertEquals("Website Bank", serverWallet?.bankName)
        assertNull(serverWallet?.balance)
        assertNull(serverWallet?.creditLimit)
    }

    @Test
    fun `test api response list of server wallets with null balance at index 5`() {
        val listType = Types.newParameterizedType(List::class.java, ServerWallet::class.java)
        val responseType = Types.newParameterizedType(ApiResponse::class.java, listType)
        val adapter = moshi.adapter<ApiResponse<List<ServerWallet>>>(responseType)

        val json = """
            {
              "success": true,
              "status": "success",
              "data": [
                {"id": "w1", "balance": 100.0},
                {"id": "w2", "balance": 200.0},
                {"id": "w3", "balance": 300.0},
                {"id": "w4", "balance": 400.0},
                {"id": "w5", "balance": 500.0},
                {"id": "w6", "balance": null}
              ]
            }
        """.trimIndent()

        val response = adapter.fromJson(json)
        assertNotNull(response)
        assertEquals(true, response?.success)
        assertNotNull(response?.data)
        assertEquals(6, response?.data?.size)
        assertNull(response?.data?.get(5)?.balance)
    }

    @Test
    fun `test fallback balance when server balance is null and local exists`() {
        val serverWallet = ServerWallet(
            id = "w6",
            bankName = "Website Bank",
            balance = null
        )

        val existingWallet = WalletEntity(
            id = "w6",
            bankName = "Website Bank",
            cardType = "checking",
            cardNumber = "1234",
            balance = 1500.0
        )

        // Simulating robust DTO mapping logic
        val resolvedBalance = serverWallet.balance ?: existingWallet.balance
        assertEquals(1500.0, resolvedBalance, 0.001)
    }

    @Test
    fun `test fallback balance when server balance is null and local does not exist`() {
        val serverWallet = ServerWallet(
            id = "w7",
            bankName = "External Bank",
            balance = null
        )

        val existingWallet: WalletEntity? = null
        val resolvedBalance = serverWallet.balance ?: existingWallet?.balance ?: 0.0
        assertEquals(0.0, resolvedBalance, 0.001)
    }
}
