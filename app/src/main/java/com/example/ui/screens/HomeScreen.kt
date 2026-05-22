package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeneratedHook
import com.example.data.model.SavedHook
import com.example.data.model.PopularHook
import com.example.ui.viewmodel.HookViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HookViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0: Generator, 1: Terpopuler, 2: Koleksi
    val savedHooks by viewModel.savedHooks.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSavedUserSession(context)
    }

    if (!viewModel.isGoogleLoggedIn) {
        GoogleLoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NaturalBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NaturalPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Sparkle Icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HookGen",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = NaturalOnBackground
                            )
                            Text(
                                text = "Ubah Penonton Jadi Pembeli",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = NaturalSecondaryText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NaturalBackground
                ),
                actions = {
                    // Google Profile View
                    var showProfileDialog by remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(NaturalActivePill)
                            .clickable { showProfileDialog = true }
                            .border(1.dp, NaturalPillBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Initials of the user name
                        val initials = if (viewModel.googleName.isNotEmpty()) {
                            viewModel.googleName.split(" ").map { it.take(1) }.joinToString("").take(2).uppercase()
                        } else {
                            "G"
                        }
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalOnBackground
                        )
                    }

                    if (showProfileDialog) {
                        AlertDialog(
                            onDismissRequest = { showProfileDialog = false },
                            containerColor = NaturalContainer,
                            tonalElevation = 8.dp,
                            confirmButton = {
                                TextButton(
                                    onClick = { showProfileDialog = false },
                                    colors = ButtonDefaults.textButtonColors(contentColor = NaturalPrimary)
                                ) {
                                    Text("Tutup", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.logoutGoogle(context, onComplete = { showProfileDialog = false })
                                        Toast.makeText(context, "Berhasil keluar dari akun Google", Toast.LENGTH_SHORT).show()
                                    },
                                    border = borderStroke(0.5.dp, CoralRed.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Keluar",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Keluar", fontSize = 12.sp)
                                }
                            },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(NaturalPrimary, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Connected",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Akun Google Terhubung",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NaturalOnBackground
                                    )
                                }
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Anda masuk sebagai:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NaturalSecondaryText
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = NaturalTextFieldBg),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(NaturalPrimary, RoundedCornerShape(20.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val letter = viewModel.googleName.firstOrNull()?.uppercase() ?: "G"
                                                Text(
                                                    text = letter.toString(),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = viewModel.googleName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = NaturalOnBackground
                                                )
                                                Text(
                                                    text = viewModel.googleEmail,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = NaturalSecondaryText
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Status: Semua koneksi API Key kecerdasan buatan terenkripsi dan terverifikasi secara aman melalui identitas Google Anda.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = NaturalMutedText
                                    )
                                }
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            val infoText = "Aplikasi Generator Hook Affiliate\nMenggunakan model kecerdasan buatan gemini-3.5-flash dengan sistem personalisasi target audiens dan penelusuran hook terpopuler ter-update."
                            Toast.makeText(context, infoText, Toast.LENGTH_LONG).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informasi Aplikasi",
                            tint = NaturalPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NaturalContainer,
                tonalElevation = 4.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Buat Hook") },
                    label = { Text("Beranda", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalOnBackground,
                        selectedTextColor = NaturalOnBackground,
                        indicatorColor = NaturalActivePill,
                        unselectedIconColor = NaturalSecondaryText,
                        unselectedTextColor = NaturalSecondaryText
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Hook Terpopuler") },
                    label = { Text("Terpopuler", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalOnBackground,
                        selectedTextColor = NaturalOnBackground,
                        indicatorColor = NaturalActivePill,
                        unselectedIconColor = NaturalSecondaryText,
                        unselectedTextColor = NaturalSecondaryText
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Hook Tersimpan") },
                    label = { Text("Koleksi", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalOnBackground,
                        selectedTextColor = NaturalOnBackground,
                        indicatorColor = NaturalActivePill,
                        unselectedIconColor = NaturalSecondaryText,
                        unselectedTextColor = NaturalSecondaryText
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NaturalBackground)
                .padding(innerPadding)
        ) {

            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> GeneratorTab(viewModel = viewModel, context = context)
                    1 -> PopularTab(viewModel = viewModel, context = context)
                    2 -> SavedTab(viewModel = viewModel, savedHooks = savedHooks, context = context)
                }
            }
        }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratorTab(viewModel: HookViewModel, context: Context) {
    var expandedNiche by remember { mutableStateOf(false) }

    val niches = listOf(
        "Cosmetik & Skincare",
        "Alat Dapur & Rumah Tangga",
        "Fashion & Aksesoris",
        "Elektronik & Gadget",
        "Makanan & Kuliner",
        "Keuangan & Investasi",
        "Kesehatan & Diet",
        "Otomotif & Hobby"
    )

    val hookTypes = listOf(
        "FOMO / Urgensi",
        "Masalah & Solusi",
        "Edukasi / Tips",
        "Storytelling / Curhat",
        "Kontroversial / Pikiran Terbuka"
    )

    val platforms = listOf("TikTok", "Instagram Reels", "YouTube Shorts", "Written Post")

    // Filter states for GENERATED results inside tab 0
    var resultKeyword by remember { mutableStateOf("") }
    var resultLengthFilter by remember { mutableStateOf("Semua") }
    var resultCtaFilter by remember { mutableStateOf("Semua") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Tips banner card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NaturalContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HighlightGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Tips",
                            tint = HighlightGreenText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💡 Formula Emas Hook Viral",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalOnBackground
                        )
                        Text(
                            text = "Gabungkan kegelisahan target audiens dengan USP produk unikmu di 3 detik pemicu video promosi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NaturalSecondaryText,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Input configuration Section
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NaturalContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NaturalPillBorder, RoundedCornerShape(20.dp))
                    .testTag("generator_input_form")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Konfigurasi Personalisasi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NaturalOnBackground
                    )

                    // Product Name field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Nama Produk / Jasa *",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalSecondaryText
                        )
                        OutlinedTextField(
                            value = viewModel.productName,
                            onValueChange = { viewModel.productName = it },
                            placeholder = { Text("Misal: Serum Glowing Bakuchiol, Sapu Otomatis", color = NaturalPlaceholderText) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = NaturalOnBackground,
                                unfocusedTextColor = NaturalOnBackground,
                                focusedContainerColor = NaturalTextFieldBg,
                                unfocusedContainerColor = NaturalTextFieldBg,
                                focusedIndicatorColor = NaturalPrimary,
                                unfocusedIndicatorColor = NaturalPillBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_name_input"),
                            singleLine = true
                        )
                    }

                    // Target Audience field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Audiens Target Utama *",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalSecondaryText
                        )
                        OutlinedTextField(
                            value = viewModel.targetAudience,
                            onValueChange = { viewModel.targetAudience = it },
                            placeholder = { Text("Misal: Ibu muda menyusui, gamer HP, anak kos hemat", color = NaturalPlaceholderText) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = NaturalOnBackground,
                                unfocusedTextColor = NaturalOnBackground,
                                focusedContainerColor = NaturalTextFieldBg,
                                unfocusedContainerColor = NaturalTextFieldBg,
                                focusedIndicatorColor = NaturalPrimary,
                                unfocusedIndicatorColor = NaturalPillBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("target_audience_input"),
                            singleLine = true
                        )
                    }

                    // Unique Selling Points (USP) field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Poin Penjualan Utama (USP) - Opsional",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalSecondaryText
                        )
                        OutlinedTextField(
                            value = viewModel.uniqueSellingPoints,
                            onValueChange = { viewModel.uniqueSellingPoints = it },
                            placeholder = { Text("Misal: Diskon 50% khusus hari ini, anti lengket, garansi 1 tahun", color = NaturalPlaceholderText) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = NaturalOnBackground,
                                unfocusedTextColor = NaturalOnBackground,
                                focusedContainerColor = NaturalTextFieldBg,
                                unfocusedContainerColor = NaturalTextFieldBg,
                                focusedIndicatorColor = NaturalPrimary,
                                unfocusedIndicatorColor = NaturalPillBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("usp_input"),
                            singleLine = true
                        )
                    }

                    // Dropdown for Niche
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Kategori Niche",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalSecondaryText
                        )
                        Box {
                            OutlinedTextField(
                                value = viewModel.selectedNiche,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expandedNiche = !expandedNiche }) {
                                        Icon(
                                            imageVector = if (expandedNiche) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = NaturalOnBackground,
                                    unfocusedTextColor = NaturalOnBackground,
                                    focusedContainerColor = NaturalTextFieldBg,
                                    unfocusedContainerColor = NaturalTextFieldBg,
                                    focusedIndicatorColor = NaturalPrimary,
                                    unfocusedIndicatorColor = NaturalPillBorder
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedNiche = !expandedNiche }
                                    .testTag("niche_dropdown_input")
                            )

                            DropdownMenu(
                                expanded = expandedNiche,
                                onDismissRequest = { expandedNiche = false },
                                modifier = Modifier
                                    .background(NaturalContainer)
                                    .border(1.dp, NaturalPillBorder, RoundedCornerShape(8.dp))
                            ) {
                                niches.forEach { niche ->
                                    DropdownMenuItem(
                                        text = { Text(niche, color = NaturalOnBackground) },
                                        onClick = {
                                            viewModel.selectedNiche = niche
                                            expandedNiche = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Platform select Pills
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Platform Target",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalSecondaryText
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            platforms.forEach { platform ->
                                val isSelected = viewModel.selectedPlatform == platform
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(if (isSelected) NaturalActivePill else NaturalTextFieldBg)
                                        .border(1.dp, NaturalPillBorder, RoundedCornerShape(30.dp))
                                        .clickable { viewModel.selectedPlatform = platform }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("platform_pill_$platform")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = "Selected",
                                                tint = NaturalOnBackground,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = platform,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            color = if (isSelected) NaturalOnBackground else NaturalSecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Hook type select Pills
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Sudut Pandang / Tipe Hook",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalSecondaryText
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            hookTypes.forEach { type ->
                                val isSelected = viewModel.selectedHookType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(if (isSelected) NaturalActivePill else NaturalTextFieldBg)
                                        .border(1.dp, NaturalPillBorder, RoundedCornerShape(30.dp))
                                        .clickable { viewModel.selectedHookType = type }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("type_pill_$type")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = "Selected",
                                                tint = NaturalOnBackground,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = type,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            color = if (isSelected) NaturalOnBackground else NaturalSecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // USP / Additional prompt notes
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Catatan Tambahan - Opsional",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalSecondaryText
                        )
                        OutlinedTextField(
                            value = viewModel.additionalNotes,
                            onValueChange = { viewModel.additionalNotes = it },
                            placeholder = { Text("Misal: Berikan sentuhan humor, batasi hanya 10 kata", color = NaturalPlaceholderText) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = NaturalOnBackground,
                                unfocusedTextColor = NaturalOnBackground,
                                focusedContainerColor = NaturalTextFieldBg,
                                unfocusedContainerColor = NaturalTextFieldBg,
                                focusedIndicatorColor = NaturalPrimary,
                                unfocusedIndicatorColor = NaturalPillBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Generation button
                    Button(
                        onClick = { viewModel.generateHooks() },
                        enabled = !viewModel.isGenerating,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalPrimary,
                            disabledContainerColor = NaturalPrimary.copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("generate_button")
                    ) {
                        if (viewModel.isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Generate Custom Hooks",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buat Kalimat Hook",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Displaying Generation Errors
        if (viewModel.generationError != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = HighlightYellowBg.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CoralRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = CoralRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = viewModel.generationError ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CoralRed
                        )
                    }
                }
            }
        }

        // Generated Results List Title and Filter Row (If result exists!)
        val results = viewModel.generatedHooks
        if (results.isNotEmpty()) {
            val filteredResults = results.filter { hook ->
                val matchesKeyword = resultKeyword.isBlank() ||
                        hook.hookText.contains(resultKeyword, ignoreCase = true) ||
                        hook.videoScenario.contains(resultKeyword, ignoreCase = true) ||
                        hook.ctaText.contains(resultKeyword, ignoreCase = true)

                val wordCount = hook.hookText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                val matchesLength = when (resultLengthFilter) {
                    "Semua" -> true
                    "Pendek (< 15 kata)" -> wordCount < 15
                    "Panjang (>= 15 kata)" -> wordCount >= 15
                    else -> true
                }

                val matchesCta = when (resultCtaFilter) {
                    "Semua" -> true
                    "Checkout/Beli" -> hook.ctaText.contains("checkout", ignoreCase = true) || hook.ctaText.contains("beli", ignoreCase = true) || hook.ctaText.contains("keranjang", ignoreCase = true)
                    "Tanya/Cek" -> hook.ctaText.contains("cek", ignoreCase = true) || hook.ctaText.contains("tonton", ignoreCase = true) || hook.ctaText.contains("link", ignoreCase = true)
                    "Diskon/Urgensi" -> hook.ctaText.contains("diskon", ignoreCase = true) || hook.ctaText.contains("promo", ignoreCase = true) || hook.ctaText.contains("sekarang", ignoreCase = true)
                    else -> true
                }

                matchesKeyword && matchesLength && matchesCta
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hasil Generator AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalOnBackground
                        )
                        Text(
                            text = "${filteredResults.size} dari ${results.size} variasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = NaturalSecondaryText
                        )
                    }

                    // Generator Output Filters Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NaturalContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NaturalPillBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Saring Hasil Generator",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NaturalOnBackground
                            )

                            // Keyword text filter input
                            OutlinedTextField(
                                value = resultKeyword,
                                onValueChange = { resultKeyword = it },
                                placeholder = { Text("Saring lewat kata kunci...", color = NaturalPlaceholderText, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Keyword", tint = NaturalSecondaryText, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = NaturalOnBackground,
                                    unfocusedTextColor = NaturalOnBackground,
                                    focusedContainerColor = NaturalTextFieldBg,
                                    unfocusedContainerColor = NaturalTextFieldBg,
                                    focusedIndicatorColor = NaturalPrimary,
                                    unfocusedIndicatorColor = NaturalPillBorder
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            )

                            // Length filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Panjang:", style = MaterialTheme.typography.labelSmall, color = NaturalSecondaryText, modifier = Modifier.width(55.dp))
                                listOf("Semua", "Pendek (< 15 kata)", "Panjang (>= 15 kata)").forEach { item ->
                                    val active = resultLengthFilter == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) NaturalActivePill else NaturalTextFieldBg)
                                            .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(12.dp))
                                            .clickable { resultLengthFilter = item }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(item.replace("kata", "kt"), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = NaturalOnBackground)
                                    }
                                }
                            }

                            // CTA Filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Aksi CTA:", style = MaterialTheme.typography.labelSmall, color = NaturalSecondaryText, modifier = Modifier.width(55.dp))
                                listOf("Semua", "Checkout/Beli", "Tanya/Cek", "Diskon/Urgensi").forEach { item ->
                                    val active = resultCtaFilter == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) NaturalActivePill else NaturalTextFieldBg)
                                            .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(12.dp))
                                            .clickable { resultCtaFilter = item }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(item, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = NaturalOnBackground)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredResults.isNotEmpty()) {
                items(filteredResults) { hook ->
                    HookOutputCard(
                        hook = hook,
                        isSavedByText = viewModel.isHookSaved(hook.hookText),
                        onToggleSave = { viewModel.toggleSaveHook(hook) },
                        viewModel = viewModel,
                        context = context
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada hasil yang sesuai dengan kriteria filter hasil.", style = MaterialTheme.typography.bodySmall, color = NaturalSecondaryText)
                    }
                }
            }
        } else {
            // Empty State
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = NaturalContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Belum Ada Data",
                            tint = NaturalPlaceholderText,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Masukkan info target audiens dan buat hook pertamamu!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NaturalSecondaryText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HookOutputCard(
    hook: GeneratedHook,
    isSavedByText: Boolean,
    onToggleSave: () -> Unit,
    viewModel: HookViewModel,
    context: Context
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = NaturalContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp))
            .testTag("hook_output_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Hook text with quotation marks
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NaturalTextFieldBg)
                    .border(1.dp, NaturalPillBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "“",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighlightGreenText,
                            lineHeight = 0.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "KALIMAT HOOK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = HighlightGreenText
                        )
                    }
                    Text(
                        text = hook.hookText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        ),
                        color = NaturalOnBackground
                    )
                }
            }

            // Scenario section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Visual",
                        tint = HighlightGreenText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VISUAL & ADEGAN VIDEO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HighlightGreenText
                    )
                }
                Text(
                    text = hook.videoScenario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NaturalSecondaryText
                )
            }

            // CTA section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Call To Action",
                        tint = HighlightYellowText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CALL TO ACTION (PENUTUP)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HighlightYellowText
                    )
                }
                Text(
                    text = hook.ctaText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NaturalSecondaryText
                )
            }

            HorizontalDivider(color = NaturalPillBorder.copy(alpha = 0.5f), thickness = 1.dp)

            // Action row: Copy, Share, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Copy button
                    OutlinedButton(
                        onClick = {
                            viewModel.recordGeneratedCopy(hook)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Generated Hook",
                                "Hook: ${hook.hookText}\nVisual: ${hook.videoScenario}\nCTA: ${hook.ctaText}"
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Selesai disalin! Nilai popularitas meningkat.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NaturalPrimary
                        ),
                        border = borderStroke(0.5.dp, NaturalPillBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 12.sp)
                    }

                    // Share button
                    OutlinedButton(
                        onClick = {
                            viewModel.recordGeneratedShare(hook)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "✨ HOOK VIRAL AFFILIATE ✨\n\nHook: ${hook.hookText}\n🎥 Visual: ${hook.videoScenario}\n📢 CTA: ${hook.ctaText}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Hook"))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NaturalPrimary
                        ),
                        border = borderStroke(0.5.dp, NaturalPillBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 12.sp)
                    }
                }

                // Bookmark Icon Button
                IconButton(
                    onClick = { onToggleSave() },
                    modifier = Modifier.testTag("toggle_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isSavedByText) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Simpan",
                        tint = if (isSavedByText) CoralRed else NaturalMutedText
                    )
                }
            }
        }
    }
}

