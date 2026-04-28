package com.nidcard.app.ui.screens.create

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

    // Validation errors
    val errors = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Image pickers
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val bytes = stream.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                photoBitmap.value = bmp
                photoBase64.value = Base64Util.encodeBytesToString(bytes)
                photoType.value = context.contentResolver.getType(it) ?: "image/jpeg"
            }
        }
    }

    val signPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val bytes = stream.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                signBitmap.value = bmp
                signBase64.value = Base64Util.encodeBytesToString(bytes)
                signType.value = context.contentResolver.getType(it) ?: "image/jpeg"
            }
        }
    }

    // Navigate after save
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.clearSaveStatus()
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Auto generate function
    fun autoGenerate() {
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
        errors.value = emptyMap()
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

    Column(modifier = Modifier.fillMaxSize().background(GovBg)) {
        // Top bar
        TopAppBar(
            title = { Text("NID কার্ড তৈরি", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = GovGreen, titleContentColor = Color.White, navigationIconContentColor = Color.White)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // --- Personal Info ---
                    SectionTitle(Icons.Default.Person, "ব্যক্তিগত তথ্য")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedField(
                            label = "নাম (বাংলা) *", value = nameBn, placeholder = "আপনার নাম বাংলায়",
                            error = errors.value["nameBn"], modifier = Modifier.weight(1f)
                        )
                        OutlinedField(
                            label = "Name (English) *", value = nameEn, placeholder = "Your name in English",
                            error = errors.value["nameEn"], modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedField(
                            label = "NID নম্বর *", value = nid, placeholder = "8252184567",
                            keyboardType = KeyboardType.Number, maxLength = 17,
                            error = errors.value["nid"], modifier = Modifier.weight(1f)
                        )
                        OutlinedField(
                            label = "PIN নম্বর *", value = pin, placeholder = "PIN",
                            keyboardType = KeyboardType.Number, maxLength = 10,
                            error = errors.value["pin"], modifier = Modifier.weight(1f)
                        )
                        OutlinedField(
                            label = "জন্ম তারিখ *", value = dob, placeholder = "05 Nov 2005",
                            error = errors.value["dob"], modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Blood group dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text("রক্তের গ্রুপ *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                            Spacer(modifier = Modifier.height(4.dp))
                            DropdownField(
                                options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
                                selected = bloodGroup,
                                placeholder = "নির্বাচন করুন"
                            )
                            errors.value["blood"]?.let {
                                Text(it, color = GovRed, fontSize = 12.sp)
                            }
                        }
                        // Gender dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text("লিঙ্গ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                            Spacer(modifier = Modifier.height(4.dp))
                            DropdownField(
                                options = listOf("male", "female", "other"),
                                selected = gender,
                                placeholder = "নির্বাচন করুন",
                                labels = mapOf("male" to "পুরুষ", "female" to "মহিলা", "other" to "অন্যান্য")
                            )
                        }
                        OutlinedField(
                            label = "জন্মস্থান *", value = birth, placeholder = "ঢাকা",
                            error = errors.value["birth"], modifier = Modifier.weight(1f)
                        )
                    }

                    // --- Parent Info ---
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(Icons.Default.People, "পিতামাতার তথ্য")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedField(
                            label = "পিতার নাম *", value = father, placeholder = "পিতার নাম",
                            error = errors.value["father"], modifier = Modifier.weight(1f)
                        )
                        OutlinedField(
                            label = "মাতার নাম *", value = mother, placeholder = "মাতার নাম",
                            error = errors.value["mother"], modifier = Modifier.weight(1f)
                        )
                    }

                    // --- Address ---
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(Icons.Default.LocationOn, "ঠিকানা")
                    OutlinedField(
                        label = "সম্পূর্ণ ঠিকানা *", value = address,
                        placeholder = "গ্রাম/মহল্লা, উপজেলা/থানা, জেলা",
                        error = errors.value["address"], singleLine = false, minLines = 2
                    )

                    // --- Uploads ---
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(Icons.Default.CloudUpload, "আপলোড")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Photo upload
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ছবি (পাসপোর্ট সাইজ) *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                            Spacer(modifier = Modifier.height(4.dp))
                            UploadZone(
                                icon = Icons.Default.CameraAlt,
                                label = "ছবি নির্বাচন করুন",
                                subtitle = "JPG/PNG, সর্বোচ্চ 5MB",
                                onClick = { photoPicker.launch("image/*") },
                                bitmap = photoBitmap.value,
                                error = errors.value["photo"]
                            )
                        }
                        // Signature upload
                        Column(modifier = Modifier.weight(1f)) {
                            Text("স্বাক্ষর *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                            Spacer(modifier = Modifier.height(4.dp))
                            UploadZone(
                                icon = Icons.Default.Draw,
                                label = "স্বাক্ষর নির্বাচন করুন",
                                subtitle = "JPG/PNG, সর্বোচ্চ 2MB",
                                onClick = { signPicker.launch("image/*") },
                                bitmap = signBitmap.value,
                                error = errors.value["signature"]
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Auto generate button
                    Button(
                        onClick = { autoGenerate() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6F42C1)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto Generate — সব তথ্য অটো পূরণ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submit button
                    Button(
                        onClick = { validateAndSave() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Shield, null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NID কার্ড তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Error message
                    errorMessage?.let {
                        Text(it, color = GovRed, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = GovGreen)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GovGreen)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Divider(color = GovGreen, thickness = 2.dp)
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun OutlinedField(
    label: String,
    value: MutableState<String>,
    placeholder: String = "",
    error: String? = null,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value.value,
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) value.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF999999)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GovGreen,
                unfocusedBorderColor = if (error != null) GovRed else GovBorder,
                cursorColor = GovGreen
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
        error?.let { Text(it, color = GovRed, fontSize = 12.sp) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DropdownField(
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
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF999999)) },
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            shape = RoundedCornerShape(8.dp),
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GovGreen, unfocusedBorderColor = GovBorder),
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
private fun UploadZone(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    bitmap: android.graphics.Bitmap?,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, if (error != null) GovRed else GovBorder, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFAFBFC))
            .clickable { onClick() }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).border(2.dp, GovGreen, RoundedCornerShape(8.dp))
            )
        } else {
            Icon(icon, null, modifier = Modifier.size(32.dp), tint = GovGreen)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = GovText)
            Text(subtitle, fontSize = 12.sp, color = GovTextLight)
        }
    }
    error?.let { Text(it, color = GovRed, fontSize = 12.sp) }
}
