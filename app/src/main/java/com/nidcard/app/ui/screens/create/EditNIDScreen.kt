package com.nidcard.app.ui.screens.create

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.ui.theme.*
import com.nidcard.app.util.Base64Util

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNIDScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.NIDViewModel = viewModel()
) {
    val context = LocalContext.current
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Form fields - pre-populate from existing card
    val nameBn = remember { mutableStateOf("") }
    val nameEn = remember { mutableStateOf("") }
    val nid = remember { mutableStateOf("") }
    val pin = remember { mutableStateOf("") }
    val father = remember { mutableStateOf("") }
    val mother = remember { mutableStateOf("") }
    val birth = remember { mutableStateOf("") }
    val dob = remember { mutableStateOf("") }
    val bloodGroup = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val gender = remember { mutableStateOf("male") }

    // Image states
    val photoBase64 = remember { mutableStateOf("") }
    val photoType = remember { mutableStateOf("image/jpeg") }
    val photoBitmap = remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val signBase64 = remember { mutableStateOf("") }
    val signType = remember { mutableStateOf("image/jpeg") }
    val signBitmap = remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // UI states
    val errors = remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var cardId by remember { mutableStateOf(0L) }
    val issueDate = remember { mutableStateOf("") }
    val createdAt = remember { mutableStateOf("") }

    // Populate fields when card loads
    LaunchedEffect(selectedCard) {
        selectedCard?.let { card ->
            cardId = card.id
            nameBn.value = card.nameBn
            nameEn.value = card.nameEn
            nid.value = card.nid
            pin.value = card.pin
            father.value = card.father
            mother.value = card.mother
            birth.value = card.birth
            dob.value = card.dob
            bloodGroup.value = card.blood
            address.value = card.address
            gender.value = card.gender
            photoBase64.value = card.photoBase64
            photoType.value = card.photoType
            signBase64.value = card.signBase64
            signType.value = card.signType
            issueDate.value = card.issueDate
            createdAt.value = card.createdAt
            errors.value = emptyMap()

            // Decode images for display
            if (card.photoBase64.isNotBlank()) {
                try {
                    photoBitmap.value = Base64Util.decodeToBitmapSampled(card.photoBase64, 300)
                } catch (_: Exception) { }
            }
            if (card.signBase64.isNotBlank()) {
                try {
                    signBitmap.value = Base64Util.decodeToBitmapSampled(card.signBase64, 300)
                } catch (_: Exception) { }
            }
        }
    }

    // Show success dialog
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            showSuccessDialog = true
        }
    }

    // Image pickers
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bytes = stream.readBytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        val (compressed, base64Str) = Base64Util.compressBitmap(bmp)
                        photoBitmap.value = compressed
                        photoBase64.value = base64Str
                        photoType.value = context.contentResolver.getType(it) ?: "image/jpeg"
                    }
                }
            } catch (_: Exception) { }
        }
    }

    val signPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bytes = stream.readBytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        val (compressed, base64Str) = Base64Util.compressBitmap(bmp)
                        signBitmap.value = compressed
                        signBase64.value = base64Str
                        signType.value = context.contentResolver.getType(it) ?: "image/jpeg"
                    }
                }
            } catch (_: Exception) { }
        }
    }

    // Validation and update
    fun validateAndUpdate() {
        val newErrors = mutableMapOf<String, String>()
        if (nameBn.value.isBlank()) newErrors["nameBn"] = "এই ঘরটি পূরণ করা আবশ্যক"
        if (nameEn.value.isBlank()) newErrors["nameEn"] = "This field is required"
        if (nid.value.isBlank() || !nid.value.matches(Regex("^[0-9]{10,17}$")))
            newErrors["nid"] = "সঠিক NID নম্বর দিন (10-17 ডিজিট)"
        if (pin.value.isBlank() || !pin.value.matches(Regex("^[0-9]{4,10}$")))
            newErrors["pin"] = "সঠিক PIN নম্বর দিন"
        if (father.value.isBlank()) newErrors["father"] = "এই ঘরটি পূরণ করা আবশ্যক"
        if (mother.value.isBlank()) newErrors["mother"] = "এই ঘরটি পূরণ করা আবশ্যক"
        if (birth.value.isBlank()) newErrors["birth"] = "এই ঘরটি পূরণ করা আবশ্যক"
        if (dob.value.isBlank()) newErrors["dob"] = "এই ঘরটি পূরণ করা আবশ্যক"
        if (bloodGroup.value.isBlank() || bloodGroup.value !in listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"))
            newErrors["blood"] = "সঠিক রক্তের গ্রুপ নির্বাচন করুন"
        if (address.value.isBlank()) newErrors["address"] = "এই ঘরটি পূরণ করা আবশ্যক"
        if (photoBase64.value.isBlank()) newErrors["photo"] = "ছবি আপলোড করুন"
        if (signBase64.value.isBlank()) newErrors["signature"] = "স্বাক্ষর আপলোড করুন"

        if (newErrors.isNotEmpty()) {
            errors.value = newErrors
            return
        }

        val card = NIDCard(
            id = cardId,
            nameBn = nameBn.value,
            nameEn = nameEn.value,
            nid = nid.value,
            pin = pin.value,
            father = father.value,
            mother = mother.value,
            birth = birth.value,
            dob = dob.value,
            blood = bloodGroup.value,
            address = address.value,
            gender = gender.value,
            issueDate = issueDate.value,
            createdAt = createdAt.value,
            photoBase64 = photoBase64.value,
            photoType = photoType.value,
            signBase64 = signBase64.value,
            signType = signType.value
        )
        viewModel.updateCard(card)
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearSaveStatus()
                navController.navigate(com.nidcard.app.ui.navigation.ScreenRoutes.viewNid(cardId)) {
                    popUpTo(com.nidcard.app.ui.navigation.ScreenRoutes.HOME) { inclusive = true }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = GovGreenPastel
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, null, tint = GovGreen, modifier = Modifier.size(42.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "NID কার্ড আপডেট হয়েছে!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GovText,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearSaveStatus()
                        navController.navigate(com.nidcard.app.ui.navigation.ScreenRoutes.viewNid(cardId)) {
                            popUpTo(com.nidcard.app.ui.navigation.ScreenRoutes.HOME) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("কার্ড দেখুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearSaveStatus()
                        navController.navigate(com.nidcard.app.ui.navigation.ScreenRoutes.HOME) {
                            popUpTo(com.nidcard.app.ui.navigation.ScreenRoutes.HOME) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, GovBorder)
                ) {
                    Text("হোম পেজে যান", color = GovTextSecondary)
                }
            }
        )
    }

    if (selectedCard == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(GovBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GovGreen, strokeWidth = 3.dp)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(GovBg)) {
        // Custom top bar
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
                        Text("NID কার্ড সম্পাদনা", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("তথ্য পরিবর্তন করুন", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // --- Personal Info ---
                    SectionHeader(
                        icon = Icons.Outlined.Person,
                        title = "ব্যক্তিগত তথ্য",
                        subtitle = "Personal Information"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernOutlinedField(
                            label = "নাম (বাংলা) *",
                            value = nameBn,
                            placeholder = "আপনার নাম বাংলায়",
                            error = errors.value["nameBn"],
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.Badge
                        )
                        ModernOutlinedField(
                            label = "Name (English) *",
                            value = nameEn,
                            placeholder = "Your name in English",
                            error = errors.value["nameEn"],
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.Badge
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernOutlinedField(
                            label = "NID নম্বর *",
                            value = nid,
                            placeholder = "8252184567",
                            keyboardType = KeyboardType.Number,
                            maxLength = 17,
                            error = errors.value["nid"],
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.CreditCard
                        )
                        ModernOutlinedField(
                            label = "PIN নম্বর *",
                            value = pin,
                            placeholder = "PIN",
                            keyboardType = KeyboardType.Number,
                            maxLength = 10,
                            error = errors.value["pin"],
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.Pin
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    ModernOutlinedField(
                        label = "জন্ম তারিখ *",
                        value = dob,
                        placeholder = "05 Nov 2005",
                        error = errors.value["dob"],
                        leadingIcon = Icons.Outlined.CalendarMonth
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            ModernLabel("রক্তের গ্রুপ *", Icons.Outlined.Bloodtype)
                            Spacer(modifier = Modifier.height(4.dp))
                            ModernDropdownField(
                                options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
                                selected = bloodGroup,
                                placeholder = "নির্বাচন করুন"
                            )
                            errors.value["blood"]?.let {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(it, color = GovRed, fontSize = 11.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            ModernLabel("লিঙ্গ", Icons.Outlined.Wc)
                            Spacer(modifier = Modifier.height(4.dp))
                            ModernDropdownField(
                                options = listOf("male", "female", "other"),
                                selected = gender,
                                placeholder = "নির্বাচন করুন",
                                labels = mapOf("male" to "পুরুষ", "female" to "মহিলা", "other" to "অন্যান্য")
                            )
                        }
                        ModernOutlinedField(
                            label = "জন্মস্থান *",
                            value = birth,
                            placeholder = "ঢাকা",
                            error = errors.value["birth"],
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.LocationOn
                        )
                    }

                    // --- Parent Info ---
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(
                        icon = Icons.Outlined.People,
                        title = "পিতামাতার তথ্য",
                        subtitle = "Parent Information"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernOutlinedField(
                            label = "পিতার নাম *",
                            value = father,
                            placeholder = "পিতার নাম",
                            error = errors.value["father"],
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.Person
                        )
                        ModernOutlinedField(
                            label = "মাতার নাম *",
                            value = mother,
                            placeholder = "মাতার নাম",
                            error = errors.value["mother"],
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.Person
                        )
                    }

                    // --- Address ---
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(
                        icon = Icons.Outlined.LocationOn,
                        title = "ঠিকানা",
                        subtitle = "Address"
                    )
                    ModernOutlinedField(
                        label = "সম্পূর্ণ ঠিকানা *",
                        value = address,
                        placeholder = "গ্রাম/মহল্লা, উপজেলা/থানা, জেলা",
                        error = errors.value["address"],
                        singleLine = false,
                        minLines = 3,
                        leadingIcon = Icons.Outlined.Home
                    )

                    // --- Uploads ---
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(
                        icon = Icons.Outlined.CloudUpload,
                        title = "ছবি ও স্বাক্ষর",
                        subtitle = "Photo & Signature"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            ModernLabel("ছবি (পাসপোর্ট সাইজ) *", Icons.Outlined.CameraAlt)
                            Spacer(modifier = Modifier.height(4.dp))
                            ModernUploadZone(
                                icon = Icons.Outlined.CameraAlt,
                                label = "ছবি পরিবর্তন",
                                subtitle = "JPG/PNG, সর্বোচ্চ 5MB",
                                onClick = { photoPicker.launch("image/*") },
                                bitmap = photoBitmap.value,
                                error = errors.value["photo"]
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            ModernLabel("স্বাক্ষর *", Icons.Outlined.Draw)
                            Spacer(modifier = Modifier.height(4.dp))
                            ModernUploadZone(
                                icon = Icons.Outlined.Draw,
                                label = "স্বাক্ষর পরিবর্তন",
                                subtitle = "JPG/PNG, সর্বোচ্চ 2MB",
                                onClick = { signPicker.launch("image/*") },
                                bitmap = signBitmap.value,
                                error = errors.value["signature"]
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    errorMessage?.let {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ErrorBg
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, tint = GovRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it, color = Color(0xFF721C24), fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Update button
                    Button(
                        onClick = { validateAndUpdate() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovBlue),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("আপডেট হচ্ছে...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("আপডেট করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
