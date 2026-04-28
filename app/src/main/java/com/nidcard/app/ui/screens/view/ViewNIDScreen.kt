package com.nidcard.app.ui.screens.view

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*
import com.nidcard.app.util.NIDCardExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNIDScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.NIDViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedCard by viewModel.selectedCard.collectAsState()
    val scope = rememberCoroutineScope()
    var frontBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var backBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadMessage by remember { mutableStateOf<String?>(null) }

    if (selectedCard == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(GovBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.SearchOff, null, modifier = Modifier.size(64.dp), tint = GovTextMuted)
                Spacer(modifier = Modifier.height(16.dp))
                Text("কোনো ডাটা পাওয়া যায়নি", color = GovTextLight, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = GovGreen), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ফিরে যান")
                }
            }
        }
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val card = selectedCard!!

    // Generate bitmaps asynchronously
    LaunchedEffect(card.id) {
        withContext(Dispatchers.IO) {
            try {
                frontBitmap = NIDCardExporter.generateFrontCardBitmap(card)
                backBitmap = NIDCardExporter.generateBackCardBitmap(card)
            } catch (e: Exception) {
                // Handle gracefully
            }
        }
    }

    fun downloadPDF() {
        scope.launch {
            isDownloading = true
            try {
                val front = frontBitmap ?: withContext(Dispatchers.IO) {
                    NIDCardExporter.generateFrontCardBitmap(card)
                }
                val back = backBitmap ?: withContext(Dispatchers.IO) {
                    NIDCardExporter.generateBackCardBitmap(card)
                }
                withContext(Dispatchers.IO) {
                    val file = NIDCardExporter.saveAsPDF(front, back, context, card.nid)
                    withContext(Dispatchers.Main) {
                        if (file != null) {
                            downloadMessage = "PDF সেভ হয়েছে: ${file.name}"
                        } else {
                            downloadMessage = "PDF সেভ করতে সমস্যা হয়েছে"
                        }
                    }
                }
            } catch (e: Exception) {
                downloadMessage = "ত্রুটি: ${e.message}"
            }
            isDownloading = false
        }
    }

    fun downloadPNG() {
        scope.launch {
            isDownloading = true
            try {
                val front = frontBitmap ?: withContext(Dispatchers.IO) {
                    NIDCardExporter.generateFrontCardBitmap(card)
                }
                val back = backBitmap ?: withContext(Dispatchers.IO) {
                    NIDCardExporter.generateBackCardBitmap(card)
                }
                withContext(Dispatchers.IO) {
                    val file = NIDCardExporter.saveAsImage(front, back, context, card.nid)
                    withContext(Dispatchers.Main) {
                        if (file != null) {
                            downloadMessage = "PNG সেভ হয়েছে: ${file.name}"
                        } else {
                            downloadMessage = "PNG সেভ করতে সমস্যা হয়েছে"
                        }
                    }
                }
            } catch (e: Exception) {
                downloadMessage = "ত্রুটি: ${e.message}"
            }
            isDownloading = false
        }
    }

    fun shareCard() {
        scope.launch {
            try {
                val front = frontBitmap ?: withContext(Dispatchers.IO) {
                    NIDCardExporter.generateFrontCardBitmap(card)
                }
                val back = backBitmap ?: withContext(Dispatchers.IO) {
                    NIDCardExporter.generateBackCardBitmap(card)
                }
                withContext(Dispatchers.IO) {
                    val file = NIDCardExporter.saveAsImage(front, back, context, card.nid)
                    if (file != null) {
                        withContext(Dispatchers.Main) {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "NID কার্ড শেয়ার করুন"))
                            } catch (e: Exception) {
                                downloadMessage = "শেয়ার করতে সমস্যা হয়েছে: ${e.message}"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                downloadMessage = "ত্রুটি: ${e.message}"
            }
        }
    }

    // Download message auto-dismiss
    LaunchedEffect(downloadMessage) {
        if (downloadMessage != null) {
            kotlinx.coroutines.delay(3000)
            downloadMessage = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(GovBg)) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = GovGreen
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NID কার্ড প্রস্তুত!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("আপনার জাতীয় পরিচয় পত্র সফলভাবে তৈরি হয়েছে", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    // Edit button in top bar
                    IconButton(onClick = {
                        navController.navigate(com.nidcard.app.ui.navigation.ScreenRoutes.editNid(card.id))
                    }) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    // Share button in top bar
                    IconButton(onClick = { shareCard() }) {
                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success badge
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                color = GovGreenSurface
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = GovGreen
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("সফলভাবে তৈরি হয়েছে", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GovGreenDark)
                        Text("NID: ${card.nid}  •  PIN: ${card.pin}", fontSize = 12.sp, color = GovTextSecondary)
                    }
                }
            }

            // Download message
            AnimatedVisibility(
                visible = downloadMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                downloadMessage?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (it.contains("সেভ") || it.contains("হয়েছে")) SuccessBg else ErrorBg
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (it.contains("সেভ") || it.contains("হয়েছে")) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                null,
                                tint = if (it.contains("সেভ") || it.contains("হয়েছে")) SuccessGreen else GovRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(it, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                color = if (it.contains("সেভ") || it.contains("হয়েছে")) Color(0xFF155724) else Color(0xFF721C24)
                            )
                        }
                    }
                }
            }

            // Download buttons - PROMINENT
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ডাউনলোড ও শেয়ার", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GovText)
                    Text("আপনার NID কার্ড ডাউনলোড করুন", fontSize = 12.sp, color = GovTextLight)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PDF Download
                        Button(
                            onClick = { downloadPDF() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF ডাউনলোড", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        // PNG Download
                        Button(
                            onClick = { downloadPNG() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GovBlue),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PNG ডাউনলোড", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Share
                    OutlinedButton(
                        onClick = { shareCard() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(2.dp, GovGreen),
                        enabled = !isDownloading
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp), tint = GovGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("শেয়ার করুন", color = GovGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Front side card
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = GovGreenPastel
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Badge, null, modifier = Modifier.size(16.dp), tint = GovGreen)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("সামনের দিক (Front)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GovText)
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                val front = frontBitmap
                if (front != null) {
                    androidx.compose.foundation.Image(
                        bitmap = front.asImageBitmap(),
                        contentDescription = "NID Card Front",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GovGreen, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("কার্ড লোড হচ্ছে...", fontSize = 13.sp, color = GovTextLight)
                        }
                    }
                }
            }

            // Back side card
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = GovBlueLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.CreditCard, null, modifier = Modifier.size(16.dp), tint = GovBlue)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("পিছনের দিক (Back)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GovText)
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                val back = backBitmap
                if (back != null) {
                    androidx.compose.foundation.Image(
                        bitmap = back.asImageBitmap(),
                        contentDescription = "NID Card Back",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GovBlue, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("কার্ড লোড হচ্ছে...", fontSize = 13.sp, color = GovTextLight)
                        }
                    }
                }
            }

            // Bottom actions
            OutlinedButton(
                onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, GovGreen)
            ) {
                Icon(Icons.Default.Home, null, tint = GovGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("হোম পেজে যান", color = GovGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
