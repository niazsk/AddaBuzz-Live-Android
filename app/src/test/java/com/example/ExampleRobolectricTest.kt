package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AddaBuzzRepository
import com.example.data.LiveKitManager
import com.example.data.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AddaBuzz Live", appName)
    }

    @Test
    fun `verify AddaBuzzRepository and 8 multi-guest seats initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = SessionManager(context)
        val liveKitManager = LiveKitManager(context)
        val repository = AddaBuzzRepository(context, sessionManager, liveKitManager)

        assertNotNull(repository.currentRoom)
        assertEquals(8, repository.guestSeats.value.size)
        assertTrue(repository.allRooms.value.isNotEmpty())
        assertTrue(repository.availableGifts.size >= 6)
        assertTrue(repository.rechargePackages.isNotEmpty())
    }

    @Test
    fun `verify wallet balance and gift sending logic`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessionManager = SessionManager(context)
        val liveKitManager = LiveKitManager(context)
        val repository = AddaBuzzRepository(context, sessionManager, liveKitManager)

        val initialCoins = sessionManager.currentUser.value?.coins ?: 0L
        val roseGift = repository.availableGifts.first { it.id == "gift_rose" }
        val sent = repository.sendGift(roseGift)

        assertTrue(sent)
        val updatedCoins = sessionManager.currentUser.value?.coins ?: 0L
        assertEquals(initialCoins - roseGift.coinPrice, updatedCoins)
    }
}
