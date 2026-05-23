package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.HookDao
import com.example.data.local.PopularHookDao
import com.example.data.model.SavedHook
import com.example.data.model.PopularHook
import com.example.data.api.RetrofitClient
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Content
import com.example.data.api.Part
import com.example.data.api.GenerationConfig
import com.example.data.api.GeneratedHook
import com.example.data.api.InlineData
import com.example.data.api.AnalyzedProductResponse
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HookRepository(
    private val hookDao: HookDao,
    private val popularHookDao: PopularHookDao
) {

    val savedHooks: Flow<List<SavedHook>> = hookDao.getAllSavedHooks()
    val popularHooks: Flow<List<PopularHook>> = popularHookDao.getAllPopularHooksSorted()

    fun isApiKeyConfigured(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return apiKey.isNotEmpty() && 
                !apiKey.contains("MY_GEMINI_API_KEY") && 
                !apiKey.contains("placeholder")
    }

    suspend fun testApiKeyConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Pair(false, "API Key belum dikonfigurasi di Secrets")
        }
        try {
            val tinyRequest = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "Verify connection. Respond with the word OK immediately.")))),
                generationConfig = GenerationConfig(
                    temperature = 0.1f,
                    responseMimeType = "text/plain"
                )
            )
            val response = RetrofitClient.service.generateContent(apiKey, tinyRequest)
            val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!textResult.isNullOrBlank()) {
                return@withContext Pair(true, "API Key Valid & Dapat Digunakan")
            } else {
                return@withContext Pair(false, "API mengembalikan respon kosong")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val cleanErrorMessage = e.localizedMessage ?: e.message ?: "Koneksi salah atau kunci tidak sah"
            return@withContext Pair(false, "Kegagalan Verifikasi: $cleanErrorMessage")
        }
    }

    suspend fun insertHook(hook: SavedHook) {
        hookDao.insertHook(hook)
    }

    suspend fun deleteHook(hook: SavedHook) {
        hookDao.deleteHook(hook)
    }

    suspend fun deleteHookById(id: Int) {
        hookDao.deleteHookById(id)
    }

    suspend fun isHookSaved(hookText: String): Boolean {
        return hookDao.isHookSaved(hookText)
    }

    // Popular Hook Usage tracking and stats update
    suspend fun trackCopy(
        hookText: String,
        videoScenario: String,
        ctaText: String,
        niche: String,
        hookType: String,
        platform: String
    ) = withContext(Dispatchers.IO) {
        val existing = popularHookDao.findPopularHookByText(hookText)
        if (existing != null) {
            popularHookDao.incrementCopyCount(existing.id)
        } else {
            // Add user-generated hook to popular dataset
            popularHookDao.insertPopularHook(
                PopularHook(
                    hookText = hookText,
                    videoScenario = videoScenario,
                    ctaText = ctaText,
                    niche = niche,
                    hookType = hookType,
                    platform = platform,
                    whyEffective = "Hook bertipe $hookType ini dirancang khusus untuk memikat audiens dengan daya tarik yang dipersonalisasi.",
                    copyCount = 1,
                    shareCount = 0,
                    isUserGenerated = true
                )
            )
        }
    }

    suspend fun trackShare(
        hookText: String,
        videoScenario: String,
        ctaText: String,
        niche: String,
        hookType: String,
        platform: String
    ) = withContext(Dispatchers.IO) {
        val existing = popularHookDao.findPopularHookByText(hookText)
        if (existing != null) {
            popularHookDao.incrementShareCount(existing.id)
        } else {
            // Add user-generated hook to popular dataset
            popularHookDao.insertPopularHook(
                PopularHook(
                    hookText = hookText,
                    videoScenario = videoScenario,
                    ctaText = ctaText,
                    niche = niche,
                    hookType = hookType,
                    platform = platform,
                    whyEffective = "Hook buatan pengguna bertipe $hookType dengan pesan kuat untuk memperluas interaksi platform.",
                    copyCount = 0,
                    shareCount = 1,
                    isUserGenerated = true
                )
            )
        }
    }

    suspend fun incrementPopularCopyCount(id: Int) = withContext(Dispatchers.IO) {
        popularHookDao.incrementCopyCount(id)
    }

    suspend fun incrementPopularShareCount(id: Int) = withContext(Dispatchers.IO) {
        popularHookDao.incrementShareCount(id)
    }

    // Checking and seeding 7 high converting Indonesian affiliate hooks
    suspend fun checkAndSeedPopularHooks() = withContext(Dispatchers.IO) {
        val count = popularHookDao.getPopularHooksCount()
        if (count == 0) {
            val seeds = listOf(
                PopularHook(
                    hookText = "Jangan coba-coba beli serum viral ini kalau kulitmu gak siap dibilang glowing kayak artis Korea dalam seminggu!",
                    videoScenario = "Tunjukkan close-up kulit kusam (sebelum), lalu transisi memegang botol serum dan wajah bersih bercahaya (sesudah).",
                    ctaText = "Klik keranjang kuning sekarang mumpung diskon 30% hari ini khusus untuk followers-ku!",
                    niche = "Kosmetik & Skincare",
                    hookType = "Masalah & Solusi",
                    platform = "TikTok",
                    whyEffective = "Menggunakan strategi pesona emosional berkontras tinggi (glowing vs kusam) dan urgensi diskon terbatas.",
                    copyCount = 145,
                    shareCount = 82
                ),
                PopularHook(
                    hookText = "Sumpah nyesel banget baru nemu alat ini sekarang! Ternyata bikin rapi laci lemari serumit ini cuma butuh waktu 2 menit.",
                    videoScenario = "Tunjukkan laci baju / lemari dapur yang sangat berantakan, posisikan organizer, lalu urutkan barang menjadi sangat estetik.",
                    ctaText = "Klik keranjang kuning kiri bawah tipe organizer-nya sebelum kehabisan voucher cashback-nya!",
                    niche = "Alat Dapur & Rumah Tangga",
                    hookType = "FOMO / Urgensi",
                    platform = "TikTok",
                    whyEffective = "Memiliki aspek visual estetik (satisfying) tinggi yang terbukti memperpanjang waktu tonton audiens (high retention rate).",
                    copyCount = 132,
                    shareCount = 74
                ),
                PopularHook(
                    hookText = "Gak perlu beli outfit jutaan rupiah! Dress 80 ribuan ini bisa bikin kamu terlihat karismatik kayak Outfit-Of-The-Day anak Jaksel.",
                    videoScenario = "Kenakan dress mewah tapi dipadukan dengan kacamata hitam, sembari melangkah percaya diri / catwalk lambat didepan kamera.",
                    ctaText = "Pilih warna favoritmu langsung di keranjang kuning sekarang, mumpung masih free ongkir!",
                    niche = "Fashion & Aksesoris",
                    hookType = "Storytelling / Curhat",
                    platform = "Instagram Reels",
                    whyEffective = "Menawarkan nilai ekspektasi status sosial tinggi dengan biaya yang sangat terjangkau (FOMO budget premium).",
                    copyCount = 121,
                    shareCount = 68
                ),
                PopularHook(
                    hookText = "Ternyata trik rahasia ini yang bikin batre HP awet seharian penuh meskipun dipakai main game non-stop!",
                    videoScenario = "Pegang HP yang sedang dicas, perilihatkan menu pengaturan tersembunyi, lalu jalankan aplikasinya secara lincah.",
                    ctaText = "Klik link di bio nomor 25 buat nemuin kabel charger super-fast charging yang dipakai di video ini!",
                    niche = "Elektronik & Gadget",
                    hookType = "Edukasi / Tips",
                    platform = "YouTube Shorts",
                    whyEffective = "Memanfaatkan format 'Trik Spesifik/Tutorial Rahasia' yang mengundang klik tinggi dari audiens pegiat teknologi.",
                    copyCount = 110,
                    shareCount = 59
                ),
                PopularHook(
                    hookText = "Bongkar resep rahasia camilan viral mall yang untungnya bisa ratusan ribu sehari, ternyata bahannya cuma modal kulkas rumah!",
                    videoScenario = "Tunjukkan keju meleleh ditarik panjang dari camilan hangat, uleni adonan sederhana dengan detail macro shot yang bikin laper.",
                    ctaText = "Untuk resep detail dan alat pemotongnya klik link di bioku sekarang ya!",
                    niche = "Makanan & Kuliner",
                    hookType = "Storytelling / Curhat",
                    platform = "TikTok",
                    whyEffective = "Menggabungkan daya tarik lapar mata (food porn) dengan nilai komersial peluang bisnis kuliner modal minim.",
                    copyCount = 98,
                    shareCount = 42
                ),
                PopularHook(
                    hookText = "Umur 20-an tapi tabungan masih jalan di tempat? Tolong stop buang uang buat 3 sifat boros terselubung ini!",
                    videoScenario = "Corat-coret buku catatan kalkulasi, letakkan segepok uang mainan atau tunjukkan grafik rekening tabungan bergerak naik.",
                    ctaText = "Gabung komunitas belajar kelola uang lewat tautan profilku sebelum kelas promonya ditutup besok!",
                    niche = "Keuangan & Investasi",
                    hookType = "Kontroversial / Pikiran Terbuka",
                    platform = "Written Post",
                    whyEffective = "Menyentuh ketakutan finansial generasi muda secara realistis dan menuntun langkah langsung ke solusinya.",
                    copyCount = 87,
                    shareCount = 31
                ),
                PopularHook(
                    hookText = "Tidurmu tersiksa karena pegal-pegal setiap bangun? Hati-hati, bantal tidur lamamu bisa memicu nyeri punggung kronis!",
                    videoScenario = "Peragakan tidur gelisah dan memegangi leher yang kaku, lalu bandingkan dengan bantal ergonomis berbusa memori yang empuk.",
                    ctaText = "Checkout bantal ortopedi bergaransi resmi langsung di tombol keranjang kuning selagi diskon 50%!",
                    niche = "Kesehatan & Diet",
                    hookType = "Masalah & Solusi",
                    platform = "TikTok",
                    whyEffective = "Menggunakan 'Kamera Peringatan Bahaya Sehat', menyasar masalah nyeri nyata yang mengganggu keseharian jutaan orang.",
                    copyCount = 76,
                    shareCount = 28
                )
            )
            popularHookDao.insertPopularHooks(seeds)
        }
    }

    // Main generate function with personalisation
    suspend fun generateHooks(
        productName: String,
        niche: String,
        hookType: String,
        platform: String,
        targetAudience: String,
        sellingPoints: String,
        additionalNotes: String
    ): List<GeneratedHook> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Check if API key is initialized or if it is just the default placeholder key
        val isKeyConfigured = apiKey.isNotEmpty() && 
                !apiKey.contains("MY_GEMINI_API_KEY") && 
                !apiKey.contains("placeholder")

        if (isKeyConfigured) {
            try {
                val systemPrompt = """
                    Kamu adalah pakar copywriter affiliate marketing handal di Indonesia. Tugasmu adalah membuat generator hook video promosi pendek (TikTok, Reels, Shorts) atau postingan media sosial yang mempunyai CTR (Click Through Rate) dan konversi penjualan tinggi.
                    
                    Format output harus SELALU mengembalikan data dalam bentuk LIST JSON murni tanpa dekorasi markdown penjelas di luar kode JSON. JSON skema:
                    [
                      {
                        "hookText": "Kalimat pembuka / hook di 3 detik pertama",
                        "videoScenario": "Deskripsi singkat visual video atau aksi yang harus diperagakan afiliator di layar",
                        "ctaText": "Rekomendasi Call To Action penutup yang relevan"
                      }
                    ]
                    
                    Petunjuk Tambahan:
                    - Buatkan tepat 3 variasi hook yang relevan.
                    - Masukkan detail Target Audiens secara emosional dan sorot keunggulan Poin Penjualan Utama (USP) di dalam kalimat hook.
                    - Gunakan bahasa gaul/santai, persuasif, mengundang rasa ingin tahu, atau bercerita sesuai dengan gaya audiens Indonesia.
                    - Jangan menuliskan basa-basi atau kata pengantar apapun selain JSON murni.
                """.trimIndent()

                val userPrompt = """
                    Buatkan 3 hook affiliate pribadi dengan detail berikut:
                    - Nama Produk / Jasa: $productName
                    - Kategori Niche: $niche
                    - Target Platform: $platform
                    - Sudut Pandang / Tipe Hook: $hookType
                    - Target Audiens Utama (Gunakan ini untuk personalisasi bahasa & kegelisahan mereka): $targetAudience
                    - Poin Penjualan Utama / USP (Sertakan ini dengan kreatif di hook/video): $sellingPoints
                    - Catatan Tambahan: $additionalNotes
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.82f
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (!rawText.isNullOrEmpty()) {
                    val cleanedJson = sanitizeJson(rawText)
                    val type = Types.newParameterizedType(List::class.java, GeneratedHook::class.java)
                    val adapter = RetrofitClient.moshiInstance.adapter<List<GeneratedHook>>(type)
                    val list = adapter.fromJson(cleanedJson)
                    if (!list.isNullOrEmpty()) {
                        return@withContext list
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If network fails or times out, smoothly proceed to offline fallback
            }
        }

        // Return offline fallback content on network failure or if API key is not yet set
        return@withContext generateOfflineTemplate(productName, niche, hookType, platform, targetAudience, sellingPoints, additionalNotes)
    }

    private fun sanitizeJson(json: String): String {
        var clean = json.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json").substringBeforeLast("```")
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```").substringBeforeLast("```")
        }
        return clean.trim()
    }

    // Personalized copywriting offline templates for affiliate marketing with targeting
    private fun generateOfflineTemplate(
        productName: String,
        niche: String,
        hookType: String,
        platform: String,
        targetAudience: String,
        sellingPoints: String,
        additionalNotes: String
    ): List<GeneratedHook> {
        val prod = if (productName.isBlank()) "produk ini" else productName
        val aud = if (targetAudience.isBlank()) "kamu" else targetAudience
        val usp = if (sellingPoints.isBlank()) "praktis digunakan sehari-hari" else sellingPoints
        val add = if (additionalNotes.isBlank()) "" else " dengan keunggulan tambahan $additionalNotes"

        return when (hookType) {
            "FOMO / Urgensi" -> listOf(
                GeneratedHook(
                    hookText = "Heh, buat para $aud! Jangan di-scroll dulu, lu bakal nyesel seumur hidup kalau kehabisan promo $prod karena $usp sisa stoknya menipis!",
                    videoScenario = "Tunjuk kamera dengan ekspresi tegang mendesak, tunjukkan background layar HP berisi promo diskon $prod.",
                    ctaText = "Yuk buruan klik keranjang kuning sebelum kehabisan voucher diskon khusus $aud ini!"
                ),
                GeneratedHook(
                    hookText = "Mending stop scroll dulu para $aud! Sisa stok $prod yang $usp ini tinggal dikit banget gara-gara viral di $platform!",
                    videoScenario = "Perlihatkan ekspresi terkejut sambil memegang produk $prod dekat ke lensa kamera.",
                    ctaText = "Klik buruan link di bio, cek stok dari $prod sekarang sebelum kehabisan!"
                ),
                GeneratedHook(
                    hookText = "Panggilan buat $aud! Cuma buat 10 orang tercepat yang dapet harga miring buat $prod yang super $usp ini!",
                    videoScenario = "Ketuk-ketuk layar HP dengan ritme cepat, tunjukkan gestur angka 10 dengan jari.",
                    ctaText = "Gak usah mikir lama, langsung checkout di keranjang kuning sekarang!"
                )
            )
            "Masalah & Solusi" -> listOf(
                GeneratedHook(
                    hookText = "Capek banget gak sih jadi $aud yang ngadepin masalah mulu? Akhirnya nemu juga penyelamat hidup: $prod yang $usp ini!",
                    videoScenario = "Mulai video dengan menghela nafas panjang bergaya lelah, lalu senyum ceria memamerkan $prod dan fungsinya.",
                    ctaText = "Cobain sendiri kehebatannya! Hubungi kami atau klik keranjang kuning sekarang."
                ),
                GeneratedHook(
                    hookText = "Kalian para $aud yang punya masalah krusial di niche $niche wajib tonton ini! Ini rahasia praktis lewat $prod.",
                    videoScenario = "Tunjukkan kondisi bermasalah (before), lalu perlihatkan kemudahan penggunaan $prod ($usp) sebagai solusi terbaik.",
                    ctaText = "Jangan biarkan masalah mengganggu aktivitasmu. Dapatkan melalui klik link di bio!"
                ),
                GeneratedHook(
                    hookText = "Awalnya iseng doang beli $prod buat ngatasin ribetnya urusan $niche, ternyata buat $aud kayak kita sangat $usp banget!",
                    videoScenario = "Kucek mata seolah keheranan, lalu peragakan detail penggunaan $prod yang super gampang.",
                    ctaText = "Kalian juga harus rasain kemudahannya. Checkout sekarang ya!"
                )
            )
            "Edukasi / Tips" -> listOf(
                GeneratedHook(
                    hookText = "3 Kesalahan fatal $aud saat milih produk $niche yang bikin nyesel! Nomor 3 paling sering diabaikan.",
                    videoScenario = "Tampilkan teks angka 1, 2, 3 di layar. Beri isyarat jempol ke bawah lalu tunjukkan $prod yang $usp.",
                    ctaText = "Biar gak salah pilih lagi, langsung pakai aja yang terbukti $usp. Klik keranjang kuning sekarang!"
                ),
                GeneratedHook(
                    hookText = "Sini-sini merapat para $aud! Cara rahasia memaksimalkan fungsi $prod agar awet dan dapet manfaat $usp maksimal.",
                    videoScenario = "Lambaian tangan 'mendekat', lalu beri tutorial close-up merawat produk dengan jelas.",
                    ctaText = "Jangan lupa saved video ini dan checkout produknya sekarang!"
                ),
                GeneratedHook(
                    hookText = "Kalian para $aud belum dibilang pinter kalau belum tahu tips gampang tentang $niche satu ini!",
                    videoScenario = "Tatap kamera secara cerdas, lalu demonstrasikan trik praktis memakai $prod.",
                    ctaText = "Share ke temanmu yang hobi $niche, lalu amankan $prod kalian sekarang!"
                )
            )
            "Storytelling / Curhat" -> listOf(
                GeneratedHook(
                    hookText = "Sebagai $aud, setengah mati aku dilarang beli $prod sama orang rumah, tapi pas udah coba fitur $usp malah rebutan!",
                    videoScenario = "Pasang ekspresi cemberut memeluk box paket, lalu perlihatkan semua orang tersenyum berebut memakainya.",
                    ctaText = "Biar gak rebutan, beli satu-satu aja ya! Klik link di bawah untuk diskon khusus!"
                ),
                GeneratedHook(
                    hookText = "Jujur, tadinya aku skeptis banget sama $prod ini karena harganya miring. Ternyata pas dipakai dengan kelebihan $usp bikin syok!",
                    videoScenario = "Buka paket sambil berbicara dengan suara curhat santai membisikkan fitur $usp ke mic.",
                    ctaText = "Daripada penasaran, langsung buktikan sendiri dengan klik beli sekarang!"
                ),
                GeneratedHook(
                    hookText = "Gara-gara liat racun $platform buat para $aud, nekat check-out $prod ini. Ternyata keunggulan $usp beneran terbukti!",
                    videoScenario = "Pegang produk di depan dada, mengangguk setuju dengan puas memakai produk.",
                    ctaText = "Sangat worth it! Buruan klik beli sekarang selagi harganya bersahabat."
                )
            )
            "Kontroversial / Pikiran Terbuka" -> listOf(
                GeneratedHook(
                    hookText = "Jangan beli $prod ini kalau kalian para $aud gak siap-siap ketagihan dengan kelebihan $usp-nya!",
                    videoScenario = "Mulai dengan menggelengkan kepala erat memberi tanda silang, lalu tersenyum puas membongkar khasiat produk.",
                    ctaText = "Siap ketagihan dengan $usp? Langsung klik keranjang kuning sekarang!"
                ),
                GeneratedHook(
                    hookText = "Katanya produk $niche buat $aud itu semuanya over-hyped? Sini aku patahin mitos itu pakai pembuktian $prod!",
                    videoScenario = "Ambil $prod asli, demonstrasikan keunggulan $usp-nya secara real-time di depan kamera.",
                    ctaText = "Buktikan secara nyata! Link pembelian resmi ada di bio profil ku."
                ),
                GeneratedHook(
                    hookText = "Gak habis pikir sama brand yang jual $prod seharga ini kepada $aud, padahal keunggulan $usp ini gantiin barang mahal!",
                    videoScenario = "Pijat pelipis dahi pura-pura pusing senang, tunjukkan tabungan hemat berkat memakai produk ini.",
                    ctaText = "Buruan mumpung brand-nya belum sadar kemurahan! Checkout sekarang!"
                )
            )
            else -> listOf(
                GeneratedHook(
                    hookText = "Sumpah racun $platform kali ini berguna banget buat $aud: Memperkenalkan $prod yang $usp!",
                    videoScenario = "Lambaikan tangan ramah, perlihatkan aesthetic close-up shot dari $prod dengan pencahayaan ciamik.",
                    ctaText = "Jangan cuma ditonton, buktikan keunggulan $usp sendiri sekarang. Cek keranjang kuning!"
                ),
                GeneratedHook(
                    hookText = "Rahasia sukses tampil maksimal untuk para $aud, salah satunya wajib punya $prod denga penawaran $usp ini!",
                    videoScenario = "Tampilkan pose percaya diri mengenakan/memakai produk $prod secara elegan.",
                    ctaText = "Mau samaan menjadi $aud modern? Langsung checkout di link di bawah ini ya!"
                ),
                GeneratedHook(
                    hookText = "Kalian para $aud gak perlu pusing mikirin urusan $niche lagi kalau udah megang $prod dengan kelebihan $usp$add!",
                    videoScenario = "Ketuk produk $prod dengan riang ke layar HP, lalu tunjukkan kemudahan pakainya.",
                    ctaText = "Klik sekarang juga dan dapatkan diskon ongkir ke seluruh Indonesia!"
                )
            )
        }
    }

    // New Image analysis capability with Gemini
    suspend fun analyzeProductImage(
        bitmap: android.graphics.Bitmap
    ): AnalyzedProductResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        val isKeyConfigured = apiKey.isNotEmpty() && 
                !apiKey.contains("MY_GEMINI_API_KEY") && 
                !apiKey.contains("placeholder")

        if (isKeyConfigured) {
            try {
                val systemPrompt = """
                    Kamu adalah pakar Copywriter Affiliate Marketing Indonesia dan ahli Computer Vision.
                    Tugasmu adalah menganalisis foto produk yang dikirimkan, lalu mengembalikan data analisis produk beserta tepat 3 variasi hook affiliate yang sangat relevan dan menarik untuk audiens Indonesia.
                    
                    Format output harus SELALU mengembalikan data dalam bentuk objek JSON murni tanpa dekorasi markdown penjelas di luar kode JSON. JSON skema:
                    {
                      "productName": "Nama Produk yang teridentifikasi secara akurat dari gambar",
                      "niche": "Pilih salah satu kategori niche: Cosmetik & Skincare, Alat Dapur & Rumah Tangga, Fashion & Aksesoris, Elektronik & Gadget, Makanan & Kuliner, Keuangan & Investasi, Kesehatan & Diet, Otomotif & Hobby",
                      "usp": "Poin penjualan utama / keunggulan produk terdeteksi singkat (maks 15 kata)",
                      "targetAudience": "Rekomendasi audiens target utama yang paling cocok (misal: 'ibu rumah tangga tangguh', 'mahasiswa sibuk')",
                      "hooks": [
                        {
                          "hookText": "Kalimat pembuka / hook di 3 detik pertama yang terpersonalisasi",
                          "videoScenario": "Deskripsi visual aksi afiliator memegang/mendemokan produk di layar",
                          "ctaText": "Rekomendasi Call To Action penutup yang mengajak checkout"
                        }
                      ]
                    }
                    
                    Jangan menuliskan basa-basi atau kata pengantar apapun selain JSON murni.
                """.trimIndent()

                val userPrompt = "Minta tolong analisis foto produk ini secara detail dan buatkan 3 hook affiliate konversi tinggi."
                val base64Image = bitmapToBase64(bitmap)

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(
                            Part(text = userPrompt),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                        ))
                    ),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.85f
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (!rawText.isNullOrEmpty()) {
                    val cleanedJson = sanitizeJson(rawText)
                    val adapter = RetrofitClient.moshiInstance.adapter(AnalyzedProductResponse::class.java)
                    val analyzed = adapter.fromJson(cleanedJson)
                    if (analyzed != null) {
                        return@withContext analyzed
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Return offline mock response nicely
        return@withContext generateOfflineAnalysis()
    }

    private fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    private fun generateOfflineAnalysis(): AnalyzedProductResponse {
        return AnalyzedProductResponse(
            productName = "Produk Kamera Affiliate",
            niche = "Cosmetik & Skincare",
            usp = "Mudah digunakan dan sangat praktis untuk sehari-hari",
            targetAudience = "Pegiat media sosial / Afiliator pemula",
            hooks = listOf(
                GeneratedHook(
                    hookText = "Bongkar rahasia sukses affiliate pakai produk ini, gak nyangka efeknya secepat ini buat pemula!",
                    videoScenario = "Tunjukkan foto produk dari dekat, hadapkan produk ke kamera dengan latar belakang cerah.",
                    ctaText = "Klik keranjang kuning di bawah ini buat amankan produk eksklusif ini sekarang!"
                ),
                GeneratedHook(
                    hookText = "Kalian yang pengen naik level wajib tahu keunggulan produk viral satu ini, dijamin nyesel baru tahu!",
                    videoScenario = "Putar produk secara berirama, tunjukkan sisi estetik kemasan produk.",
                    ctaText = "Checkout buruan mumpung lagi diskon gila-gilaan khusus hari ini!"
                ),
                GeneratedHook(
                    hookText = "Mending stop scroll dulu! Ini solusi praktis buat kamu yang cape nyari produk idaman di kategori ini.",
                    videoScenario = "Genggam produk erat, gerakkan maju mundur perlahan agar audiens melihat detail fungsinya.",
                    ctaText = "Jangan nunggu viral lagi baru beli, ambil sekarang sebelum kehabisan stok!"
                )
            )
        )
    }
}
