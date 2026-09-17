package com.example.features.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.PaymentRequest
import com.example.models.Plan
import com.example.ui.MainViewModel
import com.example.ui.theme.AnjezAccent
import com.example.ui.theme.AnjezPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    // Strict RBAC Enforcement
    if (currentUser?.isAdmin != true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Security, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("غير مصرح لك بالدخول", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "لوحة الإدارة مقتصرة فقط على حساب المدير الرئيسي (majzb012@gmail.com).",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onBack) {
                    Text("الرجوع")
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: المدفوعات, 1: المستخدمون, 2: الإحصائيات, 3: إعدادات النظام
    var userSearchQuery by remember { mutableStateOf("") }

    val pendingPayments = remember(allPayments) { allPayments.filter { it.status == "PENDING" } }
    val totalRevenueSdg = remember(allPayments) {
        allPayments.filter { it.status == "APPROVED" }.sumOf { it.amountSdg }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "لوحة إدارة أنجز AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "المدير: ${currentUser?.email}",
                    fontSize = 12.sp,
                    color = AnjezAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = AnjezPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("طلبات الدفع (${pendingPayments.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("المستخدمون (${allUsers.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("الإحصائيات", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("إعدادات كاشي والباقات", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // Payments Tab
                if (allPayments.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا توجد طلبات دفع حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allPayments) { req ->
                            PaymentAdminCard(
                                payment = req,
                                onApprove = { viewModel.approvePayment(req) },
                                onReject = { viewModel.rejectPayment(req) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Users Tab
                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { userSearchQuery = it },
                    placeholder = { Text("ابحث بالاسم أو البريد...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AnjezPrimary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val filteredUsers = allUsers.filter {
                    userSearchQuery.isBlank() ||
                            it.email.contains(userSearchQuery, ignoreCase = true) ||
                            it.name.contains(userSearchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredUsers) { u ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(u.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(u.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("الدور: ${u.role} | الباقة: ${u.planId}", fontSize = 11.sp, color = AnjezPrimary)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(if (u.isAdmin) AnjezAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(if (u.isAdmin) "Admin" else "User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Statistics Tab
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "إجمالي المستخدمين",
                            value = "${allUsers.size}",
                            icon = Icons.Default.Group,
                            color = AnjezPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "الطلبات المعلقة",
                            value = "${pendingPayments.size}",
                            icon = Icons.Default.PendingActions,
                            color = AnjezAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    StatCard(
                        title = "إجمالي الإيرادات المسجلة (المؤكدة)",
                        value = "%,d جنيه سوداني".format(totalRevenueSdg),
                        icon = Icons.Default.MonetizationOn,
                        color = SuccessGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            3 -> {
                // System & Content Settings
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("حساب كاشي المعتمد:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("402903869", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AnjezPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("التحويل يتم حصرياً عبر هذا الحساب. بنكك وأي بوابات دولية محظورة ومستبعدة.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("أسعار وحدود الباقات الشهرية:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Plan.PLANS.forEach { p ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("باقة ${p.name}: %,d SDG".format(p.priceSdg), fontSize = 13.sp)
                                    Text("${p.aiDailyLimit} AI/يوم", fontSize = 12.sp, color = AnjezPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentAdminCard(
    payment: PaymentRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateStr = remember(payment.createdAt) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(payment.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(payment.userName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(payment.userEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .background(
                            when (payment.status) {
                                "APPROVED" -> SuccessGreen.copy(alpha = 0.15f)
                                "REJECTED" -> ErrorRed.copy(alpha = 0.15f)
                                else -> AnjezAccent.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (payment.status) {
                            "APPROVED" -> "مؤكد ومفعّل"
                            "REJECTED" -> "مرفوض"
                            else -> "معلق قيد المراجعة"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (payment.status) {
                            "APPROVED" -> SuccessGreen
                            "REJECTED" -> ErrorRed
                            else -> AnjezAccent
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("الباقة المطلوبة: باقة ${payment.planId} (%,d SDG)".format(payment.amountSdg), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("مرجع التحويل / الإيصال: ${payment.receiptReference}", fontSize = 13.sp, color = AnjezPrimary)
            Text("حساب كاشي المستلم: ${payment.kashiNumber} | التاريخ: $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (payment.status == "PENDING") {
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تأكيد وتفعيل الباقة", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("رفض الدفع", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
