package com.nidcard.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*
import com.nidcard.app.util.Base64Util

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

    val resetCode = remember { mutableStateOf("") }
    val newPass = remember { mutableStateOf("") }
    val confirmPass = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GovGreenDark, GovGreen, GovGreenLight))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Surface(
                    modifier = Modifier.size(75.dp),
                    shape = CircleShape,
                    color = GovGreen
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(34.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Admin Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GovGreen)
                Text("প্রশাসনিক প্যানেলে প্রবেশ করুন", fontSize = 13.sp, color = GovTextLight)

                Spacer(modifier = Modifier.height(28.dp))

                if (resetSuccess) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF166534), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("পাসওয়ার্ড সফলভাবে পরিবর্তন হয়েছে!", fontSize = 13.sp, color = Color(0xFF166534))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                loginError?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF5F5)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = GovRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(it, fontSize = 13.sp, color = GovRed)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!showReset) {
                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("পাসওয়ার্ড দিন...") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovGreen)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.login(password.value)
                            password.value = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Login, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("লগইন করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Divider(modifier = Modifier.weight(1f), color = GovBorder)
                        Text("অথবা", modifier = Modifier.padding(horizontal = 10.dp), fontSize = 12.sp, color = Color(0xFFADB5BD))
                        Divider(modifier = Modifier.weight(1f), color = GovBorder)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                        placeholder = { Text("রিসেট কোড দিন...") },
                        leadingIcon = { Icon(Icons.Default.Security, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovRed)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPass.value,
                        onValueChange = { newPass.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("নতুন পাসওয়ার্ড দিন...") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovRed)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPass.value,
                        onValueChange = { confirmPass.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("আবার পাসওয়ার্ড দিন...") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovRed)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.resetPassword(resetCode.value, newPass.value, confirmPass.value)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovRed),
                        shape = RoundedCornerShape(12.dp)
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
                        Text("লগইনে ফিরে যান", color = GovGreen)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("হোম পেজে ফিরে যান", color = GovGreen, fontWeight = FontWeight.Medium)
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

    // Timer state
    val timerActive by viewModel.timerActive.collectAsState()
    val timerType by viewModel.timerType.collectAsState()
    val timerValue by viewModel.timerValue.collectAsState()
    val timerCountdown by viewModel.timerCountdownText.collectAsState()
    val timerExpired by viewModel.timerExpired.collectAsState()
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerInputValue by remember { mutableStateOf("24") }
    var timerInputType by remember { mutableStateOf("hours") }
    var timerDateMillis by remember { mutableStateOf(0L) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(deleteMessage) {
        if (deleteMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearDeleteMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GovGreenDark, GovGreen, GovGreenLight)))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp, 24.dp).clip(RoundedCornerShape(4.dp)).background(GovGreen), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Color.Red))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("গণপ্রজাতন্ত্রী বাংলাদেশ সরকার", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("People's Republic of Bangladesh", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                }
            }
        }

        // Header
        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp, color = Color.White) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(14.dp), color = GovGreen) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Admin Panel", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GovGreen)
                    Text("জাতীয় পরিচয় পত্র ডাটা ম্যানেজমেন্ট", fontSize = 12.sp, color = GovTextLight)
                }
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = GovRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("লগআউট", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Nav
        Surface(modifier = Modifier.fillMaxWidth(), color = GovGreen) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).clickable { navController.popBackStack() }) {
                    Icon(Icons.Default.Home, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("হোম", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
                Row(modifier = Modifier.background(Color.White.copy(alpha = 0.1f)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Content
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("মোট NID কার্ড", cards.size.toString(), Icons.Default.People, GovGreen)
                StatCard("আজকের তৈরি", cards.count { it.createdAt.startsWith(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }.toString(), Icons.Default.CalendarToday, Color(0xFF2563EB))
                StatCard("সিলেক্টেড", selectedIds.size.toString(), Icons.Default.CheckBox, GovGold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timer card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (timerActive) (if (timerExpired) Color(0xFFFFF5F5) else Color(0xFFFFFBEB)) else Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            if (timerActive) Icons.Default.Timer else Icons.Default.TimerOff,
                            null,
                            tint = if (timerActive) (if (timerExpired) GovRed else Color(0xFFD97706)) else Color(0xFF9CA3AF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "অটো-ডিলিট টাইমার",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (timerActive) (if (timerExpired) GovRed else Color(0xFF92400E)) else GovText,
                            modifier = Modifier.weight(1f)
                        )
                        if (timerActive) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (timerExpired) GovRed else Color(0xFFD97706)
                            ) {
                                Text(
                                    if (timerExpired) "মেয়াদ উত্তীর্ণ!" else "সক্রিয়",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    if (timerActive && timerCountdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            timerCountdown,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timerExpired) GovRed else Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (timerActive) {
                            ActionButton("টাইমার বন্ধ করুন", Icons.Default.Stop, GovRed) {
                                viewModel.clearTimer()
                            }
                        } else {
                            ActionButton("টাইমার সেট করুন", Icons.Default.Timer, GovGreen) {
                                showTimerDialog = true
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it; viewModel.setSearchQuery(it.text) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("নাম, NID, PIN দিয়ে খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton("সব সিলেক্ট", Icons.Default.SelectAll, Color(0xFF2563EB)) {
                            viewModel.selectAll(cards)
                        }
                        if (selectedIds.isNotEmpty()) {
                            ActionButton("সিলেক্ট ডিলিট (${selectedIds.size})", Icons.Default.Delete, GovRed) {
                                viewModel.deleteSelected()
                            }
                        }
                        ActionButton("সব ডিলিট", Icons.Default.DeleteForever, GovRed) {
                            showConfirmDialog = true
                        }
                    }
                }
            }

            // Delete message
            deleteMessage?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD4EDDA)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF28A745), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, fontSize = 13.sp, color = Color(0xFF155724), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cards list
            if (cards.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(48.dp), tint = Color(0xFFDDDDDD))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("কোনো NID ডাটা নেই", color = GovTextLight, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cards, key = { it.id }) { card ->
                        AdminNIDCard(
                            card = card,
                            isSelected = selectedIds.contains(card.id),
                            onSelect = { viewModel.toggleSelection(card.id) },
                            onView = {
                                navController.previousBackStackEntry?.savedStateHandle?.set("admin_card", card)
                                navController.popBackStack()
                            },
                            onDelete = { viewModel.deleteCard(card.nid) }
                        )
                    }
                }
            }
        }

        // Confirm dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("সব ডাটা ডিলিট করবেন?") },
                text = { Text("আপনি কি নিশ্চিত যে সব NID ডাটা ডিলিট করতে চান? এটি পূর্বাবস্থায় ফেরানো যাবে না।") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteAll()
                        showConfirmDialog = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = GovRed)) {
                        Text("হ্যাঁ, ডিলিট করুন")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showConfirmDialog = false }) {
                        Text("না")
                    }
                }
            )
        }

        // Timer setup dialog
        if (showTimerDialog) {
            AlertDialog(
                onDismissRequest = { showTimerDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, tint = GovGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("অটো-ডিলিট টাইমার সেট করুন")
                    }
                },
                text = {
                    Column {
                        Text("টাইমার মেয়াদ উত্তীর্ণ হলে সব NID ডাটা অটোমেটিক ডিলিট হয়ে যাবে।", fontSize = 13.sp, color = GovTextLight)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Timer type selector
                        Text("টাইমার ধরন:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { timerInputType = "hours" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (timerInputType == "hours") GovGreen else Color(0xFFF3F4F6)
                            ) {
                                Text(
                                    "ঘণ্টা",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (timerInputType == "hours") Color.White else GovText,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                onClick = { timerInputType = "days" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (timerInputType == "days") GovGreen else Color(0xFFF3F4F6)
                            ) {
                                Text(
                                    "দিন",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (timerInputType == "days") Color.White else GovText,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                onClick = { showDatePicker = true },
                                shape = RoundedCornerShape(10.dp),
                                color = if (timerInputType == "date") GovGreen else Color(0xFFF3F4F6)
                            ) {
                                Text(
                                    "তারিখ",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (timerInputType == "date") Color.White else GovText,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                )
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
                                shape = RoundedCornerShape(10.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovGreen)
                            )
                        } else if (timerDateMillis > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0FDF4)
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
                        enabled = if (timerInputType == "date") timerDateMillis > 0 else (timerInputValue.toIntOrNull() ?: 0) > 0
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("টাইমার শুরু করুন")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showTimerDialog = false
                        timerInputValue = "24"
                        timerInputType = "hours"
                        timerDateMillis = 0L
                    }) {
                        Text("বাতিল")
                    }
                }
            )
        }

        // Date picker dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis() + 86400000L
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            // Set to end of that day (23:59:59)
                            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = it }
                            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                            calendar.set(java.util.Calendar.MINUTE, 59)
                            calendar.set(java.util.Calendar.SECOND, 59)
                            timerDateMillis = calendar.timeInMillis
                            timerInputType = "date"
                        }
                        showDatePicker = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = GovGreen)) {
                        Text("নির্বাচন করুন")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDatePicker = false }) {
                        Text("বাতিল")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun RowScope.StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = color) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GovText)
                Text(label, fontSize = 11.sp, color = GovTextLight)
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = color
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdminNIDCard(
    card: NIDCard,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Checkbox
            Checkbox(checked = isSelected, onCheckedChange = { onSelect() })

            // Photo
            if (card.photoBase64.isNotBlank()) {
                val bitmap = remember(card.photoBase64) { Base64Util.decodeToBitmap(card.photoBase64) }
                bitmap?.let {
                    androidx.compose.foundation.Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    )
                }
            } else {
                Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFE9ECEF)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFFADB5BD), modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(card.nameBn, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Text(card.nameEn, fontSize = 12.sp, color = GovTextLight, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text("NID: ${card.nid}", fontSize = 11.sp, color = GovTextLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF2F2)) {
                        Text(card.blood, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFFDC3545), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Actions
            IconButton(onClick = onView) {
                Icon(Icons.Default.Visibility, null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = GovRed, modifier = Modifier.size(22.dp))
            }
        }
    }
}
