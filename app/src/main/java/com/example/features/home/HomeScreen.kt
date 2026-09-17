package com.example.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Plan
import com.example.ui.MainViewModel
import com.example.ui.theme.AnjezAccent
import com.example.ui.theme.AnjezPrimary
import com.example.ui.theme.WarningAmber

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val samplePrompt: String,
    val forceCategory: String? = null
)

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToTools: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var selectedAttachmentType by remember { mutableStateOf<String?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }

    val user by viewModel.currentUser.collectAsState()
    val usage by viewModel.currentUsage.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val plan = remember(user?.planId) { Plan.getPlan(user?.planId ?: "free") }

    val quickActions = remember {
        listOf(
            QuickAction("اكتب إعلان", Icons.Default.Edit, "اكتب لي إعلان جذاب لمحل ملابس شبابي بالخرطوم مع خصومات", "writing"),
            QuickAction("اشرح درس", Icons.Default.School, "اشرح لي قوانين نيوتن للحركة في الفيزياء بطريقة مبسطة مع أمثلة", "study"),
            QuickAction("اكتب كود", Icons.Default.Code, "اكتب لي كود بايثون لقراءة ملف CSV وحساب متوسط المبيعات", "code"),
            QuickAction("صمم صورة", Icons.Default.Image, "صمم لي صورة إعلان لمنتج قهوة سودانية تقليدية على الطراز الحديث", "image"),
            QuickAction("بحث مباشر", Icons.Default.Search, "ما هي أحدث تطورات الذكاء الاصطناعي اليوم؟", "search"),
            QuickAction("لخص مستند", Icons.Default.Description, "لخص لي أهم بنود هذا العقد واستخرج الالتزامات المالية", "file")
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Offline Warning Banner
        AnimatedVisibility(visible = isOffline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarningAmber.copy(alpha = 0.15f))
                    .border(1.dp, WarningAmber, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = WarningAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الإنترنت غير متصل. بعض خدمات أنجز AI تحتاج اتصالاً بالإنترنت.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        if (isOffline) Spacer(modifier = Modifier.height(14.dp))

        // Top Greeting Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "شعار أنجز AI",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "أهلاً يا ${user?.name ?: "صديقنا"}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "أها، داير تنجز شنو؟",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Plan Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AnjezPrimary.copy(alpha = 0.15f))
                    .border(1.dp, AnjezPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = plan.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AnjezPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Real Daily Usage Meter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UsageMeterItem(
                    label = "عمليات AI",
                    used = usage?.aiCount ?: 0,
                    limit = plan.aiDailyLimit,
                    icon = Icons.Default.AutoAwesome,
                    color = AnjezPrimary
                )
                UsageMeterItem(
                    label = "الصور",
                    used = usage?.imageCount ?: 0,
                    limit = plan.imageDailyLimit,
                    icon = Icons.Default.Image,
                    color = AnjezAccent
                )
                UsageMeterItem(
                    label = "الفيديو",
                    used = usage?.videoCount ?: 0,
                    limit = plan.videoDailyLimit,
                    icon = Icons.Default.Edit,
                    color = Color(0xFF6366F1)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Central AI Prompt Box (The Core)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Attached file chip if any
                if (attachmentName != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AnjezPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📎 $attachmentName",
                            fontSize = 12.sp,
                            color = AnjezPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable {
                                attachmentName = null
                                selectedAttachmentType = null
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            "اكتب طلبك هنا... (اكتب لي إعلان، حلل صورة، اشرح درس، اكتب كود...)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AnjezPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom actions row: Attachments + Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                selectedAttachmentType = "image"
                                attachmentName = "صورة مرفقة (جاهزة للتحليل)"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "إرفاق صورة",
                                tint = if (selectedAttachmentType == "image") AnjezPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedAttachmentType = "file"
                                attachmentName = "مستند مستلم (جاهز للتلخيص)"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "إرفاق ملف",
                                tint = if (selectedAttachmentType == "file") AnjezPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedAttachmentType = "audio"
                                attachmentName = "تسجيل صوتي (جاهز للتفريغ)"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "إرفاق صوت",
                                tint = if (selectedAttachmentType == "audio") AnjezPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Send Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AnjezPrimary, AnjezAccent))
                            )
                            .clickable(enabled = promptInput.isNotBlank() || attachmentName != null) {
                                val text = promptInput
                                val attType = selectedAttachmentType
                                promptInput = ""
                                selectedAttachmentType = null
                                attachmentName = null
                                viewModel.sendMessage(
                                    prompt = text,
                                    attachmentUri = if (attType != null) "content://attachment" else null,
                                    attachmentType = attType
                                )
                                onNavigateToChat()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Suggestions Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "إنجازات مقترحة سريعة",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "كل الأدوات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AnjezPrimary,
                modifier = Modifier.clickable { onNavigateToTools() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quickActions) { item ->
                Card(
                    modifier = Modifier
                        .width(170.dp)
                        .clickable {
                            promptInput = item.samplePrompt
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AnjezPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = AnjezPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.samplePrompt,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Feature Highlight Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "💡 المحادثة هي مركز أنجز AI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "لست بحاجة لفتح أداة معينة كل مرة. اكتب سؤالك، أو ألصق الكود، أو اسأل عن درسك، وسيتولى نظام التوجيه الذكي إنجاز المهمة مباشرة.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun UsageMeterItem(
    label: String,
    used: Int,
    limit: Int,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$used / $limit",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