// Helper boundary strokes
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PopularTab(viewModel: HookViewModel, context: Context) {
    val popularHooks by viewModel.popularHooks.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    val categories = listOf(
        "Semua",
        "Kosmetik & Skincare",
        "Alat Dapur & Rumah Tangga",
        "Fashion & Aksesoris",
        "Elektronik & Gadget",
        "Makanan & Kuliner",
        "Keuangan & Investasi",
        "Kesehatan & Diet"
    )

    val filteredPopular = if (selectedCategoryFilter == "Semua") {
        popularHooks
    } else {
        popularHooks.filter { it.niche == selectedCategoryFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High quality header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NaturalContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔥 Hook Terpopuler Komunitas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NaturalOnBackground
                    )
                    Text(
                        text = "Daftar 5-10 hook affiliate dengan tingkat CTR tertinggi. Statistik disalin & dibagikan bertambah secara berkala saat pengguna memakai template ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NaturalSecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Saring Berdasarkan Kategori Niche",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NaturalSecondaryText
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategoryFilter == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) NaturalActivePill else NaturalTextFieldBg)
                                .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedCategoryFilter = category }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) NaturalOnBackground else NaturalSecondaryText
                            )
                        }
                    }
                }
            }
        }

        // Popular list data
        if (filteredPopular.isNotEmpty()) {
            items(filteredPopular, key = { it.id }) { popular ->
                PopularHookCardItem(
                    popular = popular,
                    isSaved = viewModel.isHookSaved(popular.hookText),
                    onToggleSave = { viewModel.toggleSavePopularHook(popular) },
                    onCopy = {
                        viewModel.recordPopularCopy(popular)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Popular Hook",
                            "Hook: ${popular.hookText}\nVisual: ${popular.videoScenario}\nCTA: ${popular.ctaText}"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Selesai menyalin hook populer! Metrik interaksi bertambah.", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        viewModel.recordPopularShare(popular)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "✨ HOOK VIRAL TERPOPULER AFFILIATE ✨\n\nHook: ${popular.hookText}\n🎥 Visual: ${popular.videoScenario}\n📢 CTA: ${popular.ctaText}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Hook"))
                    }
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada produk populer di kategori ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NaturalSecondaryText
                    )
                }
            }
        }
    }
}

