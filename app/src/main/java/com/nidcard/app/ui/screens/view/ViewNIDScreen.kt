package com.nidcard.app.ui.screens.view

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*
import com.nidcard.app.util.Base64Util
import com.nidcard.app.util.NIDCardExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNIDScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.NIDViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedCard by viewModel.selectedCard.collectAsState()
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    if (selectedCard == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("কোনো ডাটা পাওয়া যায়নি", color = GovTextLight, fontSize = 16.sp)
        }
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val card = selectedCard!!

    // Generate bitmaps
    var frontBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var backBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(card.id) {
        withContext(Dispatchers.IO) {
            frontBitmap = NIDCardExporter.generateFrontCardBitmap(card, context)
            backBitmap = NIDCardExporter.generateBackCardBitmap(card, context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NID কার্ড তৈরি সম্পন্ন", fontWeight = FontWeight.Bold)
                        Text("আপনার জাতীয় পরিচয় পত্র প্রস্তুত", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    // Download PDF
                    IconButton(onClick = {
                        scope.launch {
                            frontBitmap?.let { front ->
                                backBitmap?.let { back ->
                                    withContext(Dispatchers.IO) {
                                        val file = NIDCardExporter.saveAsPDF(front, back, context, card.nid)
                                        withContext(Dispatchers.Main) {
                                            if (file != null) {
                                                Toast.makeText(context, "PDF সেভ হয়েছে: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
                    }
                    // Download PNG
                    IconButton(onClick = {
                        scope.launch {
                            frontBitmap?.let { front ->
                                backBitmap?.let { back ->
                                    withContext(Dispatchers.IO) {
                                        val file = NIDCardExporter.saveAsImage(front, back, context, card.nid)
                                        withContext(Dispatchers.Main) {
                                            if (file != null) {
                                                Toast.makeText(context, "PNG সেভ হয়েছে: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Image, null, tint = Color.White)
                    }
                    // Share
                    IconButton(onClick = {
                        scope.launch {
                            frontBitmap?.let { front ->
                                backBitmap?.let { back ->
                                    withContext(Dispatchers.IO) {
                                        val file = NIDCardExporter.saveAsImage(front, back, context, card.nid)
                                        if (file != null) {
                                            withContext(Dispatchers.Main) {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "image/png"
                                                    putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "NID কার্ড শেয়ার করুন"))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GovGreen, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Front side
            Text("সামনের দিক (Front)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GovGreen)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                if (frontBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = frontBitmap!!.asImageBitmap(),
                        contentDescription = "NID Card Front",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GovGreen)
                    }
                }
            }

            // Back side
            Text("পিছনের দিক (Back)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GovGreen)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                if (backBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = backBitmap!!.asImageBitmap(),
                        contentDescription = "NID Card Back",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GovGreen)
                    }
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            frontBitmap?.let { front ->
                                backBitmap?.let { back ->
                                    withContext(Dispatchers.IO) {
                                        val file = NIDCardExporter.saveAsPDF(front, back, context, card.nid)
                                        withContext(Dispatchers.Main) {
                                            if (file != null) Toast.makeText(context, "PDF সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF ডাউনলোড", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        scope.launch {
                            frontBitmap?.let { front ->
                                backBitmap?.let { back ->
                                    withContext(Dispatchers.IO) {
                                        val file = NIDCardExporter.saveAsImage(front, back, context, card.nid)
                                        withContext(Dispatchers.Main) {
                                            if (file != null) Toast.makeText(context, "PNG সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PNG ডাউনলোড", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, GovGreen)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = GovGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("নতুন কার্ড", color = GovGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
