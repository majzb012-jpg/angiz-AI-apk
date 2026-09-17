package com.example.features.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ServiceState
import com.example.ui.MainViewModel
import com.example.ui.theme.AnjezAccent
import com.example.ui.theme.AnjezPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen

data class ToolCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val state: ServiceState = ServiceState.AVAILABLE
)

@Composable
fun ToolsHubScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit
) {
    var activeModalTool by remember { mutableStateOf<ToolCardItem?>(null) }

    val tools = remember {
        listOf(
            ToolCardItem("writing", "مركز الكتابة والمحتوى", "بوستات، إعلانات، مقالات، إعادة صياغة بمختلف اللهجات", Icons.Default.Edit, AnjezPrimary),
            ToolCardItem("image", "مركز الصور والتصميم", "توليد تصاميم، بوسترات، تحليل صور، وتعديل وصفي", Icons.Default.Image, AnjezAccent),
            ToolCardItem("study", "مركز الدراسة والمدرس الذكي", "شرح دروس، تبسيط مفاهيم، اختبارات، وخطط دراسية", Icons.Default.School, Color(0xFF8B5CF6)),
            ToolCardItem("coding", "مركز البرمجة والأكواد", "كتابة، شرح، Debugging وحل أخطاء البرمجة", Icons.Default.Code, Color(0xFF3B82F6)),
            ToolCardItem("file", "مركز المستندات والملفات", "تلخيص وتحليل ملفات PDF و DOCX واستخراج الجداول", Icons.Default.Description, Color(0xFF10B981)),
            ToolCardItem("data", "تحليل البيانات والإحصاء", "تنظيف الجداول، واستخراج الإحصائيات المهمة من CSV", Icons.Default.Analytics, Color(0xFFEC4899)),
            ToolCardItem("search", "البحث الذكي المباشر", "البحث المباشر في الويب مع توثيق المصادر والروابط", Icons.Default.Search, Color(0xFF06B6D4)),
            ToolCardItem("video", "مركز الفيديو والسيناريو", "كتابة سكريبتات المشاهد والستوري بورد", Icons.Default.Videocam, Color(0xFFF97316)),
            ToolCardItem("audio", "مركز الصوت والتفريغ", "تفريغ وتلخيص التسجيلات الصوتية باللهجات العربية", Icons.Default.Mic, Color(0xFF6366F1))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "مراكز العمل والإنتاج",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "اختر الأداة المناسبة لمهمتك أو اطلبها مباشرة من المحادثة",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tools) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { activeModalTool = item },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(item.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(22.dp))
                            }

                            // Availability dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (item.state) {
                                            ServiceState.AVAILABLE -> SuccessGreen
                                            ServiceState.UNAVAILABLE -> ErrorRed
                                            ServiceState.MAINTENANCE -> AnjezAccent
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            maxLines = 3
                        )
                    }
                }
            }
        }
    }

    // Modal tool prompt generator dialog
    activeModalTool?.let { tool ->
        ToolInteractiveDialog(
            tool = tool,
            onDismiss = { activeModalTool = null },
            onSubmit = { prompt, category ->
                activeModalTool = null
                viewModel.sendMessage(prompt, forceService = category)
                onNavigateToChat()
            }
        )
    }
}

@Composable
fun ToolInteractiveDialog(
    tool: ToolCardItem,
    onDismiss: () -> Unit,
    onSubmit: (prompt: String, category: String) -> Unit
) {
    var inputMain by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("عام") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(tool.icon, contentDescription = null, tint = tool.color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(tool.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = tool.subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Options per tool
                when (tool.id) {
                    "writing" -> {
                        Text("اللهجة أو الأسلوب:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("سوداني", "فصحى", "تسويقي", "مصري").forEach { dialect ->
                                FilterChip(
                                    selected = selectedOption == dialect,
                                    onClick = { selectedOption = dialect },
                                    label = { Text(dialect, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    "coding" -> {
                        Text("لغة البرمجة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Python", "Kotlin", "JS/React", "SQL").forEach { lang ->
                                FilterChip(
                                    selected = selectedOption == lang,
                                    onClick = { selectedOption = lang },
                                    label = { Text(lang, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    "video" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AnjezAccent.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "ملاحظة: توليد سكريبت وتخطيط المشاهد متاح. تحويل الفيديو إلى MP4 مباشرة تحت الربط بالنموذج المتقدم.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputMain,
                    onValueChange = { inputMain = it },
                    placeholder = {
                        Text(
                            when (tool.id) {
                                "writing" -> "اكتب فكرة الإعلان أو المنشور..."
                                "study" -> "اكتب عنوان الدرس أو المفهوم المراد شرحه..."
                                "coding" -> "صف الكود أو المشكلة التي تريد حلها..."
                                "image" -> "صف تفاصيل الصورة التي تريد توليدها..."
                                "search" -> "اكتب موضوع البحث في الويب..."
                                else -> "اكتب تفاصيل طلبك هنا..."
                            },
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputMain.isNotBlank()) {
                        val finalPrompt = when (tool.id) {
                            "writing" -> "اكتب محتوى بأسلوب [$selectedOption]: $inputMain"
                            "coding" -> "لغة [$selectedOption]: $inputMain"
                            else -> inputMain
                        }
                        onSubmit(finalPrompt, tool.id)
                    }
                },
                enabled = inputMain.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = tool.color)
            ) {
                Text("إنجاز المهمة", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