@Composable
fun PopularHookCardItem(
    popular: PopularHook,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = NaturalContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp))
            .testTag("popular_hook_card_${popular.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header stats & niche badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HighlightGreenBg)
                            .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = popular.niche,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = HighlightGreenText
                        )
                    }
                }

                // Stats badge row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Copies",
                            tint = NaturalSecondaryText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${popular.copyCount} disalin",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = NaturalSecondaryText
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Shares",
                            tint = NaturalSecondaryText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${popular.shareCount} dibagikan",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = NaturalSecondaryText
                        )
                    }
                }
            }

            // Hook Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NaturalTextFieldBg)
                    .border(1.dp, NaturalPillBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = popular.hookText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    color = NaturalOnBackground
                )
            }

            // Scenario & CTA details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎥 Visual: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HighlightGreenText
                    )
                    Text(
                        text = popular.videoScenario,
                        style = MaterialTheme.typography.bodySmall,
                        color = NaturalSecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📢 CTA: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HighlightYellowText
                    )
                    Text(
                        text = popular.ctaText,
                        style = MaterialTheme.typography.bodySmall,
                        color = NaturalSecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Reason Why Effective banner!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HighlightGreenBg.copy(alpha = 0.6f))
                    .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Why Effective",
                        tint = HighlightGreenText,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Mengapa Efektif:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = HighlightGreenText
                        )
                        Text(
                            text = popular.whyEffective,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = HighlightGreenText.copy(alpha = 0.9f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = NaturalPillBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

            // Button actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Copy
                    OutlinedButton(
                        onClick = onCopy,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NaturalPrimary
                        ),
                        border = borderStroke(0.5.dp, NaturalPillBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Salin", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    // Share
                    OutlinedButton(
                        onClick = onShare,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NaturalPrimary
                        ),
                        border = borderStroke(0.5.dp, NaturalPillBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Bookmark heart
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Simpan",
                        tint = if (isSaved) CoralRed else NaturalMutedText
                    )
                }
            }
        }
    }
}


