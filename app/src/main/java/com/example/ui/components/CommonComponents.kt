package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StudentAvatar(
    name: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "S" }

    val bg = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp
        )
    }
}

@Composable
fun StatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        AttendanceStatus.PRESENT -> Triple(StatusPresentBg, StatusPresent, "Present")
        AttendanceStatus.ABSENT -> Triple(StatusAbsentBg, StatusAbsent, "Absent")
        AttendanceStatus.LEAVE -> Triple(StatusLeaveBg, StatusLeave, "Leave")
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ClassSectionFilterRow(
    classes: List<String>,
    sections: List<String>,
    selectedClass: String,
    selectedSection: String,
    onClassSelected: (String) -> Unit,
    onSectionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Classes Chip Row
        Text(
            text = "Select Class",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            classes.forEach { c ->
                FilterChip(
                    selected = (c == selectedClass),
                    onClick = { onClassSelected(c) },
                    label = { Text(c, fontWeight = if (c == selectedClass) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sections Chip Row
        Text(
            text = "Select Section",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEach { s ->
                FilterChip(
                    selected = (s == selectedSection),
                    onClick = { onSectionSelected(s) },
                    label = { Text("Sec $s", fontWeight = if (s == selectedSection) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun AttendanceSegmentedControl(
    currentStatus: AttendanceStatus,
    onStatusChange: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Present Pill
        val isPresent = currentStatus == AttendanceStatus.PRESENT
        val pBg by animateColorAsState(if (isPresent) StatusPresent else Color.Transparent, label = "pBg")
        val pFg by animateColorAsState(if (isPresent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "pFg")

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(pBg)
                .clickable { onStatusChange(AttendanceStatus.PRESENT) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPresent) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = pFg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text("P", color = pFg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Absent Pill
        val isAbsent = currentStatus == AttendanceStatus.ABSENT
        val aBg by animateColorAsState(if (isAbsent) StatusAbsent else Color.Transparent, label = "aBg")
        val aFg by animateColorAsState(if (isAbsent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "aFg")

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(aBg)
                .clickable { onStatusChange(AttendanceStatus.ABSENT) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAbsent) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = aFg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text("A", color = aFg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Leave Pill
        val isLeave = currentStatus == AttendanceStatus.LEAVE
        val lBg by animateColorAsState(if (isLeave) StatusLeave else Color.Transparent, label = "lBg")
        val lFg by animateColorAsState(if (isLeave) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "lFg")

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(lBg)
                .clickable { onStatusChange(AttendanceStatus.LEAVE) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLeave) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = lFg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text("L", color = lFg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.buttonColors()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Native Android DatePickerDialog trigger
 */
fun showAndroidDatePicker(
    context: android.content.Context,
    currentDateStr: String,
    allowFutureDates: Boolean = false,
    onDateSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    try {
        val parsed = sdf.parse(currentDateStr)
        if (parsed != null) {
            cal.time = parsed
        }
    } catch (e: Exception) {
        // use now
    }

    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance()
            selected.set(year, month, dayOfMonth)
            val result = sdf.format(selected.time)
            onDateSelected(result)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    if (!allowFutureDates) {
        dialog.datePicker.maxDate = System.currentTimeMillis()
    }

    dialog.show()
}
