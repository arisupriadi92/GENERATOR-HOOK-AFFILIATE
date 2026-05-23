package com.example

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Hook Gen - Telatenpedia", appName)
  }

  @Test
  fun `verify drawable resource logo loads successfully`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = context.getDrawable(R.drawable.affiliate_hook_icon_1779478713137)
    org.junit.Assert.assertNotNull("The logo drawable should not be null", drawable)
  }

  @Test
  fun `verify main activity launches without crashing`() {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      org.junit.Assert.assertNotNull("MainActivity should launch successfully", scenario)
    }
  }

  @Test
  fun `verify HookViewModel can check API configuration and seed database`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.example.data.local.AppDatabase.getDatabase(context)
    val repository = com.example.data.repository.HookRepository(database.hookDao(), database.popularHookDao())
    val viewModel = com.example.ui.viewmodel.HookViewModel(repository)
    
    // Check API Key Configured doesn't crash
    val configured = viewModel.isApiKeyConfigured()
    println("API Key Configured Status: $configured")
    
    // Verify saved and popular hooks flows don't crash
    val saved = viewModel.savedHooks.value
    val popular = viewModel.popularHooks.value
    org.junit.Assert.assertNotNull(saved)
    org.junit.Assert.assertNotNull(popular)
  }
}
