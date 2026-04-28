package com.nidcard.app.ui.screens.admin

import androidx.compose.animation.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.io.InputStream

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*
import com.nidcard.app.util.Base64Util

import androidx.compose.ui.platform.LocalContext

@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.AdminViewModel = viewModel()
) {
    if (!viewModel.isLoggedIn.collectAsState().value) {
        AdminLoginScreen(navController, viewModel)
    } else {
        AdminPanelScreen(navController, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminLoginScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.AdminViewModel
) {
    val password = remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsState()
    val resetSuccess by viewModel.resetSuccess.collectAsState()
    var showReset by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val resetCode = remember { mutableStateOf("") }
    val newPass = remember { mutableStateOf("") }
    val confirmPass = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GovGreenDark, GovGreen, GovGreenLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = GovGreenPastel
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Security, null, tint = GovGreen, modifier = Modifier.size(36.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Admin Login", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GovText)
                Text("প্রশাসনিক প্যানেলে প্রবেশ করুন", fontSize = 13.sp, color = GovTextLight)

                Spacer(modifier = Modifier.height(24.dp))

                // Reset success message
                AnimatedVisibility(visible = resetSuccess) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessBg
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("পাসওয়ার্ড সফলভাবে পরিবর্তন হয়েছে!", fontSize = 13.sp, color = Color(0xFF155724))
                        }
                    }
                }

                // Login error message
                AnimatedVisibility(visible = loginError != null && !showReset) {
                    loginError?.let {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = ErrorBg
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, tint = GovRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it, fontSize = 13.sp, color = Color(0xFF721C24))
                            }
                        }
                    }
                }

                if (!showReset) {
                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        placeholder = { Text("পাসওয়ার্ড দিন...", fontSize = 14.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Key, null, modifier = Modifier.size(20.dp), tint = GovTextLight) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = GovTextLight
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovGreen, cursorColor = GovGreen, focusedTextColor = GovText, unfocusedTextColor = GovText),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = GovText)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.login(password.value)
                            password.value = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Login, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("লগইন করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = { showReset = true }) {
                        Icon(Icons.Default.Key, null, modifier = Modifier.size(16.dp), tint = GovRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পাসওয়ার্ড ভুলে গেছেন?", color = GovRed, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    // Reset form
                    OutlinedTextField(
                        value = resetCode.value,
                        onValueChange = { resetCode.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("রিসেট কোড দিন...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Security, null, modifier = Modifier.size(20.dp), tint = GovTextLight) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovRed, focusedTextColor = GovText, unfocusedTextColor = GovText)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPass.value,
                        onValueChange = { newPass.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("নতুন পাসওয়ার্ড দিন...", fontSize = 13.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp), tint = GovTextLight) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovRed, focusedTextColor = GovText, unfocusedTextColor = GovText)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPass.value,
                        onValueChange = { confirmPass.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("আবার পাসওয়ার্ড দিন...", fontSize = 13.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp), tint = GovTextLight) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovRed, focusedTextColor = GovText, unfocusedTextColor = GovText)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.resetPassword(resetCode.value, newPass.value, confirmPass.value)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পাসওয়ার্ড রিসেট করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        showReset = false
                        viewModel.clearLoginError()
                        viewModel.clearResetSuccess()
                    }) {
                        Text("লগইনে ফিরে যান", color = GovGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp), tint = GovTextLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("হোম পেজে ফিরে যান", color = GovTextLight, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPanelScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.AdminViewModel
) {
    val cards by viewModel.searchResults.collectAsState()
    val deleteMessage by viewModel.deleteMessage.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteCardDialog by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<NIDCard?>(null) }

    // Timer state
    val timerActive by viewModel.timerActive.collectAsState()
    val timerCountdown by viewModel.timerCountdownText.collectAsState()
    val timerExpired by viewModel.timerExpired.collectAsState()
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerInputValue by remember { mutableStateOf("24") }
    var timerInputType by remember { mutableStateOf("hours") }
    var timerDateMillis by remember { mutableStateOf(0L) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Auto-delete pending confirmation state
    val pendingAutoDelete by viewModel.pendingAutoDelete.collectAsState()

    LaunchedEffect(deleteMessage) {
        if (deleteMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearDeleteMessage()
        }
    }

    val context = LocalContext.current

    // Backup launcher
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val json = viewModel.exportAllCardsAsJson()
                    if (json != null) {
                        outputStream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
            } catch (_: Exception) { }
        }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream: InputStream ->
                    val json = String(inputStream.readBytes(), Charsets.UTF_8)
                    val result = viewModel.importCardsFromJson(json)
                    if (result >= 0) {
                        // Import success — the searchResults flow will auto-update
                    }
                }
            } catch (_: Exception) { }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(GovBg)) {
        // Header
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
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Admin Panel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ডাটা ম্যানেজমেন্ট প্যানেল", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("লগআউট", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "মোট NID",
                    value = cards.size.toString(),
                    icon = Icons.Outlined.Badge,
                    color = GovGreen,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "সিলেক্টেড",
                    value = selectedIds.size.toString(),
                    icon = Icons.Outlined.CheckBox,
                    color = GovGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer card
            if (timerActive) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (timerExpired) ErrorBg else WarningBg
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                if (timerExpired) Icons.Default.Warning else Icons.Default.Timer,
                                null,
                                tint = if (timerExpired) GovRed else WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "অটো-ডিলিট টাইমার",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (timerExpired) GovRed else Color(0xFF92400E)
                                )
                                if (timerCountdown.isNotEmpty()) {
                                    Text(
                                        timerCountdown,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timerExpired) GovRed else WarningAmber
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (timerExpired) GovRed else WarningAmber
                            ) {
                                Text(
                                    if (timerExpired) "মেয়াদ উত্তীর্ণ!" else "সক্রিয়",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AdminActionButton("টাইমার বন্ধ", Icons.Default.Stop, GovRed) {
                                viewModel.clearTimer()
                            }
                        }
                    }
                }
            }

            // Action bar card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it; viewModel.setSearchQuery(it.text) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        placeholder = { Text("নাম, NID, PIN দিয়ে খুঁজুন...", fontSize = 13.sp, color = GovTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = GovTextLight) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovGreen, cursorColor = GovGreen, focusedTextColor = GovText, unfocusedTextColor = GovText),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = GovText)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminActionButton("সব সিলেক্ট", Icons.Default.SelectAll, GovBlue) {
                            viewModel.selectAll(cards)
                        }
                        AdminActionButton("ব্যাকআপ", Icons.Default.Backup, Color(0xFF6D28D9)) {
                            backupLauncher.launch("application/json")
                        }
                        AdminActionButton("ইম্পোর্ট", Icons.Default.Restore, Color(0xFF0369A1)) {
                            importLauncher.launch("application/json")
                        }
                        if (!timerActive) {
                            AdminActionButton("টাইমার", Icons.Default.Timer, GovGold) {
                                showTimerDialog = true
                            }
                        }
                    }

                    if (selectedIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminActionButton("সিলেক্ট ডিলিট (${selectedIds.size})", Icons.Default.Delete, GovRed) {
                                viewModel.deleteSelected()
                            }
                            AdminActionButton("সব ডিলিট", Icons.Default.DeleteForever, Color(0xFF7F1D1D)) {
                                showConfirmDialog = true
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        AdminActionButton("সব ডিলিট", Icons.Default.DeleteForever, GovRed) {
                            showConfirmDialog = true
                        }
                    }
                }
            }

            // Delete message
            AnimatedVisibility(
                visible = deleteMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                deleteMessage?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessBg
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(it, fontSize = 13.sp, color = Color(0xFF155724), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cards list (LazyColumn for performance)
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                if (cards.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    modifier = Modifier.size(72.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Inbox, null, modifier = Modifier.size(36.dp), tint = GovTextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("কোনো NID ডাটা নেই", color = GovTextLight, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("নতুন NID কার্ড তৈরি করুন", color = GovTextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(cards, key = { it.id }) { card ->
                        AdminNIDCardItem(
                            card = card,
                            isSelected = selectedIds.contains(card.id),
                            onSelect = { viewModel.toggleSelection(card.id) },
                            onView = {
                                navController.navigate("view_nid/${card.id}")
                            },
                            onDelete = {
                                cardToDelete = card
                                showDeleteCardDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Confirm dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = ErrorBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Warning, null, tint = GovRed, modifier = Modifier.size(26.dp))
                        }
                    }
                },
                title = { Text("সব ডাটা ডিলিট করবেন?", fontWeight = FontWeight.Bold) },
                text = { Text("আপনি কি নিশ্চিত যে সব NID ডাটা ডিলিট করতে চান?\nএটি পূর্বাবস্থায় ফেরানো যাবে না।", color = GovTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAll()
                            showConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GovRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("হ্যাঁ, ডিলিট করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showConfirmDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("না", color = GovTextSecondary)
                    }
                }
            )
        }

        // Timer setup dialog
        if (showTimerDialog) {
            AlertDialog(
                onDismissRequest = { showTimerDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, tint = GovGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("অটো-ডিলিট টাইমার", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("টাইমার মেয়াদ উত্তীর্ণ হলে সব NID ডাটা অটোমেটিক ডিলিট হবে।", fontSize = 13.sp, color = GovTextLight)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("ধরন নির্বাচন:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GovText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimerTypeChip("ঘণ্টা", "hours", timerInputType) { timerInputType = "hours" }
                            TimerTypeChip("দিন", "days", timerInputType) { timerInputType = "days" }
                            TimerTypeChip("তারিখ", "date", timerInputType) {
                                timerInputType = "date"
                                showDatePicker = true
                            }
                        }

                        if (timerInputType != "date") {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = timerInputValue,
                                onValueChange = { if (it.all { c -> c.isDigit() }) timerInputValue = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (timerInputType == "hours") "ঘণ্টা সংখ্যা" else "দিন সংখ্যা") },
                                leadingIcon = { Icon(Icons.Default.Pin, null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovGreen, focusedTextColor = GovText, unfocusedTextColor = GovText)
                            )
                        } else if (timerDateMillis > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GovGreenSurface
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, null, tint = GovGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                            .format(java.util.Date(timerDateMillis)),
                                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GovGreen
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val numericVal = timerInputValue.toIntOrNull() ?: 1
                            if (timerInputType == "date" && timerDateMillis > 0) {
                                viewModel.setTimerByDate(timerDateMillis)
                            } else {
                                viewModel.setTimer(timerInputType, if (numericVal < 1) 1 else numericVal)
                            }
                            showTimerDialog = false
                            timerInputValue = "24"
                            timerInputType = "hours"
                            timerDateMillis = 0L
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = if (timerInputType == "date") timerDateMillis > 0 else (timerInputValue.toIntOrNull() ?: 0) > 0
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("টাইমার শুরু করুন")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showTimerDialog = false
                            timerInputValue = "24"
                            timerInputType = "hours"
                            timerDateMillis = 0L
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("বাতিল") }
                }
            )
        }

        // Date picker
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis() + 86400000L
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = it }
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                calendar.set(java.util.Calendar.MINUTE, 59)
                                calendar.set(java.util.Calendar.SECOND, 59)
                                timerDateMillis = calendar.timeInMillis
                                timerInputType = "date"
                            }
                            showDatePicker = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("নির্বাচন করুন") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDatePicker = false }, shape = RoundedCornerShape(12.dp)) {
                        Text("বাতিল")
                    }
                }
            ) { DatePicker(state = datePickerState) }
        }

        // Single card delete confirmation dialog
        if (showDeleteCardDialog && cardToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteCardDialog = false; cardToDelete = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = ErrorBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Delete, null, tint = GovRed, modifier = Modifier.size(26.dp))
                        }
                    }
                },
                title = { Text("NID কার্ড ডিলিট করবেন?", fontWeight = FontWeight.Bold) },
                text = { Text("NID: ${cardToDelete?.nid}\n${cardToDelete?.nameBn}\n\nএটি পূর্বাবস্থায় ফেরানো যাবে না।", color = GovTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            cardToDelete?.let { viewModel.deleteCard(it.nid) }
                            showDeleteCardDialog = false
                            cardToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GovRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("হ্যাঁ, ডিলিট করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteCardDialog = false; cardToDelete = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("না", color = GovTextSecondary)
                    }
                }
            )
        }

        // Auto-delete pending confirmation dialog (timer expired while app was closed)
        if (pendingAutoDelete) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissPendingAutoDelete() },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = ErrorBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Timer, null, tint = GovRed, modifier = Modifier.size(26.dp))
                        }
                    }
                },
                title = { Text("অটো-ডিলিট টাইমার মেয়াদ উত্তীর্ণ!", fontWeight = FontWeight.Bold, color = GovRed) },
                text = { Text("আপনার নির্ধারিত টাইমার মেয়াদ উত্তীর্ণ হয়েছে।\nআপনি কি সব NID ডাটা ডিলিট করতে চান?\nএটি পূর্বাবস্থায় ফেরানো যাবে না।", color = GovTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmPendingAutoDelete() },
                        colors = ButtonDefaults.buttonColors(containerColor = GovRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("হ্যাঁ, সব ডিলিট করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.dismissPendingAutoDelete() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("না, টাইমার বন্ধ করুন", color = GovTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun TimerTypeChip(
    label: String,
    type: String,
    currentType: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (currentType == type) GovGreen else Color(0xFFF3F4F6)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (currentType == type) Color.White else GovTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GovText)
                Text(title, fontSize = 11.sp, color = GovTextLight)
            }
        }
    }
}

@Composable
private fun AdminActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdminNIDCardItem(
    card: NIDCard,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    var photoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Load photo with try-catch to prevent crash
    LaunchedEffect(card.id) {
        if (card.photoBase64.isNotBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    photoBitmap = Base64Util.decodeToBitmapSampled(card.photoBase64, 150)
                } catch (e: Exception) {
                    // Ignore decode errors - don't crash
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                colors = CheckboxDefaults.colors(checkedColor = GovGreen)
            )

            // Photo thumbnail
            if (photoBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = photoBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Person, null, tint = GovTextMuted, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(card.nameBn, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(card.nameEn, fontSize = 11.sp, color = GovTextLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text("NID: ${card.nid}", fontSize = 10.sp, color = GovTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = ErrorRed.copy(alpha = 0.1f)) {
                        Text(card.blood, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Actions
            IconButton(onClick = onView) {
                Icon(Icons.Default.Visibility, null, tint = GovBlue, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = GovRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}
