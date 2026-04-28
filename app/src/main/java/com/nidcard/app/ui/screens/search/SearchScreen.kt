package com.nidcard.app.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.NIDViewModel = viewModel()
) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery.value) {
        if (searchQuery.value.isNotBlank()) {
            isSearching = true
            viewModel.setSearchQuery(searchQuery.value)
        } else {
            isSearching = false
        }
    }

    LaunchedEffect(searchResults) {
        isSearching = false
    }

    Column(modifier = Modifier.fillMaxSize().background(GovBg)) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = GovGreen
        ) {
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
                    Text("NID খুঁজুন", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text("নাম, NID নম্বর বা PIN দিয়ে অনুসন্ধান", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // Search bar
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                placeholder = { Text("নাম, NID নম্বর বা PIN দিয়ে খুঁজুন...", fontSize = 13.sp, color = GovTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = GovTextLight) },
                trailingIcon = {
                    if (searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = { searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp), tint = GovTextLight)
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

            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${searchResults.size}টি ফলাফল পাওয়া গেছে",
                    fontSize = 12.sp,
                    color = GovTextLight,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (searchQuery.value.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Search, null, modifier = Modifier.size(40.dp), tint = GovTextMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "নাম, NID নম্বর বা PIN দিয়ে খুঁজুন",
                        color = GovTextLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "যেকোনো তথ্য টাইপ করুন",
                        color = GovTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (isSearching || searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isSearching) {
                        CircularProgressIndicator(color = GovGreen, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("খুঁজছি...", color = GovTextLight, fontSize = 14.sp)
                    } else {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.SearchOff, null, modifier = Modifier.size(40.dp), tint = GovTextMuted)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "কোনো তথ্য পাওয়া যায়নি",
                            color = GovTextLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "অন্য কিছু দিয়ে খুঁজে দেখুন",
                            color = GovTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(searchResults, key = { it.id }) { card ->
                    NIDCardListItem(
                        card = card,
                        onClick = {
                            navController.navigate("view_nid/${card.id}")
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NIDCardListItem(card: NIDCard, onClick: () -> Unit) {
    var photoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(card.id) {
        if (card.photoBase64.isNotBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    photoBitmap = com.nidcard.app.util.Base64Util.decodeToBitmapSampled(card.photoBase64, 200)
                } catch (e: Exception) {
                    // Ignore decode errors
                }
            }
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo or placeholder
            val photo = photoBitmap
            if (photo != null) {
                androidx.compose.foundation.Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Person, null, tint = GovTextMuted, modifier = Modifier.size(26.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    card.nameBn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    card.nameEn,
                    fontSize = 12.sp,
                    color = GovTextLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("NID: ${card.nid}", fontSize = 11.sp, color = GovTextSecondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("PIN: ${card.pin}", fontSize = 11.sp, color = GovTextSecondary)
                }
            }

            // Blood group badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ErrorRed.copy(alpha = 0.1f)
            ) {
                Text(
                    card.blood,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = ErrorRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(Icons.Default.ChevronRight, null, tint = GovTextMuted, modifier = Modifier.size(20.dp))
        }
    }
}
