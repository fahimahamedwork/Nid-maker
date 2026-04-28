package com.nidcard.app.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.NIDViewModel = viewModel()
) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(searchQuery.value) {
        viewModel.setSearchQuery(searchQuery.value)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("NID খুঁজুন", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = GovGreen, titleContentColor = Color.White, navigationIconContentColor = Color.White)
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("নাম, NID, PIN দিয়ে খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = { searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GovGreen,
                unfocusedBorderColor = GovBorder
            )
        )

        if (searchQuery.value.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = Color(0xFFDDDDDD))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("নাম, NID নম্বর বা PIN দিয়ে খুঁজুন", color = GovTextLight, fontSize = 15.sp)
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color(0xFFDDDDDD))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("কোনো তথ্য পাওয়া যায়নি", color = GovTextLight, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(searchResults, key = { it.id }) { card ->
                    NIDCardListItem(
                        card = card,
                        onClick = {
                            viewModel.selectCard(card)
                            navController.navigate("view_nid")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NIDCardListItem(card: NIDCard, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Photo or placeholder
            if (card.photoBase64.isNotBlank()) {
                val bitmap = remember(card.photoBase64) {
                    com.nidcard.app.util.Base64Util.decodeToBitmap(card.photoBase64)
                }
                bitmap?.let {
                    androidx.compose.foundation.Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE9ECEF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFFADB5BD), modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(card.nameBn, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Text(card.nameEn, fontSize = 12.sp, color = GovTextLight, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("NID: ${card.nid}", fontSize = 11.sp, color = GovTextLight)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("PIN: ${card.pin}", fontSize = 11.sp, color = GovTextLight)
                }
            }

            // Blood group badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFEF2F2)
            ) {
                Text(
                    card.blood,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = Color(0xFFDC3545),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(Icons.Default.ChevronRight, null, tint = GovTextLight, modifier = Modifier.size(20.dp))
        }
    }
}
