package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.AppRoleMode
import com.example.core.AssetRegistry
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.data.model.BadgeProgressItem
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing

/**
 * Badges Gallery Screen: displays all 9 SkillBridge badges with Locked/Unlocked state,
 * completion progress, remaining evidence steps, and share toggle for earned badges.
 */
@Composable
fun BadgesGalleryScreen(
    badgeProgressList: List<BadgeProgressItem>,
    isGuest: Boolean,
    onToggleShareable: (badgeId: String, currentShareable: Boolean) -> Unit,
    onProveNextStep: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val unlockedCount = badgeProgressList.count { it.isUnlocked }
    val totalCount = badgeProgressList.size

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("badges_gallery_list"),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy),
                shape = RoundedCornerShape(SkillBridgeRadius.lg)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.badgesTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeColors.PureWhite
                    )
                    Text(
                        text = if (lang == AppLanguage.AR) {
                            "المفتوحة: $unlockedCount / $totalCount — تُفتح الشارات تلقائيًا عند تحقق الأدلة الموثّقة فقط."
                        } else {
                            "Unlocked: $unlockedCount / $totalCount — Badges unlock strictly from verified actions & mentor reviews."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.SoftCyan
                    )
                    LinearProgressIndicator(
                        progress = { if (totalCount == 0) 0f else unlockedCount.toFloat() / totalCount },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(SkillBridgeRadius.pill)),
                        color = SkillBridgeColors.LimeGreen,
                        trackColor = SkillBridgeColors.PureWhite.copy(alpha = 0.2f)
                    )
                }
            }
        }

        items(badgeProgressList, key = { it.definition.id }) { item ->
            val def = item.definition
            val visual = AssetRegistry.badgeVisualFor(def.id)
            val unlocked = item.isUnlocked
            Card(
                modifier = Modifier.fillMaxWidth().testTag("badge_card_${def.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (unlocked) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(
                    1.dp,
                    if (unlocked) SkillBridgeColors.VerifiedGreen.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (unlocked) visual.accentLight.copy(alpha = 0.16f) else SkillBridgeColors.LockedBg,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (unlocked) visual.icon else Icons.Filled.Lock,
                                    contentDescription = def.title.get(lang),
                                    tint = if (unlocked) visual.accentLight else SkillBridgeColors.LockedSlate
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = def.title.get(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = def.description.get(lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusPill(
                            text = if (unlocked) strings.unlockedBadgesLabel else strings.lockedBadgesLabel,
                            tone = if (unlocked) StatusTone.VERIFIED else StatusTone.LOCKED,
                            icon = if (unlocked) Icons.Filled.Verified else Icons.Filled.Lock
                        )
                    }

                    Text(
                        text = if (lang == AppLanguage.AR) "معيار الاستحقاق: ${def.criteria.ar}" else "Criteria: ${def.criteria.en}",
                        style = MaterialTheme.typography.labelLarge,
                        color = SkillBridgeColors.BridgeBlueDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.progressPercent}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        StatusPill(
                            text = "+${def.pointsAwarded} ${strings.pointsLabel}",
                            tone = if (unlocked) StatusTone.PAID else StatusTone.NEUTRAL
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (item.progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(SkillBridgeRadius.pill)),
                        color = if (unlocked) SkillBridgeColors.VerifiedGreen else SkillBridgeColors.BridgeBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )

                    if (!unlocked) {
                        Text(
                            text = item.remainingLabel.get(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isGuest) {
                            OutlinedButton(
                                onClick = {
                                    val target = when (def.id) {
                                        "reverse_eng_explorer" -> "ch_re_jwt_lab"
                                        else -> "ch_smart_city"
                                    }
                                    onProveNextStep(target)
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(strings.proveThisSkill)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = SkillBridgeColors.BridgeBlueDark)
                                Text(
                                    text = if (lang == AppLanguage.AR) "مشاركة الشارة في السجل العام" else "Show on Public Passport",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked = item.unlockedEntry?.isShareable ?: true,
                                onCheckedChange = {
                                    onToggleShareable(def.id, item.unlockedEntry?.isShareable ?: true)
                                }
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Account, Session, Verification Boundaries & Account Deletion Screen.
 */
@Composable
fun AccountSettingsScreen(
    displayName: String,
    email: String,
    isGuest: Boolean,
    roleMode: AppRoleMode,
    isOffline: Boolean,
    aiPersonalizationEnabled: Boolean,
    aiContextSharingEnabled: Boolean,
    onToggleAiPrefs: (personalization: Boolean, contextSharing: Boolean) -> Unit,
    onOpenPrivacyStates: () -> Unit,
    onSignInFromGuest: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var showSignOutConfirm by remember { mutableStateOf(false) }
    var deleteStep by remember { mutableStateOf(0) } // 0 = closed, 1 = first confirm, 2 = final confirm

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("account_settings_screen"),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = strings.accountSettingsTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = SkillBridgeColors.SoftCyanTint, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = SkillBridgeColors.DeepNavy)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isGuest) (if (lang == AppLanguage.AR) "وضع المعاينة للزائر" else "Guest Preview Session")
                                else email.ifBlank { "Google Account" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusPill(
                            text = if (isGuest) (if (lang == AppLanguage.AR) "زائر" else "Guest")
                            else (if (lang == AppLanguage.AR) "جلسة موثّقة" else "Verified Session"),
                            tone = if (isGuest) StatusTone.PENDING else StatusTone.VERIFIED
                        )
                    }

                    InfoNoteBanner(
                        text = if (isGuest) strings.guestLockedReason
                        else if (isOffline) strings.offlineBannerText
                        else if (lang == AppLanguage.AR) "حسابك متصل بـ Firebase Auth وFirestore وتُحفظ تفضيلاتك وإنجازاتك تلقائيًا."
                        else "Signed in via Google Credential Manager. Your profile, preferences, badges & ledger sync securely to Firestore."
                    )

                    if (isGuest) {
                        Button(
                            onClick = onSignInFromGuest,
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("account_guest_signin_btn")
                        ) {
                            Text(strings.signInGoogle)
                        }
                    }
                }
            }
        }

        // Role & Permission Boundaries
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark)
                        Text(
                            text = if (lang == AppLanguage.AR) "حدود الدور والصلاحيات" else "Role & Verification Boundaries",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (lang == AppLanguage.AR) {
                            "الدور النشط: ${roleMode.id}. النقاط والمستويات والشارات لا تُمنح إلا عبر تسليمات موثّقة وسجل Ledger غير قابل للتلاعب."
                        } else {
                            "Active Mode: ${roleMode.id}. Points, levels, and badges are computed strictly from verified submissions and immutable ledger records."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // AI Agent Privacy & Personalization Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark)
                        Text(
                            text = if (lang == AppLanguage.AR) "تفضيلات المساعد الذكي والخصوصية" else "AI Agent Privacy & Personalization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.aiAgentPersonalizationToggle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = aiPersonalizationEnabled,
                            onCheckedChange = { onToggleAiPrefs(it, aiContextSharingEnabled) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.aiAgentContextToggle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = aiContextSharingEnabled,
                            onCheckedChange = { onToggleAiPrefs(aiPersonalizationEnabled, it) }
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenPrivacyStates,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.privacySettingsTitle)
                    }
                }
            }
        }

        // Sign Out & Account Deletion
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DisputedBg.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, SkillBridgeColors.DisputedRed.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showSignOutConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("account_sign_out_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = SkillBridgeColors.DeepNavy)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isGuest) (if (lang == AppLanguage.AR) "إنهاء معاينة الزائر" else "Exit Guest Preview")
                            else strings.signOut,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (!isGuest) {
                        Button(
                            onClick = { deleteStep = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkillBridgeColors.DisputedRed,
                                contentColor = SkillBridgeColors.PureWhite
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("account_delete_btn")
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.deleteAccountAction, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(strings.signOut) },
            text = {
                Text(
                    if (lang == AppLanguage.AR) "هل تريد تسجيل الخروج؟ ستبقى إعدادات اللغة والمظهر محفوظة."
                    else "Sign out of SkillBridge? Your language and theme preferences will remain saved."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSignOutConfirm = false
                    onSignOut()
                }) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) { Text(strings.cancelLabel) }
            }
        )
    }

    if (deleteStep == 1) {
        AlertDialog(
            onDismissRequest = { deleteStep = 0 },
            title = { Text(strings.deleteAccountTitle) },
            text = { Text(strings.deleteAccountBody) },
            confirmButton = {
                Button(
                    onClick = { deleteStep = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DisputedRed)
                ) {
                    Text(if (lang == AppLanguage.AR) "متابعة للحذف النهائي" else "Continue to Final Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStep = 0 }) { Text(strings.cancelLabel) }
            }
        )
    }

    if (deleteStep == 2) {
        AlertDialog(
            onDismissRequest = { deleteStep = 0 },
            title = {
                Text(if (lang == AppLanguage.AR) "تأكيد أخير: حذف نهائي" else "Final Confirmation: Delete Everything")
            },
            text = {
                Text(
                    if (lang == AppLanguage.AR) "سيتم مسح بيانات حسابك من Firestore نهائيًا وتسجيل خروجك فورًا."
                    else "All your Firestore profile, goals, submissions, badges, and ledger documents will be permanently erased."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteStep = 0
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DisputedRed),
                    modifier = Modifier.testTag("confirm_delete_account_final_btn")
                ) {
                    Text(strings.deleteAccountAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStep = 0 }) { Text(strings.cancelLabel) }
            }
        )
    }
}