@Composable
fun SavedTab(viewModel: HookViewModel, savedHooks: List<SavedHook>, context: Context) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSavedLength by remember { mutableStateOf("Semua") }
    var selectedSavedCtaType by remember { mutableStateOf("Semua") }
    var selectedSavedNiche by remember { mutableStateOf("Semua") }

    val savedNiches = listOf("Semua", "Cosmetik & Skincare", "Alat Dapur & Rumah Tangga", "Fashion & Aksesoris", "Elektronik & Gadget", "Makanan & Kuliner", "Keuangan & Investasi", "Kesehatan & Diet")

    val filteredSavedHooks = savedHooks.filter { hook ->
        // Keyword
        val matchesKeyword = searchQuery.isBlank() || 
                hook.productName.contains(searchQuery, ignoreCase = true) ||
                hook.hookText.contains(searchQuery, ignoreCase = true) ||
                hook.videoScenario.contains(searchQuery, ignoreCase = true) ||
                hook.ctaText.contains(searchQuery, ignoreCase = true)

        // Length word computation
        val words = hook.hookText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val matchesLength = when (selectedSavedLength) {
            "Semua" -> true
            "Pendek (< 10 kata)" -> words < 10
            "Sedang (10-20 kata)" -> words in 10..20
            "Panjang (> 20 kata)" -> words > 20
            else -> true
        }

        // CTA keyword patterns
        val matchesCta = when (selectedSavedCtaType) {
            "Semua" -> true
            "Pembelian" -> hook.ctaText.contains("checkout", ignoreCase = true) || hook.ctaText.contains("beli", ignoreCase = true) || hook.ctaText.contains("keranjang", ignoreCase = true)
            "Info Tanya" -> hook.ctaText.contains("cek", ignoreCase = true) || hook.ctaText.contains("tonton", ignoreCase = true) || hook.ctaText.contains("link", ignoreCase = true)
            "Interaktif" -> hook.ctaText.contains("share", ignoreCase = true) || hook.ctaText.contains("simpan", ignoreCase = true) || hook.ctaText.contains("saved", ignoreCase = true)
            else -> true
        }

        // Niche
        val matchesNiche = if (selectedSavedNiche == "Semua") true else hook.niche == selectedSavedNiche

        matchesKeyword && matchesLength && matchesCta && matchesNiche
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & comprehensive filters card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NaturalContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Koleksi Hook Tersimpan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalOnBackground
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(NaturalActivePill)
                                .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${filteredSavedHooks.size} hook",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NaturalOnBackground
                            )
                        }
                    }

                    // Search OutlinedTextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Telusuri produk atau kalimat...", color = NaturalPlaceholderText) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = NaturalSecondaryText) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Bersihkan", tint = NaturalSecondaryText)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = NaturalOnBackground,
                            unfocusedTextColor = NaturalOnBackground,
                            focusedContainerColor = NaturalTextFieldBg,
                            unfocusedContainerColor = NaturalTextFieldBg,
                            focusedIndicatorColor = NaturalPrimary,
                            unfocusedIndicatorColor = NaturalPillBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("saved_search_input"),
                        singleLine = true
                    )

                    // Filter: Hook Length
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Saring Panjang kalimat:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NaturalMutedText)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Semua", "Pendek (< 10 kata)", "Sedang (10-20 kata)", "Panjang (> 20 kata)").forEach { opt ->
                                val active = selectedSavedLength == opt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) NaturalActivePill else NaturalTextFieldBg)
                                        .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedSavedLength = opt }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(opt.replace(" kata", "kt"), fontSize = 10.sp, color = NaturalOnBackground)
                                }
                            }
                        }
                    }

                    // Filter: CTA Type
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Saring Tipe Call to Action:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NaturalMutedText)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Semua", "Pembelian", "Info Tanya", "Interaktif").forEach { opt ->
                                val active = selectedSavedCtaType == opt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) NaturalActivePill else NaturalTextFieldBg)
                                        .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedSavedCtaType = opt }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(opt, fontSize = 10.sp, color = NaturalOnBackground)
                                }
                            }
                        }
                    }

                    // Filter: Niche Category Category
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Saring Niche Kategori:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NaturalMutedText)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box {
                                var expandedNicheFilter by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NaturalActivePill)
                                        .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(8.dp))
                                        .clickable { expandedNicheFilter = !expandedNicheFilter }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedSavedNiche, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NaturalOnBackground)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih", modifier = Modifier.size(12.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedNicheFilter,
                                    onDismissRequest = { expandedNicheFilter = false },
                                    modifier = Modifier.background(NaturalContainer).border(0.5.dp, NaturalPillBorder, RoundedCornerShape(6.dp))
                                ) {
                                    savedNiches.forEach { n ->
                                        DropdownMenuItem(
                                            text = { Text(n, fontSize = 12.sp, color = NaturalOnBackground) },
                                            onClick = {
                                                selectedSavedNiche = n
                                                expandedNicheFilter = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved List Items
        if (filteredSavedHooks.isNotEmpty()) {
            items(filteredSavedHooks, key = { it.id }) { saved ->
                SavedHookItemCard(
                    saved = saved,
                    onDelete = { viewModel.deleteHook(saved) },
                    viewModel = viewModel,
                    context = context
                )
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Empty Library",
                        tint = NaturalPlaceholderText,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (searchQuery.isEmpty() && selectedSavedLength == "Semua" && selectedSavedCtaType == "Semua" && selectedSavedNiche == "Semua") 
                            "Koleksimu masih kosong.\nSimpan beberapa hook hasil generator AI-mu!" 
                        else 
                            "Pencarian tidak ditemukan. Coba gunakan kata kunci atau penyaring lain.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NaturalSecondaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SavedHookItemCard(
    saved: SavedHook,
    onDelete: () -> Unit,
    viewModel: HookViewModel,
    context: Context
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = NaturalContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp))
            .testTag("saved_hook_card_${saved.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Product & Badge info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = saved.productName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NaturalOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HighlightGreenBg)
                                .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = saved.platform,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = HighlightGreenText
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NaturalActivePill)
                                .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = saved.hookType,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = NaturalOnBackground
                            )
                        }
                    }
                }

                // Delete Icon
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = CoralRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = NaturalPillBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

            // Main hook text displaying
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NaturalTextFieldBg)
                    .border(1.dp, NaturalPillBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = saved.hookText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    color = NaturalOnBackground
                )
            }

            // Expandable/Detailed Visual Scenario & CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎥 Visual: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HighlightGreenText
                    )
                    Text(
                        text = saved.videoScenario,
                        style = MaterialTheme.typography.bodySmall,
                        color = NaturalSecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📢 CTA: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HighlightYellowText
                    )
                    Text(
                        text = saved.ctaText,
                        style = MaterialTheme.typography.bodySmall,
                        color = NaturalSecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Action
                OutlinedButton(
                    onClick = {
                        viewModel.recordSavedCopy(saved)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Saved Hook",
                            "Produk: ${saved.productName}\nPlatform: ${saved.platform}\nHook: ${saved.hookText}\nVisual: ${saved.videoScenario}\nCTA: ${saved.ctaText}"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Selesai menyalin hook simpanan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NaturalPrimary
                    ),
                    border = borderStroke(0.5.dp, NaturalPillBorder),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Salin", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                // Share Action
                OutlinedButton(
                    onClick = {
                        viewModel.recordSavedShare(saved)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "✨ HOOK AFFILIATE SAYA: ${saved.productName} ✨\n\nHook: ${saved.hookText}\n🎥 Visual: ${saved.videoScenario}\n📢 CTA: ${saved.ctaText}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Hook"))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NaturalPrimary
                    ),
                    border = borderStroke(0.5.dp, NaturalPillBorder),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bagikan", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun GoogleLoginScreen(
    viewModel: HookViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showChooser by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.Center
        ) {
            // Large Glow Logo Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NaturalPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Sparkle Icon Large",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to HookGen AI Studio",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = NaturalOnBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Autopilot Hook Video Promosi Terintegrasi Identitas Anda.\nMasuk dengan Google untuk menyinkronkan data & akses model bahasa raya Gemini.",
                style = MaterialTheme.typography.bodyMedium,
                color = NaturalSecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Beautiful custom login card
            Card(
                colors = CardDefaults.cardColors(containerColor = NaturalContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NaturalPillBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Akses Instan & Aman",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NaturalOnBackground
                    )

                    Text(
                        text = "Poin penting sebelum memulai:\n• Seluruh hook disimpan secara otomatis di database lokal\n• Integrasi Gemini API Key aman terlindungi di balik platform token Google\n• Sinkronisasi akun sekali klik tanpa kata sandi rumit",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = NaturalSecondaryText,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // AUTH BUTTON
                    Button(
                        onClick = { showChooser = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2937)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .testTag("google_login_button"),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = NaturalPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Masuk dengan Google",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF374151)
                            )
                        }
                    }
                }
            }
        }

        // Animated Google Account Chooser Bottom Sheet/Dialog Box
        if (showChooser) {
            AlertDialog(
                onDismissRequest = { if (!viewModel.isAuthInProgress) showChooser = false },
                containerColor = NaturalContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .border(1.dp, NaturalPillBorder, RoundedCornerShape(20.dp)),
                confirmButton = {},
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "G",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = NaturalPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pilih akun untuk melanjutkan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NaturalOnBackground
                        )
                        Text(
                            text = "ke aplikasi HookGen AI Studio",
                            style = MaterialTheme.typography.bodySmall,
                            color = NaturalSecondaryText
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        if (viewModel.isAuthInProgress) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = NaturalPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Menghubungkan akun Google Anda...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NaturalOnBackground
                                )
                            }
                        } else {
                            // Account 1: User's actual account from metadata!
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NaturalTextFieldBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.loginWithGoogle(context, "Ari Supriadi", "arisupriadi.AS@gmail.com", onComplete = { showChooser = false })
                                    }
                                    .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(NaturalPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "A",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Ari Supriadi",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NaturalOnBackground
                                        )
                                        Text(
                                            text = "arisupriadi.AS@gmail.com",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NaturalSecondaryText
                                        )
                                    }
                                }
                            }

                            // Account 2: Secondary / Test Account
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NaturalTextFieldBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.loginWithGoogle(context, "Affiliate Creator", "affiliate.creator@gmail.com", onComplete = { showChooser = false })
                                    }
                                    .border(0.5.dp, NaturalPillBorder, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFF0284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "C",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Affiliate Creator",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NaturalOnBackground
                                        )
                                        Text(
                                            text = "affiliate.creator@gmail.com",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NaturalSecondaryText
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { showChooser = false },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.textButtonColors(contentColor = CoralRed)
                            ) {
                                Text("Batal", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
        }
    }
}
