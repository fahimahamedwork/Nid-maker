package com.nidcard.app.ui.screens.create

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
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
fun CreateNIDScreen(
    navController: NavController,
    viewModel: com.nidcard.app.viewmodel.NIDViewModel = viewModel()
) {
    val context = LocalContext.current
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val lastInsertedId by viewModel.lastInsertedId.collectAsState()

    // Form fields
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
    var isGenerating by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val createdNid = remember { mutableStateOf("") }

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
            } catch (e: Exception) {
                // Handle error silently
            }
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
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    // Navigate after save to ViewNIDScreen
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            showSuccessDialog = true
        }
    }

    // Auto generate function
    fun autoGenerate() {
        isGenerating = true
        nameBn.value = "মোহাম্মদ ফাহাদ আহমেদ"
        nameEn.value = "Md. Fahad Ahamed"
        nid.value = "8252184567890"
        pin.value = "1234567890"
        father.value = "মোঃ আবদুল হাই"
        mother.value = "মোসাঃ রাবেয়া খাতুন"
        birth.value = "ঢাকা"
        dob.value = "05 Nov 2005"
        bloodGroup.value = "B+"
        address.value = "গ্রাম: কালিকাপুর, উপজেলা: নবাবগঞ্জ, জেলা: ঢাকা"
        gender.value = "male"
        errors.value = emptyMap()

        // Auto-generate placeholder photo and signature
        try {
            val photoBmp = Base64Util.generatePlaceholderPhoto(300, 400)
            photoBitmap.value = photoBmp
            val (compressedPhoto, photoBase64Str) = Base64Util.compressBitmap(photoBmp)
            photoBase64.value = photoBase64Str
            photoType.value = "image/jpeg"

            val signBmp = Base64Util.generatePlaceholderSignature(300, 100)
            signBitmap.value = signBmp
            val (compressedSign, signBase64Str) = Base64Util.compressBitmap(signBmp)
            signBase64.value = signBase64Str
            signType.value = "image/jpeg"
        } catch (e: Exception) {
            // Silent fail - user can still manually upload
        }
        isGenerating = false
    }

    fun validateAndSave() {
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

        createdNid.value = nid.value
        val card = NIDCard(
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
            photoBase64 = photoBase64.value,
            photoType = photoType.value,
            signBase64 = signBase64.value,
            signType = signType.value
        )
        viewModel.saveCard(card)
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearSaveStatus()
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
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
                        "NID কার্ড সফলভাবে তৈরি হয়েছে!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GovText,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "NID: ${createdNid.value}",
                        fontSize = 14.sp,
                        color = GovTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "আপনি এখন কার্ডটি ডাউনলোড করতে পারবেন",
                        fontSize = 13.sp,
                        color = GovTextLight,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        val insertedId = lastInsertedId
                        viewModel.clearSaveStatus()
                        // Navigate to view the created card using its database ID
                        if (insertedId > 0) {
                            navController.navigate("view_nid/$insertedId") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("কার্ড দেখুন ও ডাউনলোড করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearSaveStatus()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
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
                        Text("NID কার্ড তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("সকল তথ্য পূরণ করুন", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
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
            // Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProgressStep(1, "তথ্য", true)
                ProgressDivider(true)
                ProgressStep(2, "ঠিকানা", true)
                ProgressDivider(true)
                ProgressStep(3, "আপলোড", true)
            }

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

                    // Name fields
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

                    // NID, PIN, DOB
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

                    // Blood group, Gender, Birth place
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
                        // Photo upload
                        Column(modifier = Modifier.weight(1f)) {
                            ModernLabel("ছবি (পাসপোর্ট সাইজ) *", Icons.Outlined.CameraAlt)
                            Spacer(modifier = Modifier.height(4.dp))
                            ModernUploadZone(
                                icon = Icons.Outlined.CameraAlt,
                                label = "ছবি নির্বাচন",
                                subtitle = "JPG/PNG, সর্বোচ্চ 5MB",
                                onClick = { photoPicker.launch("image/*") },
                                bitmap = photoBitmap.value,
                                error = errors.value["photo"]
                            )
                        }
                        // Signature upload
                        Column(modifier = Modifier.weight(1f)) {
                            ModernLabel("স্বাক্ষর *", Icons.Outlined.Draw)
                            Spacer(modifier = Modifier.height(4.dp))
                            ModernUploadZone(
                                icon = Icons.Outlined.Draw,
                                label = "স্বাক্ষর নির্বাচন",
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

                    // Auto generate button
                    OutlinedButton(
                        onClick = { autoGenerate() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(2.dp, GovPurple),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GovPurple)
                    ) {
                        Icon(
                            if (isGenerating) Icons.Default.Autorenew else Icons.Default.AutoFixHigh,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto Generate — সব তথ্য অটো পূরণ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submit button
                    Button(
                        onClick = { validateAndSave() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Shield, null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NID কার্ড তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- Modern Composable Components ---

@Composable
private fun ProgressStep(step: Int, label: String, completed: Boolean) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = if (completed) GovGreen else GovBorder
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (completed) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text(step.toString(), fontWeight = FontWeight.Bold, color = GovTextLight, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun RowScope.ProgressDivider(completed: Boolean) {
    Divider(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 12.dp),
        color = if (completed) GovGreen else GovBorder,
        thickness = 2.dp
    )
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = GovGreenPastel
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = GovGreen)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GovText)
            Text(subtitle, fontSize = 11.sp, color = GovTextLight)
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun ModernLabel(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = GovTextLight)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GovTextSecondary)
    }
}

@Composable
private fun ModernOutlinedField(
    label: String,
    value: MutableState<String>,
    placeholder: String = "",
    error: String? = null,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GovTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value.value,
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) value.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = GovTextMuted) },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, null, modifier = Modifier.size(18.dp), tint = GovTextLight) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GovGreen,
                unfocusedBorderColor = if (error != null) GovRed else GovBorder,
                cursorColor = GovGreen,
                errorBorderColor = GovRed,
                errorCursorColor = GovRed
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
        if (error != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Error, null, tint = GovRed, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(error, color = GovRed, fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernDropdownField(
    options: List<String>,
    selected: MutableState<String>,
    placeholder: String,
    labels: Map<String, String> = emptyMap()
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = labels[selected.value] ?: selected.value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            placeholder = { Text(placeholder, fontSize = 13.sp, color = GovTextMuted) },
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            shape = RoundedCornerShape(12.dp),
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GovGreen,
                unfocusedBorderColor = GovBorder
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(labels[option] ?: option, fontSize = 14.sp) },
                    onClick = {
                        selected.value = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernUploadZone(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    bitmap: android.graphics.Bitmap?,
    error: String?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (error != null) ErrorBg else Color(0xFFF8FAFC),
        border = if (error != null) BorderStroke(2.dp, GovRed) else BorderStroke(1.5.dp, GovBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, GovGreen, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("পরিবর্তন করতে ক্লিক করুন", fontSize = 10.sp, color = GovGreen, fontWeight = FontWeight.Medium)
            } else {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = GovGreenPastel
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, modifier = Modifier.size(24.dp), tint = GovGreen)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = GovText)
                Text(subtitle, fontSize = 11.sp, color = GovTextLight)
            }
        }
    }
    if (error != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.Error, null, tint = GovRed, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(error, color = GovRed, fontSize = 11.sp)
        }
    }
}
