package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.data.local.DeviceSessionEntity
import com.example.data.remote.SyncState
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DeviceFormFactor {
    COMPACT_MOBILE,
    MEDIUM_TABLET,
    EXPANDED_DESKTOP
}

fun resolveDeviceFormFactor(width: Dp): DeviceFormFactor = when {
    width >= 960.dp -> DeviceFormFactor.EXPANDED_DESKTOP
    width >= 600.dp -> DeviceFormFactor.MEDIUM_TABLET
    else -> DeviceFormFactor.COMPACT_MOBILE
}

data class DesktopNavItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String,
    val badgeCount: Int = 0,
    val shortcutHint: String = ""
)

data class DataTableColumn(
    val header: String,
    val weight: Float = 1f,
    val alignEnd: Boolean = false
)

@Composable
fun DesktopPermanentSidebar(
    displayName: String,
    loginId: String,
    roleLabel: String,
    roleIcon: ImageVector,
    primaryItems: List<DesktopNavItem>,
    selectedKey: String,
    onSelectPrimary: (String) -> Unit,
    secondaryItems: List<DesktopNavItem> = emptyList(),
    selectedSecondaryKey: String? = null,
    onSelectSecondary: ((String) -> Unit)? = null,
    activeSessionCount: Int,
    darkTheme: Boolean,
    onOpenActiveSessions: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val strings = LocalAppStrings.current

    Surface(
        modifier = modifier
            .width(256.dp)
            .fillMaxHeight()
            .testTag("desktop_sidebar"),
        color = SkillBridgeColors.DeepNavy,
        contentColor = SkillBridgeColors.PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brand header matching SkillBridge Brand Showcase Poster
            SkillBridgeLogo(
                darkBackground = true,
                compactOnly = false,
                showTagline = true
            )

            Surface(
                shape = RoundedCornerShape(SkillBridgeRadius.pill),
                color = SkillBridgeColors.BridgeBlue.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, SkillBridgeColors.SoftCyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LaptopMac,
                        contentDescription = null,
                        tint = SkillBridgeColors.SoftCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (lang == AppLanguage.AR) "منصة سطح المكتب الموحدة" else "Unified Desktop Workspace",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SkillBridgeColors.SoftCyan
                    )
                }
            }

            // Authenticated account card
            Surface(
                shape = RoundedCornerShape(SkillBridgeRadius.md),
                color = SkillBridgeColors.DeepNavyElevated,
                border = BorderStroke(1.dp, SkillBridgeColors.PureWhite.copy(alpha = 0.10f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkillBridgeColors.BridgeBlue.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = roleLabel,
                            tint = SkillBridgeColors.SoftCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@$loginId • $roleLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkillBridgeColors.SoftCyan.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = SkillBridgeColors.PureWhite.copy(alpha = 0.12f))

            // Scrollable navigation sections
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                primaryItems.forEach { item ->
                    SidebarNavRow(
                        item = item,
                        selected = selectedSecondaryKey == null && selectedKey == item.key,
                        onClick = { onSelectPrimary(item.key) }
                    )
                }

                if (secondaryItems.isNotEmpty() && onSelectSecondary != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (lang == AppLanguage.AR) "بوابات واختصارات" else "WORKSPACE TOOLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkillBridgeColors.SoftCyan.copy(alpha = 0.65f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    secondaryItems.forEach { item ->
                        SidebarNavRow(
                            item = item,
                            selected = selectedSecondaryKey == item.key,
                            onClick = { onSelectSecondary(item.key) }
                        )
                    }
                }
            }

            HorizontalDivider(color = SkillBridgeColors.PureWhite.copy(alpha = 0.12f))

            // Active Cross-Device Sessions button
            Surface(
                onClick = onOpenActiveSessions,
                shape = RoundedCornerShape(SkillBridgeRadius.sm),
                color = SkillBridgeColors.PureWhite.copy(alpha = 0.08f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("desktop_sessions_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Devices,
                            contentDescription = null,
                            tint = SkillBridgeColors.LimeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (lang == AppLanguage.AR) "الأجهزة المتصلة" else "Active Devices",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SkillBridgeColors.PureWhite
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        color = SkillBridgeColors.LimeGreen.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "$activeSessionCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.LimeGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Theme + Language quick row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onToggleDarkTheme,
                    shape = RoundedCornerShape(SkillBridgeRadius.sm),
                    color = SkillBridgeColors.PureWhite.copy(alpha = 0.08f),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("desktop_theme_toggle")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = SkillBridgeColors.SoftCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Surface(
                    onClick = onToggleLanguage,
                    shape = RoundedCornerShape(SkillBridgeRadius.sm),
                    color = SkillBridgeColors.PureWhite.copy(alpha = 0.08f),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("desktop_language_toggle")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = SkillBridgeColors.SoftCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.languageToggleLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.PureWhite
                        )
                    }
                }
            }

            // Sign out action (consistent testTag across all 4 roles)
            Surface(
                onClick = onSignOut,
                shape = RoundedCornerShape(SkillBridgeRadius.sm),
                color = SkillBridgeColors.DisputedRed.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, SkillBridgeColors.DisputedRed.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("role_sign_out_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = strings.signOut,
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.signOut,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = SkillBridgeColors.PureWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarNavRow(
    item: DesktopNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bg = when {
        selected -> SkillBridgeColors.BridgeBlue.copy(alpha = 0.24f)
        isHovered -> SkillBridgeColors.PureWhite.copy(alpha = 0.10f)
        else -> Color.Transparent
    }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(SkillBridgeRadius.sm),
        color = bg,
        border = if (selected) BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.65f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .hoverable(interactionSource)
            .testTag(item.testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) SkillBridgeColors.LimeGreen else SkillBridgeColors.SoftCyan,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) SkillBridgeColors.PureWhite else SkillBridgeColors.PureWhite.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (item.badgeCount > 0) {
                Surface(
                    shape = RoundedCornerShape(SkillBridgeRadius.pill),
                    color = SkillBridgeColors.LimeGreen
                ) {
                    Text(
                        text = "${item.badgeCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeColors.DeepNavy,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (item.shortcutHint.isNotBlank()) {
                Text(
                    text = item.shortcutHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgeColors.PureWhite.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
fun DesktopWorkspaceTopBar(
    sectionTitle: String,
    subtitle: String,
    syncState: SyncState,
    lastSyncedEpochMs: Long,
    pendingWritesCount: Int,
    isOffline: Boolean,
    activeSessionCount: Int,
    unreadNotifications: Int,
    notificationsTestTag: String,
    onManualSync: () -> Unit,
    onOpenActiveSessions: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSearch: (() -> Unit)? = null,
    canGoBack: Boolean = false,
    onGoBack: (() -> Unit)? = null
) {
    val lang = LocalAppLanguage.current
    val strings = LocalAppStrings.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (canGoBack && onGoBack != null) {
                        OutlinedButton(
                            onClick = onGoBack,
                            modifier = Modifier.height(40.dp).testTag("top_back_btn")
                        ) {
                            Text(strings.backLabel)
                        }
                    }
                    Column {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onOpenSearch != null) {
                        IconButton(
                            onClick = onOpenSearch,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("top_search_btn")
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = strings.searchPlaceholder)
                        }
                    }

                    SyncStatusChip(
                        syncState = syncState,
                        lastSyncedEpochMs = lastSyncedEpochMs,
                        pendingWritesCount = pendingWritesCount,
                        isOffline = isOffline,
                        onManualSync = onManualSync
                    )

                    Surface(
                        onClick = onOpenActiveSessions,
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("active_sessions_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Devices,
                                contentDescription = null,
                                tint = SkillBridgeColors.BridgeBlueDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (lang == AppLanguage.AR) "$activeSessionCount أجهزة" else "$activeSessionCount devices",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag(notificationsTestTag)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge { Text("$unreadNotifications") }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = strings.notificationsTitle)
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun SyncStatusChip(
    syncState: SyncState,
    lastSyncedEpochMs: Long,
    pendingWritesCount: Int,
    isOffline: Boolean,
    onManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val effectiveState = if (isOffline) SyncState.OFFLINE_QUEUED else syncState
    val timeLabel = remember(lastSyncedEpochMs) {
        SimpleDateFormat("HH:mm", Locale.US).format(Date(lastSyncedEpochMs))
    }

    val (bg, fg, icon, label) = when (effectiveState) {
        SyncState.LIVE_CLOUD -> Quadruple(
            SkillBridgeColors.VerifiedBg,
            SkillBridgeColors.VerifiedGreen,
            Icons.Filled.CloudDone,
            if (lang == AppLanguage.AR) "مزامنة سحابية • $timeLabel" else "Live Sync • $timeLabel"
        )
        SyncState.LOCAL_SYNCED -> Quadruple(
            SkillBridgeColors.ReviewBg,
            SkillBridgeColors.BridgeBlueDark,
            Icons.Filled.CloudDone,
            if (lang == AppLanguage.AR) "موحد مركزيًا • $timeLabel" else "Central DB • $timeLabel"
        )
        SyncState.SYNCING -> Quadruple(
            SkillBridgeColors.PendingBg,
            SkillBridgeColors.PendingAmber,
            Icons.Filled.Sync,
            if (lang == AppLanguage.AR) "جارٍ المزامنة..." else "Syncing..."
        )
        SyncState.OFFLINE_QUEUED -> Quadruple(
            SkillBridgeColors.DisputedBg,
            SkillBridgeColors.DisputedRed,
            Icons.Filled.CloudOff,
            if (lang == AppLanguage.AR) "بانتظار الشبكة ($pendingWritesCount)" else "Offline Queue ($pendingWritesCount)"
        )
    }

    Surface(
        onClick = onManualSync,
        shape = RoundedCornerShape(SkillBridgeRadius.pill),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.35f)),
        modifier = modifier
            .height(38.dp)
            .testTag("sync_status_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(15.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg,
                maxLines = 1
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
fun ResponsiveKpiGrid(
    isDesktop: Boolean,
    desktopColumns: Int = 4,
    items: List<@Composable (Modifier) -> Unit>,
    modifier: Modifier = Modifier
) {
    val columns = if (isDesktop) desktopColumns.coerceAtLeast(2) else 2
    val rows = items.chunked(columns)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { cell ->
                    cell(Modifier.weight(1f))
                }
                repeat(columns - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ResponsiveChartRow(
    isDesktop: Boolean,
    charts: List<@Composable (Modifier) -> Unit>,
    modifier: Modifier = Modifier
) {
    if (isDesktop) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            charts.forEach { chart ->
                chart(Modifier.weight(1f))
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            charts.forEach { chart ->
                chart(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun DesktopListDetailSplit(
    leftWeight: Float = 0.43f,
    leftPane: @Composable () -> Unit,
    rightPane: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(leftWeight)
                .fillMaxHeight()
        ) {
            leftPane()
        }
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(
            modifier = Modifier
                .weight(1f - leftWeight)
                .fillMaxHeight()
        ) {
            rightPane()
        }
    }
}

@Composable
fun <T> DesktopDataTable(
    columns: List<DataTableColumn>,
    rows: List<T>,
    rowKey: (T) -> Any,
    selectedKey: Any? = null,
    onRowClick: ((T) -> Unit)? = null,
    emptyMessage: String = "No records found.",
    modifier: Modifier = Modifier,
    cellContent: @Composable (row: T, colIndex: Int) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SkillBridgeRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SkillBridgeColors.DeepNavy.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                columns.forEach { col ->
                    Text(
                        text = col.header,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = if (col.alignEnd) TextAlign.End else TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(col.weight)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                rows.forEachIndexed { idx, row ->
                    val key = rowKey(row)
                    val isSelected = selectedKey != null && selectedKey == key
                    val interactionSource = remember(key) { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()
                    val rowBg = when {
                        isSelected -> SkillBridgeColors.SoftCyanTint.copy(alpha = 0.65f)
                        isHovered -> MaterialTheme.colorScheme.surfaceContainerLow
                        idx % 2 == 1 -> MaterialTheme.colorScheme.surfaceContainerLowest
                        else -> Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .then(
                                if (onRowClick != null) {
                                    Modifier
                                        .hoverable(interactionSource)
                                        .clickable(interactionSource = interactionSource, indication = null) {
                                            onRowClick(row)
                                        }
                                } else {
                                    Modifier.hoverable(interactionSource)
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        columns.forEachIndexed { colIdx, col ->
                            Box(
                                modifier = Modifier.weight(col.weight),
                                contentAlignment = if (col.alignEnd) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                cellContent(row, colIdx)
                            }
                        }
                    }
                    if (idx < rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveSessionsDialog(
    sessions: List<DeviceSessionEntity>,
    currentSessionId: String,
    syncState: SyncState,
    onManualSync: () -> Unit,
    onRevokeSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val fmt = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Devices, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark)
                Text(
                    text = if (lang == AppLanguage.AR) "الأجهزة والجلسات المتزامنة" else "Connected Devices & Sync Sessions"
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (lang == AppLanguage.AR)
                        "يستخدم تطبيق الموبايل ونسخة سطح المكتب نفس الحساب وقاعدة البيانات المركزية فورياً."
                    else
                        "Mobile and Desktop/Laptop share one central database and authenticated account state in real time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (sessions.isEmpty()) {
                    Text(
                        text = if (lang == AppLanguage.AR) "لا توجد جلسات مسجلة بعد." else "No device sessions recorded yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions, key = { it.id }) { sess ->
                            val isCurrent = sess.id == currentSessionId
                            val isDesktop = sess.deviceType == "DESKTOP_LAPTOP"
                            Surface(
                                shape = RoundedCornerShape(SkillBridgeRadius.sm),
                                color = if (sess.revoked) SkillBridgeColors.DisputedBg.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) SkillBridgeColors.BridgeBlue else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDesktop) Icons.Filled.LaptopMac else Icons.Filled.Smartphone,
                                        contentDescription = sess.deviceType,
                                        tint = if (sess.revoked) SkillBridgeColors.DisputedRed else SkillBridgeColors.BridgeBlueDark
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sess.deviceLabel.ifBlank { sess.deviceType },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${sess.windowWidthDp}dp • ${fmt.format(Date(sess.lastActiveEpochMs))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    when {
                                        sess.revoked -> StatusPill(
                                            if (lang == AppLanguage.AR) "ملغاة" else "Revoked",
                                            StatusTone.DISPUTED
                                        )
                                        isCurrent -> StatusPill(
                                            if (lang == AppLanguage.AR) "هذا الجهاز" else "This Device",
                                            StatusTone.VERIFIED
                                        )
                                        else -> OutlinedButton(
                                            onClick = { onRevokeSession(sess.id) },
                                            modifier = Modifier
                                                .height(38.dp)
                                                .testTag("revoke_session_${sess.id}")
                                        ) {
                                            Text(
                                                text = if (lang == AppLanguage.AR) "إنهاء" else "Revoke",
                                                color = SkillBridgeColors.DisputedRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onManualSync,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (lang == AppLanguage.AR) "مزامنة الآن"
                    else if (syncState == SyncState.SYNCING) "Syncing..." else "Sync Now"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (lang == AppLanguage.AR) "إغلاق" else "Close")
            }
        }
    )
}
