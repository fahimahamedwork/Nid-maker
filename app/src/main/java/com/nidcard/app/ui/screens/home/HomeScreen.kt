package com.nidcard.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var showNidField by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(GovBg)) {
        // Government Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(GovGreenDark, GovGreen, GovGreenLight)
                    )
                )
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bangladesh flag icon
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier.size(30.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = GovGreen
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(26.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Government of the People's Republic of Bangladesh",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Title section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 6.dp,
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = GovGreen
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AccountBalance,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "জাতীয় পরিচয় পত্র",
                            color = GovGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "National Identity Card — Election Commission Bangladesh",
                            color = GovTextLight,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick search bar
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    placeholder = { Text("NID বা PIN দিয়ে দ্রুত খুঁজুন...", fontSize = 13.sp, color = GovTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = GovTextLight)
                    },
                    trailingIcon = {
                        if (searchQuery.value.text.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.quickSearch(searchQuery.value.text.trim())
                                showSearchResult = true
                            }) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GovGreen
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("খুঁজুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GovGreen,
                        unfocusedBorderColor = GovBorder,
                        cursorColor = GovGreen
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
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
            AnimatedVisibility(
                visible = showSearchResult,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
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

            // Stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "মোট NID কার্ড",
                    value = totalCount.toString(),
                    icon = Icons.Outlined.Badge,
                    color = GovGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "আজকের তৈরি",
                    value = todayCount.toString(),
                    icon = Icons.Outlined.CalendarToday,
                    color = GovBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main action card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Create NID button - Primary CTA
                    Button(
                        onClick = { navController.navigate("create_nid") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.AddCard, null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "নতুন NID কার্ড তৈরি করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search button
                    OutlinedButton(
                        onClick = { navController.navigate("search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, GovGreen)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = GovGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "NID খুঁজুন",
                            color = GovGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Security note
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GovGreenSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Security,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = GovGreen
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "আপনার তথ্য সম্পূর্ণ নিরাপদ — কোনো সার্ভারে প্রেরণ করা হয় না",
                                fontSize = 12.sp,
                                color = GovGreenDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Admin access button
            OutlinedButton(
                onClick = { navController.navigate("admin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, GovBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GovTextLight)
            ) {
                Icon(
                    Icons.Outlined.AdminPanelSettings,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Admin Panel",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = GovDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "© 2026 নির্বাচন কমিশন বাংলাদেশ",
                    fontSize = 11.sp,
                    color = GovTextMuted
                )
                Text(
                    "Election Commission Bangladesh — Offline Edition",
                    fontSize = 10.sp,
                    color = GovTextMuted
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = GovText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                title,
                fontSize = 11.sp,
                color = GovTextLight,
                textAlign = TextAlign.Center
            )
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
            colors = CardDefaults.cardColors(containerColor = SuccessBg),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = SuccessGreen
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("তথ্য পাওয়া গেছে!", color = Color(0xFF155724), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Info grid
                val infoItems = listOf(
                    "নাম" to result.nameBn,
                    "NID" to result.nid,
                    "PIN" to result.pin,
                    "DOB" to result.dob,
                    "রক্ত" to result.blood
                )
                infoItems.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { (label, value) ->
                            Text(
                                "$label: $value",
                                fontSize = 12.sp,
                                color = Color(0xFF155724),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Text(
                    "পিতা: ${result.father}  •  মাতা: ${result.mother}",
                    fontSize = 12.sp,
                    color = Color(0xFF155724)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onViewCard(result) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কার্ড দেখুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("বন্ধ করুন", color = GovTextLight, fontSize = 13.sp)
                    }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErrorBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SearchOff, null, tint = GovRed, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("কোনো তথ্য পাওয়া যায়নি", color = Color(0xFF721C24), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("বন্ধ", fontSize = 12.sp, color = GovTextLight)
                }
            }
        }
    }
}
