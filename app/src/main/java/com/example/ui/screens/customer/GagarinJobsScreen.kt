package com.example.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.JobVacancyEntity
import com.example.data.model.UserRole
import com.example.data.util.AdOwnershipHelper
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.EcosystemViewModel
import kotlinx.coroutines.launch

val jobCategories = listOf(
    "Barchasi",
    "Savdo & Do'kon",
    "Haydovchilik",
    "Oshxona & Kafe",
    "Qurilish & Usta",
    "Ishlab chiqarish",
    "Moliya & IT",
    "Boshqa"
)

val jobTypes = listOf(
    "To‘liq stavka",
    "Yarim stavka",
    "Erkin grafik",
    "Bir martalik ish"
)

private fun normalizePhoneNumber(phone: String?): String {
    return phone?.replace("[^0-9]".toRegex(), "") ?: ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GagarinJobsScreen(
    ecosystemViewModel: EcosystemViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by authViewModel.session.collectAsState()
    val selectedCategory by ecosystemViewModel.selectedJobCategory.collectAsState()
    val jobsList by ecosystemViewModel.jobs.collectAsState()

    var showPostJobDialog by remember { mutableStateOf(false) }
    var jobToEdit by remember { mutableStateOf<JobVacancyEntity?>(null) }
    var jobToDelete by remember { mutableStateOf<JobVacancyEntity?>(null) }
    var showAuthPromptDialog by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (showAuthPromptDialog) {
        AlertDialog(
            onDismissRequest = { showAuthPromptDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Tizimga kirish talab qilinadi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SecondaryNavy
                )
            },
            text = {
                Text(
                    text = "Ish va vakansiyalar bo‘limiga e'lon joylashtirish uchun avval o‘z akkauntingizga kiring yoki ro‘yxatdan o‘ting.",
                    fontSize = 13.sp,
                    color = SecondaryNavy
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAuthPromptDialog = false
                        showAuthModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Tizimga kirish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthPromptDialog = false }) {
                    Text("Bekor qilish", color = SlateGray)
                }
            }
        )
    }

    if (showAuthModal) {
        ModalBottomSheet(
            onDismissRequest = { showAuthModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CustomerAuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    showAuthModal = false
                    showPostJobDialog = true
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Tizimga muvaffaqiyatli kirdingiz!")
                    }
                },
                onBack = { showAuthModal = false }
            )
        }
    }

    if (showPostJobDialog) {
        PostJobVacancyDialog(
            jobToEdit = null,
            initialPhone = session.user?.phone ?: "+998 ",
            initialPerson = session.user?.fullName ?: "",
            onDismiss = { showPostJobDialog = false },
            onSubmit = { title, company, category, salary, type, location, phone, person, reqs, desc ->
                ecosystemViewModel.postJob(
                    userId = session.user?.id ?: 0L,
                    title = title,
                    companyName = company,
                    category = category,
                    salaryText = salary,
                    jobType = type,
                    location = location,
                    contactPhone = phone,
                    contactPerson = person,
                    requirements = reqs,
                    description = desc
                ) { success, msg, newJobId ->
                    showPostJobDialog = false
                    if (success && newJobId != null) {
                        AdOwnershipHelper.registerMyJob(context, newJobId)
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    jobToEdit?.let { targetJob ->
        PostJobVacancyDialog(
            jobToEdit = targetJob,
            initialPhone = targetJob.contactPhone,
            initialPerson = targetJob.contactPerson,
            onDismiss = { jobToEdit = null },
            onSubmit = { title, company, category, salary, type, location, phone, person, reqs, desc ->
                val updated = targetJob.copy(
                    title = title,
                    companyName = company,
                    category = category,
                    salaryText = salary,
                    jobType = type,
                    location = location,
                    contactPhone = phone,
                    contactPerson = person,
                    requirements = reqs,
                    description = desc
                )
                ecosystemViewModel.updateJob(updated) { success, msg ->
                    jobToEdit = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    jobToDelete?.let { job ->
        AlertDialog(
            onDismissRequest = { jobToDelete = null },
            title = { Text("Vakansiyani o‘chirish", fontWeight = FontWeight.Bold, color = SecondaryNavy) },
            text = { Text("\"${job.title}\" vakansiyasini o‘chirishni tasdiqlaysizmi?") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = job.id
                        jobToDelete = null
                        ecosystemViewModel.deleteJob(id) { success, msg ->
                            if (success) {
                                AdOwnershipHelper.removeMyJob(context, id)
                            }
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("O‘chirish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { jobToDelete = null }) {
                    Text("Bekor qilish", color = SlateGray)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(LightBackground)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "💼 GAGARIN ISH VA VAKANSIYALAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Ish toping yoki ishchi e'lon qiling!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Gagarin shahri va Mirzacho‘l bo‘ylab barcha sohadagi ish o‘rinlari.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.Work,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            // Category filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(jobCategories) { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF7C3AED) else CardSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF7C3AED) else BorderColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { ecosystemViewModel.selectJobCategory(cat) }
                                .testTag("job_cat_${cat.lowercase()}")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else SecondaryNavy,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Job Items
            items(jobsList) { job ->
                val canEdit = AdOwnershipHelper.canManageJob(context, job, session)
                val canDelete = AdOwnershipHelper.canDeleteJob(context, job, session)

                JobCardItem(
                    job = job,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    onEdit = { jobToEdit = job },
                    onDelete = { jobToDelete = job },
                    onCallEmployer = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                )
            }

            if (jobsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bu toifada hali ish e'lonlari yo‘q. Birinchi bo‘lib vakansiya qo‘shing!",
                            color = SlateGray,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Post Job Floating Action Button
        FloatingActionButton(
            onClick = {
                if (!session.isLoggedIn) {
                    showAuthPromptDialog = true
                } else {
                    showPostJobDialog = true
                }
            },
            containerColor = Color(0xFF7C3AED),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("post_job_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Vakansiya qo‘shish")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Vakansiya qo‘shish", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp)
        )
    }
}

@Composable
fun JobCardItem(
    job: JobVacancyEntity,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onCallEmployer: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("job_item_${job.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SecondaryNavy
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Filled.Business, contentDescription = null, tint = SlateGray, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = job.companyName, fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canEdit) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp).testTag("edit_job_btn_${job.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Tahrirlash",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("delete_job_btn_${job.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "O‘chirish",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF7C3AED).copy(alpha = 0.12f),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = job.jobType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Salary highlight
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECFDF5), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Payments, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Oylik maosh: ${job.salaryText}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description / Requirements
            if (job.description.isNotBlank()) {
                Text(
                    text = job.description,
                    fontSize = 12.sp,
                    color = SecondaryNavy.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (job.requirements.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Talablar: ${job.requirements}",
                        fontSize = 11.sp,
                        color = SlateGray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Location & Contact footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SlateGray, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = job.location, fontSize = 11.sp, color = SlateGray)
                }

                Button(
                    onClick = { onCallEmployer(job.contactPhone) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("call_employer_btn_${job.id}")
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aloqa: ${job.contactPhone}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobVacancyDialog(
    jobToEdit: JobVacancyEntity? = null,
    initialPhone: String = "+998 ",
    initialPerson: String = "",
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        company: String,
        category: String,
        salary: String,
        type: String,
        location: String,
        phone: String,
        person: String,
        reqs: String,
        desc: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(jobToEdit?.title ?: "") }
    var company by remember { mutableStateOf(jobToEdit?.companyName ?: "") }
    var selectedCat by remember { mutableStateOf(jobToEdit?.category ?: "Savdo & Do'kon") }
    var salary by remember { mutableStateOf(jobToEdit?.salaryText ?: "") }
    var selectedType by remember { mutableStateOf(jobToEdit?.jobType ?: jobTypes[0]) }
    var location by remember { mutableStateOf(jobToEdit?.location ?: "Gagarin shahri") }
    var phone by remember { mutableStateOf(jobToEdit?.contactPhone ?: initialPhone) }
    var contactPerson by remember { mutableStateOf(jobToEdit?.contactPerson ?: initialPerson) }
    var requirements by remember { mutableStateOf(jobToEdit?.requirements ?: "") }
    var description by remember { mutableStateOf(jobToEdit?.description ?: "") }

    val selectableJobCats = jobCategories.filter { it != "Barchasi" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (jobToEdit != null) "✏️ Vakansiyani tahrirlash" else "💼 Yangi vakansiya joylash",
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Lavozim / Kasb nomi *") },
                    placeholder = { Text("Masalan: Do‘kon sotuvchisi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Ish sohasi / Toifa *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryNavy
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(selectableJobCats) { cat ->
                        val isSel = cat == selectedCat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) Color(0xFF7C3AED) else SurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFF7C3AED) else BorderColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCat = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else SecondaryNavy,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Tashkilot yoki do‘kon nomi") },
                    placeholder = { Text("Masalan: Gagarin Savdo Markazi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Oylik maosh (so‘mda) *") },
                    placeholder = { Text("Masalan: 4 000 000 - 6 000 000 so‘m") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Bog‘lanish telefoni *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    label = { Text("Talablar (ixtiyoriy)") },
                    placeholder = { Text("Masalan: Tajriba, xushmuomalalik") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Batafsil ma'lumot") },
                    placeholder = { Text("Ish sharoitlari, tushlik, dam olish kunlari...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(title, company, selectedCat, salary, selectedType, location, phone, contactPerson, requirements, description)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_job_btn")
            ) {
                Text(if (jobToEdit != null) "Saqlash" else "E'lon qilish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish", color = SlateGray)
            }
        }
    )
}
