package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeneratedHook
import com.example.data.api.AnalyzedProductResponse
import com.example.data.model.SavedHook
import com.example.data.model.PopularHook
import com.example.data.repository.HookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HookViewModel(private val repository: HookRepository) : ViewModel() {

    // Google Sign-In States
    var isGoogleLoggedIn by mutableStateOf(false)
        private set
    var googleEmail by mutableStateOf("")
        private set
    var googleName by mutableStateOf("")
        private set
    var isAuthInProgress by mutableStateOf(false)

    fun loadSavedUserSession(context: android.content.Context) {
        val prefs = context.applicationContext.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        isGoogleLoggedIn = prefs.getBoolean("is_logged_in", false)
        googleEmail = prefs.getString("google_email", "") ?: ""
        googleName = prefs.getString("google_name", "") ?: ""
    }

    fun loginWithGoogle(context: android.content.Context, name: String, email: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isAuthInProgress = true
            kotlinx.coroutines.delay(1000) // Realistic connection latency
            val prefs = context.applicationContext.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("is_logged_in", true)
            editor.putString("google_email", email)
            editor.putString("google_name", name)
            editor.apply()
            
            googleEmail = email
            googleName = name
            
            // First complete local UI dialog state dismissal safely
            onComplete()
            
            // Allow a short duration buffer for AlertDialog to dismiss cleanly and detach from window
            kotlinx.coroutines.delay(200)
            
            // Finally transition to the main Scaffold content
            isGoogleLoggedIn = true
            isAuthInProgress = false
        }
    }

    fun logoutGoogle(context: android.content.Context, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val prefs = context.applicationContext.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            
            // First invoke onComplete callback to hide the profile dialog cleanly
            onComplete()
            
            // Allow a brief delay for screen focus transfer and dialog close transition
            kotlinx.coroutines.delay(200)
            
            isGoogleLoggedIn = false
            googleEmail = ""
            googleName = ""
        }
    }

    // Input States
    var productName by mutableStateOf("")
    var selectedNiche by mutableStateOf("Kosmetik & Skincare")
    var selectedHookType by mutableStateOf("Masalah & Solusi")
    var selectedPlatform by mutableStateOf("TikTok")
    var targetAudience by mutableStateOf("")
    var uniqueSellingPoints by mutableStateOf("")
    var additionalNotes by mutableStateOf("")

    // Generation UI States
    var isGenerating by mutableStateOf(false)
    var isGeneratingMore by mutableStateOf(false)
    var generationError by mutableStateOf<String?>(null)
    var generatedHooks by mutableStateOf<List<GeneratedHook>>(emptyList())

    // Camera/Image AI Analysis States
    var productBitmap by mutableStateOf<android.graphics.Bitmap?>(null)
    var isAnalyzingImage by mutableStateOf(false)
    var imageAnalysisError by mutableStateOf<String?>(null)
    var cameraAnalyzedResponse by mutableStateOf<com.example.data.api.AnalyzedProductResponse?>(null)

    fun clearImage() {
        productBitmap = null
        cameraAnalyzedResponse = null
        imageAnalysisError = null
    }

    fun analyzeImageAndGenerate(bitmap: android.graphics.Bitmap, context: android.content.Context) {
        viewModelScope.launch {
            isAnalyzingImage = true
            imageAnalysisError = null
            productBitmap = bitmap
            generatedHooks = emptyList() // clear previous hooks to show loading
            
            try {
                val result = repository.analyzeProductImage(bitmap)
                cameraAnalyzedResponse = result
                
                // Populate text form inputs so user can see what was detected and can refine if desired
                productName = result.productName
                val validNiches = listOf(
                    "Cosmetik & Skincare",
                    "Alat Dapur & Rumah Tangga",
                    "Fashion & Aksesoris",
                    "Elektronik & Gadget",
                    "Makanan & Kuliner",
                    "Keuangan & Investasi",
                    "Kesehatan & Diet",
                    "Otomotif & Hobby"
                )
                selectedNiche = if (result.niche in validNiches) result.niche else "Cosmetik & Skincare"
                uniqueSellingPoints = result.usp
                targetAudience = result.targetAudience
                
                // Directly set the results as the generated hooks
                generatedHooks = result.hooks
                
            } catch (e: Exception) {
                imageAnalysisError = "Gagal menganalisis gambar: ${e.message}"
            } finally {
                isAnalyzingImage = false
            }
        }
    }

    // Database Observed States
    val savedHooks: StateFlow<List<SavedHook>> = repository.savedHooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val popularHooks: StateFlow<List<PopularHook>> = repository.popularHooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Run database seeding for Popular Hooks on VM initialization
        viewModelScope.launch {
            repository.checkAndSeedPopularHooks()
        }
    }

    fun generateHooks() {
        if (productName.isBlank()) {
            generationError = "Nama Produk / Jasa tidak boleh kosong"
            return
        }
        if (targetAudience.isBlank()) {
            generationError = "Audiens Target Utama tidak boleh kosong (misal: 'ibu muda')"
            return
        }

        viewModelScope.launch {
            isGenerating = true
            generationError = null
            try {
                val results = repository.generateHooks(
                    productName = productName,
                    niche = selectedNiche,
                    hookType = selectedHookType,
                    platform = selectedPlatform,
                    targetAudience = targetAudience,
                    sellingPoints = uniqueSellingPoints,
                    additionalNotes = additionalNotes
                )
                generatedHooks = results
                if (results.isEmpty()) {
                    generationError = "Gagal membuat hook. Silakan coba lagi."
                }
            } catch (e: Exception) {
                generationError = "Terjadi kesalahan: ${e.message}"
            } finally {
                isGenerating = false
            }
        }
    }

    fun loadMoreHooks() {
        if (productName.isBlank()) {
            generationError = "Nama Produk / Jasa tidak boleh kosong"
            return
        }
        if (targetAudience.isBlank()) {
            generationError = "Audiens Target Utama tidak boleh kosong"
            return
        }

        viewModelScope.launch {
            isGeneratingMore = true
            generationError = null
            try {
                val results = repository.generateHooks(
                    productName = productName,
                    niche = selectedNiche,
                    hookType = selectedHookType,
                    platform = selectedPlatform,
                    targetAudience = targetAudience,
                    sellingPoints = uniqueSellingPoints,
                    additionalNotes = if (additionalNotes.isBlank()) "Variasikan kalimat baru yang menarik dan berbeda dari sebelumnya" else additionalNotes + " (Variasikan kalimat baru yang menarik dan berbeda dari sebelumnya)"
                )
                if (results.isNotEmpty()) {
                    generatedHooks = generatedHooks + results
                } else {
                    generationError = "Gagal memuat alternatif hook tambahan."
                }
            } catch (e: Exception) {
                generationError = "Gagal memuat lebih banyak: ${e.message}"
            } finally {
                isGeneratingMore = false
            }
        }
    }

    fun toggleSaveHook(hook: GeneratedHook) {
        viewModelScope.launch {
            val alreadySaved = savedHooks.value.any { it.hookText == hook.hookText }
            if (alreadySaved) {
                val match = savedHooks.value.firstOrNull { it.hookText == hook.hookText }
                if (match != null) {
                    repository.deleteHook(match)
                }
            } else {
                repository.insertHook(
                    SavedHook(
                        productName = productName,
                        niche = selectedNiche,
                        hookType = selectedHookType,
                        platform = selectedPlatform,
                        hookText = hook.hookText,
                        videoScenario = hook.videoScenario,
                        ctaText = hook.ctaText
                    )
                )
            }
        }
    }

    // Direct toggle save for Popular Hook
    fun toggleSavePopularHook(popular: PopularHook) {
        viewModelScope.launch {
            val alreadySaved = savedHooks.value.any { it.hookText == popular.hookText }
            if (alreadySaved) {
                val match = savedHooks.value.firstOrNull { it.hookText == popular.hookText }
                if (match != null) {
                    repository.deleteHook(match)
                }
            } else {
                repository.insertHook(
                    SavedHook(
                        productName = "Produk Populer",
                        niche = popular.niche,
                        hookType = popular.hookType,
                        platform = popular.platform,
                        hookText = popular.hookText,
                        videoScenario = popular.videoScenario,
                        ctaText = popular.ctaText
                    )
                )
            }
        }
    }

    fun saveDirectHook(savedHook: SavedHook) {
        viewModelScope.launch {
            repository.insertHook(savedHook)
        }
    }

    fun deleteHook(hook: SavedHook) {
        viewModelScope.launch {
            repository.deleteHook(hook)
        }
    }

    fun isHookSaved(hookText: String): Boolean {
        return savedHooks.value.any { it.hookText == hookText }
    }

    // Copy / Share Tracking Actions
    fun recordPopularCopy(hook: PopularHook) {
        viewModelScope.launch {
            repository.incrementPopularCopyCount(hook.id)
        }
    }

    fun recordPopularShare(hook: PopularHook) {
        viewModelScope.launch {
            repository.incrementPopularShareCount(hook.id)
        }
    }

    fun recordGeneratedCopy(hook: GeneratedHook) {
        viewModelScope.launch {
            repository.trackCopy(
                hookText = hook.hookText,
                videoScenario = hook.videoScenario,
                ctaText = hook.ctaText,
                niche = selectedNiche,
                hookType = selectedHookType,
                platform = selectedPlatform
            )
        }
    }

    fun recordGeneratedShare(hook: GeneratedHook) {
        viewModelScope.launch {
            repository.trackShare(
                hookText = hook.hookText,
                videoScenario = hook.videoScenario,
                ctaText = hook.ctaText,
                niche = selectedNiche,
                hookType = selectedHookType,
                platform = selectedPlatform
            )
        }
    }

    fun recordSavedCopy(hook: SavedHook) {
        viewModelScope.launch {
            repository.trackCopy(
                hookText = hook.hookText,
                videoScenario = hook.videoScenario,
                ctaText = hook.ctaText,
                niche = hook.niche,
                hookType = hook.hookType,
                platform = hook.platform
            )
        }
    }

    fun recordSavedShare(hook: SavedHook) {
        viewModelScope.launch {
            repository.trackShare(
                hookText = hook.hookText,
                videoScenario = hook.videoScenario,
                ctaText = hook.ctaText,
                niche = hook.niche,
                hookType = hook.hookType,
                platform = hook.platform
            )
        }
    }
}

class HookViewModelFactory(private val repository: HookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
