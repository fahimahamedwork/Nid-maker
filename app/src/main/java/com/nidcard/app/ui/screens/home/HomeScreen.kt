package com.nidcard.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.NIDViewModel = viewModel()
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val todayCount by viewModel.todayCount.collectAsState()
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }
    val searchResult by viewModel.quickSearchResult.collectAsState()
    var showSearchResult by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GovGreenDark, GovGreen, GovGreenLight)))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp, 24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(GovGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("গণপ্রজাতন্ত্রী বাংলাদেশ সরকার", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Government of the People's Republic of Bangladesh", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                }
            }
        }

        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = GovGreen) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("জাতীয় পরিচয় পত্র", color = GovGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("National Identity Card — Election Commission Bangladesh", color = GovTextLight, fontSize = 11.sp)
                }
                Surface(shape = RoundedCornerShape(16.dp), color = GovRed) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সুরক্ষিত", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Navigation bar
        Surface(modifier = Modifier.fillMaxWidth(), color = GovGreen) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("হোম", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                // Search input
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 6.dp).height(38.dp),
                    placeholder = { Text("NID বা PIN...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        Surface(
                            onClick = {
                                if (searchQuery.value.text.isNotBlank()) {
                                    viewModel.quickSearch(searchQuery.value.text.trim())
                                    showSearchResult = true
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = GovGold
                        ) {
                            Text("অনুসন্দান", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GovGreen, unfocusedBorderColor = GovBorder, cursorColor = GovGreen
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                // Admin
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable { navController.navigate("admin") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Search result card
            if (showSearchResult) {
                SearchResultCard(
                    result = searchResult,
                    onDismiss = {
                        showSearchResult = false
                        viewModel.clearQuickSearch()
                        searchQuery.value = TextFieldValue("")
                    },
                    onViewCard = { card ->
                        viewModel.selectCard(card)
                        navController.navigate("view_nid")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // Card header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(GovGreen, GovGreenLight)))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(24.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("NID কার্ড আবেদন", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("আপনার তথ্য প্রদান করুন এবং জাতীয় পরিচয় পত্র তৈরি করুন", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Stats row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("মোট NID", totalCount.toString(), GovGreen)
                            StatItem("আজকের তৈরি", todayCount.toString(), Color(0xFF2563EB))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { navController.navigate("create_nid") },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddCard, null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("নতুন NID কার্ড তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { navController.navigate("search") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, GovGreen)
                        ) {
                            Icon(Icons.Default.Search, null, tint = GovGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("NID খুঁজুন", color = GovGreen, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("আপনার তথ্য সুরক্ষিত — কোনো সার্ভারে সংরক্ষণ করা হয় না", fontSize = 12.sp, color = GovTextLight)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("© 2026 নির্বাচন কমিশন বাংলাদেশ", fontSize = 12.sp, color = GovTextLight)
                Text("Election Commission Bangladesh", fontSize = 11.sp, color = GovTextLight.copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SearchResultCard(
    result: NIDCard?,
    onDismiss: () -> Unit,
    onViewCard: (NIDCard) -> Unit
) {
    if (result != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD4EDDA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF28A745), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("তথ্য পাওয়া গেছে!", color = Color(0xFF155724), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))

                val fields = listOf(
                    "নাম" to result.nameBn, "Name" to result.nameEn,
                    "NID" to result.nid, "PIN" to result.pin,
                    "DOB" to result.dob, "Blood" to result.blood
                )
                fields.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { (label, value) ->
                            Text("$label: $value", fontSize = 13.sp, color = Color(0xFF155724), modifier = Modifier.weight(1f))
                        }
                    }
                }
                Text("পিতা: ${result.father}  |  মাতা: ${result.mother}", fontSize = 13.sp, color = Color(0xFF155724))
                Text("ঠিকানা: ${result.address}", fontSize = 13.sp, color = Color(0xFF155724))

                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { onViewCard(result) }, colors = ButtonDefaults.buttonColors(containerColor = GovGreen), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Badge, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NID কার্ড দেখুন", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                TextButton(onClick = onDismiss) { Text("গুছিয়ে নিন", color = GovTextLight, fontSize = 12.sp) }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8D7DA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cancel, null, tint = Color(0xFF721C24), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("কোনো তথ্য পাওয়া যায়নি", color = Color(0xFF721C24), fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("গুছিয়ে নিন", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = GovTextLight)
    }
}
