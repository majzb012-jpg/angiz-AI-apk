package com.example.features.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Plan
import com.example.ui.MainViewModel
import com.example.ui.theme.AnjezAccent
import com.example.ui.theme.AnjezPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val usage by viewModel.currentUsage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val plan = remember(user?.planId) { Plan.getPlan(user?.planId ?: "free") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showKashiModal by remember { mutableStateOf(false) }
    var selectedPlanToUpgrade by remember { mutableStateOf<Plan?>(null) }
    var receiptRefInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "حسابي وإعدادات الاشتراك",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AnjezPrimary, AnjezAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (user?.isAdmin == true) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "مستخدم أنجز AI",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = user?.email ?: "",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (user?.isAdmin == true) AnjezAccent.copy(alpha = 0.2f) else AnjezPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (user?.isAdmin == true) "🛡️ مدير النظام (Admin)" else "👤 حساب مستخدم",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user?.isAdmin == true) AnjezAccent else AnjezPrimary
                        )
                    }
                }
            }
        }

        // Admin Entry Button (Only if user is Admin)
        if (user?.isAdmin == true) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onNavigateToAdmin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AnjezAccent)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "لوحة الإدارة والتحكم (Admin Dashboard)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Current Plan & Usage Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الباقة الحالية",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "باقة ${plan.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AnjezPrimary
                        )
                    }

                    Button(
                        onClick = { showKashiModal = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AnjezPrimary)
                    ) {
                        Text("ترقية الباقة عبر كاشي", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Meters
                UsageProgressBar(
                    label = "عمليات الذكاء الاصطناعي اليومية",
                    used = usage?.aiCount ?: 0,
                    limit = plan.aiDailyLimit,
                    icon = Icons.Default.AutoAwesome,
                    color = AnjezPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                UsageProgressBar(
                    label = "الصور اليومية",
                    used = usage?.imageCount ?: 0,
                    limit = plan.imageDailyLimit,
                    icon = Icons.Default.Image,
                    color = AnjezAccent
                )

                Spacer(modifier = Modifier.height(12.dp))

                UsageProgressBar(
                    label = "الفيديوهات اليومية",
                    used = usage?.videoCount ?: 0,
                    limit = plan.videoDailyLimit,
                    icon = Icons.Default.Videocam,
                    color = Color(0xFF6366F1)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "يتم تجديد عدادات الاستخدام تلقائياً كل 24 ساعة.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Developer Section Card
        DeveloperSectionCard(
            onCopyEmail = {
                clipboardManager.setText(AnnotatedString("majzb012@gmail.com"))
                viewModel.showToast("تم نسخ بريد المطور: majzb012@gmail.com")
            },
            onSendEmail = {
                try {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:majzb012@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "تواصل بخصوص تطبيق أنجز AI")
                    }
                    context.startActivity(emailIntent)
                } catch (e: Exception) {
                    clipboardManager.setText(AnnotatedString("majzb012@gmail.com"))
                    viewModel.showToast("تم نسخ بريد المطور: majzb012@gmail.com")
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Settings (Dark/Light mode & Logout)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = AnjezAccent
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("الوضع الداكن (Dark Mode)", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onLogout() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تسجيل الخروج",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Kashi Upgrade Dialog
    if (showKashiModal) {
        AlertDialog(
            onDismissRequest = { showKashiModal = false },
            title = {
                Text(
                    text = "ترقية الاشتراك — الدفع عبر كاشي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "وسيلة الدفع الوحيدة المعتمدة هي محفظة «كاشي».",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Kashi Account Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AnjezPrimary.copy(alpha = 0.1f))
                            .border(1.dp, AnjezPrimary, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("رقم حساب كاشي للتحويل:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "402903869",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AnjezPrimary,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("اختر الباقة:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    Plan.PLANS.filter { it.id != "free" }.forEach { p ->
                        val isSelected = selectedPlanToUpgrade?.id == p.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPlanToUpgrade = p },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AnjezPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(p.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("${p.aiDailyLimit} عملية AI يومياً", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "%,d SDG".format(p.priceSdg),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AnjezPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = receiptRefInput,
                        onValueChange = { receiptRefInput = it },
                        label = { Text("رقم العملية أو اسم المحوّل (مرجع الإيصال)") },
                        placeholder = { Text("مثلاً: تم التحويل من 0912345678") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ملاحظة: سيتم إرسال الطلب إلى لوحة الإدارة للمراجعة وتفعيل باقتك بمجرد التأكد.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val chosen = selectedPlanToUpgrade ?: Plan.PLANS.first { it.id == "basic" }
                        viewModel.submitKashiPayment(
                            planId = chosen.id,
                            amount = chosen.priceSdg,
                            reference = receiptRefInput
                        )
                        showKashiModal = false
                        receiptRefInput = ""
                        selectedPlanToUpgrade = null
                    },
                    enabled = receiptRefInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AnjezPrimary)
                ) {
                    Text("إرسال طلب الدفع", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKashiModal = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun UsageProgressBar(
    label: String,
    used: Int,
    limit: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    val progress = if (limit > 0) (used.toFloat() / limit).coerceIn(0f, 1f) else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "$used / $limit",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun DeveloperSectionCard(
    onCopyEmail: () -> Unit,
    onSendEmail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AnjezPrimary, AnjezAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = "مطور التطبيق",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مطور البرنامج",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AnjezAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Lead Developer",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AnjezAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "مجذوب خضر محمد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "تم تصميم وتطوير تطبيق «أنجز AI» بالكامل بواسطة المهندس مجذوب خضر محمد لتقديم منصة ذكاء اصطناعي شاملة وسريعة تلبي كافة احتياجاتك اليومية والمهنية بأعلى معايير الجودة.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "طريقة التواصل والدعم المباشر مع المطور:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "majzb012@gmail.com",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AnjezPrimary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSendEmail,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AnjezPrimary)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مراسلة المطور", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCopyEmail,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نسخ البريد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "الإصدار 1.0.0 (النسخة الرسمية) • جميع الحقوق محفوظة لـ أنجز AI",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
