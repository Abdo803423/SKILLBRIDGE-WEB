package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.core.AppLanguage
import com.example.core.AssetRegistry
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.core.localizedDomain
import com.example.core.onGoogleSignInClicked
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.DeviceFormFactor
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.SkillBridgeEmblem
import com.example.ui.components.SkillBridgeLogo
import com.example.ui.components.resolveDeviceFormFactor
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Brand-matched Login & Registration Screen following Phone #1 in SkillBridge Brand Showcase Poster.
 * Authenticates the 4 isolated role accounts (Platform Admin, Student, Company, Company Director)
 * plus Student Google Sign-In or new Student registration.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    onToggleLanguage: () -> Unit,
    onLoginWithCredentials: suspend (loginId: String, password: String) -> Result<Unit>,
    onRegisterStudent: suspend (loginId: String, displayName: String, email: String, password: String) -> Result<Unit>,
    onGoogleAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var isRegisterMode by remember { mutableStateOf(false) }
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SkillBridgeColors.SoftCyanTint,
                        SkillBridgeColors.OffWhiteCanvas,
                        SkillBridgeColors.PureWhite
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val formFactor = resolveDeviceFormFactor(maxWidth)
        val isDesktop = formFactor == DeviceFormFactor.EXPANDED_DESKTOP

        val authCardContent: @Composable () -> Unit = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SkillBridgeRadius.lg),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.PureWhite),
                border = BorderStroke(1.dp, SkillBridgeColors.BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRegisterMode) {
                                if (lang == AppLanguage.AR) "إنشاء حساب طالب جديد" else "Create Student Account"
                            } else {
                                if (lang == AppLanguage.AR) "تسجيل الدخول الآمن" else "Sign In to SkillBridge"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.DeepNavy
                        )
                        TextButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                authError = null
                            }
                        ) {
                            Text(
                                text = if (isRegisterMode) {
                                    if (lang == AppLanguage.AR) "لدي حساب" else "Back to Sign In"
                                } else {
                                    if (lang == AppLanguage.AR) "حساب طالب جديد" else "New Student?"
                                },
                                fontWeight = FontWeight.SemiBold,
                                color = SkillBridgeColors.BridgeBlueDark
                            )
                        }
                    }

                    if (!authError.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DisputedBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SkillBridgeColors.DisputedRed,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { loginId = it; authError = null },
                        label = { Text(if (lang == AppLanguage.AR) "معرّف الدخول (Login ID)" else "Login ID") },
                        placeholder = { Text("student_demo / company_demo / director_demo / admin_demo") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_id_input")
                    )

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text(if (lang == AppLanguage.AR) "الاسم الكامل" else "Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (lang == AppLanguage.AR) "البريد الإلكتروني" else "Email Address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; authError = null },
                        label = { Text(if (lang == AppLanguage.AR) "كلمة المرور" else "Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Button(
                        onClick = {
                            authError = null
                            isBusy = true
                            scope.launch {
                                val res = if (isRegisterMode) {
                                    onRegisterStudent(loginId, displayName, email, password)
                                } else {
                                    onLoginWithCredentials(loginId, password)
                                }
                                isBusy = false
                                res.onFailure { authError = it.message ?: "Authentication failed." }
                            }
                        },
                        enabled = !isBusy && loginId.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkillBridgeColors.DeepNavy,
                            contentColor = SkillBridgeColors.PureWhite
                        ),
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SkillBridgeColors.PureWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isRegisterMode) {
                                    if (lang == AppLanguage.AR) "إنشاء حساب والدخول" else "Create Account"
                                } else {
                                    if (lang == AppLanguage.AR) "تسجيل الدخول" else "Sign In"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = if (lang == AppLanguage.AR) "أو" else "OR",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkillBridgeColors.InkMuted
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    OutlinedButton(
                        onClick = {
                            authError = null
                            isBusy = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isBusy = false
                                    onGoogleAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isBusy = false
                                    authError = err
                                },
                                onAuthCancelled = { isBusy = false },
                                scope = scope
                            )
                        },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("welcome_sign_in_button")
                    ) {
                        Text(
                            text = strings.signInGoogle,
                            color = SkillBridgeColors.DeepNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        val demoRolesCardContent: @Composable () -> Unit = {
            if (!isRegisterMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SkillBridgeRadius.md),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.PureWhite),
                    border = BorderStroke(1.dp, SkillBridgeColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (lang == AppLanguage.AR) "حسابات العرض التجريبية (تُوجَّه تلقائيًا للوحة دورها)"
                            else "Demo Role Accounts (Auto-routed by backend credentials)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.DeepNavy
                        )
                        Text(
                            text = if (lang == AppLanguage.AR)
                                "كل حساب يملك جلسة مستقلة وصلاحيات خاصة من الخادم دون تبديل أدوار داخل التطبيق."
                            else
                                "Each account has its own salted credentials and backend-enforced dashboard. Tap to autofill:",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkillBridgeColors.InkSecondary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DemoFillButton(
                                label = "Student (student_demo)",
                                icon = Icons.Filled.School,
                                tag = "demo_fill_student"
                            ) {
                                loginId = "student_demo"
                                password = "Student@123"
                                authError = null
                            }
                            DemoFillButton(
                                label = "Company (company_demo)",
                                icon = Icons.Filled.Business,
                                tag = "demo_fill_company"
                            ) {
                                loginId = "company_demo"
                                password = "Company@123"
                                authError = null
                            }
                            DemoFillButton(
                                label = "Director (director_demo)",
                                icon = Icons.Filled.SupervisorAccount,
                                tag = "demo_fill_director"
                            ) {
                                loginId = "director_demo"
                                password = "Director@123"
                                authError = null
                            }
                            DemoFillButton(
                                label = "Admin (admin_demo)",
                                icon = Icons.Filled.AdminPanelSettings,
                                tag = "demo_fill_admin"
                            ) {
                                loginId = "admin_demo"
                                password = "Admin@123"
                                authError = null
                            }
                        }
                    }
                }
            }
        }

        if (isDesktop) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .widthIn(max = 1240.dp)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Desktop Showcase Panel
                Surface(
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxHeight()
                        .testTag("desktop_welcome_showcase"),
                    shape = RoundedCornerShape(SkillBridgeRadius.xl),
                    color = SkillBridgeColors.DeepNavy
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = AssetRegistry.HERO_BRIDGE_BANNER),
                            contentDescription = "SkillBridge career bridge visual",
                            contentScale = ContentScale.Crop,
                            alpha = 0.25f,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SkillBridgeLogo(darkBackground = true, compactOnly = false, showTagline = true)
                                Surface(
                                    shape = RoundedCornerShape(SkillBridgeRadius.pill),
                                    color = SkillBridgeColors.BridgeBlue.copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, SkillBridgeColors.SoftCyan.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.AR) "نسخة سطح المكتب واللابتوب" else "Desktop & Mobile Unified",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SkillBridgeColors.SoftCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = if (lang == AppLanguage.AR)
                                        "منصة واحدة — تجربتان متزامنتان للجوال وسطح المكتب"
                                    else
                                        "One Unified Platform.\nSynced Across Mobile & Laptop.",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillBridgeColors.PureWhite
                                )
                                Text(
                                    text = if (lang == AppLanguage.AR)
                                        "اعمل على نفس الحسابات وقاعدة البيانات المركزية عبر 4 بوابات معزولة الصلاحيات."
                                    else
                                        "Authenticate once into the shared SkillBridge central database across Student, Company, Director, and Platform Admin workspaces.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SkillBridgeColors.SoftCyan
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(SkillBridgeRadius.md),
                                    color = SkillBridgeColors.PureWhite.copy(alpha = 0.09f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Devices, contentDescription = null, tint = SkillBridgeColors.LimeGreen)
                                        Column {
                                            Text(
                                                text = if (lang == AppLanguage.AR) "مزامنة لحظية بين الأجهزة" else "Real-Time Cross-Device Sync",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = SkillBridgeColors.PureWhite
                                            )
                                            Text(
                                                text = if (lang == AppLanguage.AR)
                                                    "قدّم من الهاتف، قيّم من اللابتوب، وحدّث جواز الخبرة فورًا."
                                                else
                                                    "Apply on Mobile, evaluate on Desktop, and update your Experience Passport instantly.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SkillBridgeColors.SoftCyan.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(SkillBridgeRadius.md),
                                    color = SkillBridgeColors.PureWhite.copy(alpha = 0.09f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Verified, contentDescription = null, tint = SkillBridgeColors.SoftCyan)
                                        Column {
                                            Text(
                                                text = if (lang == AppLanguage.AR) "4 بوابات بصلاحيات صارمة" else "4 Role-Isolated Workspaces",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = SkillBridgeColors.PureWhite
                                            )
                                            Text(
                                                text = if (lang == AppLanguage.AR)
                                                    "الطالب • الشركة • المشرف • مدير المنصة"
                                                else
                                                    "Student • Company • Company Director • Platform Admin",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SkillBridgeColors.SoftCyan.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Desktop Auth Column
                Column(
                    modifier = Modifier
                        .weight(0.95f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onToggleLanguage,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("welcome_language_toggle")
                        ) {
                            Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.languageToggleLabel)
                        }
                    }
                    authCardContent()
                    demoRolesCardContent()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .widthIn(max = 560.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkillBridgeLogo(darkBackground = false, compactOnly = false, showTagline = true)
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("welcome_language_toggle")
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.languageToggleLabel)
                    }
                }

                // Brand showcase header matching Poster Phone #1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .clip(RoundedCornerShape(SkillBridgeRadius.lg))
                ) {
                    Image(
                        painter = painterResource(id = AssetRegistry.HERO_BRIDGE_BANNER),
                        contentDescription = "SkillBridge career bridge visual",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        SkillBridgeColors.DeepNavy.copy(alpha = 0.25f),
                                        SkillBridgeColors.DeepNavy.copy(alpha = 0.86f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SkillBridgeEmblem(size = 48.dp, darkBackground = true)
                        Column {
                            Text(
                                text = strings.welcomeTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgeColors.PureWhite
                            )
                            Text(
                                text = if (lang == AppLanguage.AR) "حوّل مهاراتك إلى خبرة موثّقة وفرص حقيقية"
                                else "Bridge your skills to greater opportunities",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SkillBridgeColors.SoftCyan
                            )
                        }
                    }
                }

                authCardContent()
                demoRolesCardContent()
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DemoFillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(SkillBridgeRadius.sm),
        color = SkillBridgeColors.OffWhiteCanvas,
        border = BorderStroke(1.dp, SkillBridgeColors.BorderLight),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = SkillBridgeColors.DeepNavy
            )
        }
    }
}

/**
 * Student Profile Setup Wizard (no role-switching allowed).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWizardScreen(
    initialName: String,
    onComplete: (
        displayName: String,
        country: String,
        timezone: String,
        domain: String,
        careerLevel: String,
        currentSkills: List<String>,
        targetSkills: List<String>,
        goalsSummary: String,
        weeklyHours: Int
    ) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = LocalAppLanguage.current

    var displayName by remember { mutableStateOf(initialName) }
    var country by remember { mutableStateOf("Egypt") }
    var timezone by remember { mutableStateOf("UTC+2") }
    var selectedDomain by remember { mutableStateOf(SkillBridgeEcosystemData.domains.first()) }
    var currentSkillsInput by remember { mutableStateOf("Kotlin, SQL, REST APIs") }
    var targetSkillsInput by remember { mutableStateOf("Distributed Systems, Compose Performance, Security Audits") }
    var goalsSummary by remember { mutableStateOf("Build verified backend & data evidence through company project briefs.") }
    var weeklyHours by remember { mutableFloatStateOf(10f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (lang == AppLanguage.AR) "تحديث ملفك المهني" else "Student Profile Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        InfoNoteBanner(
            text = if (lang == AppLanguage.AR) {
                "يتم ترقية مستواك المهني تلقائيًا بناءً على التسليمات الموثّقة من المشرفين."
            } else {
                "Your verified Career Level updates automatically when Directors approve your practical deliverables."
            }
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text(if (lang == AppLanguage.AR) "الاسم المعروض" else "Display Name") },
            modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text(if (lang == AppLanguage.AR) "الدولة" else "Country") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = timezone,
                onValueChange = { timezone = it },
                label = { Text(if (lang == AppLanguage.AR) "التوقيت" else "Timezone") },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = if (lang == AppLanguage.AR) "المجال الأساسي" else "Primary Domain",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkillBridgeEcosystemData.domains.forEach { dom ->
                FilterChip(
                    selected = selectedDomain == dom,
                    onClick = { selectedDomain = dom },
                    label = { Text(localizedDomain(dom, lang)) }
                )
            }
        }

        OutlinedTextField(
            value = currentSkillsInput,
            onValueChange = { currentSkillsInput = it },
            label = { Text(if (lang == AppLanguage.AR) "المهارات الحالية (مفصولة بفاصلة)" else "Current Skills (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = targetSkillsInput,
            onValueChange = { targetSkillsInput = it },
            label = { Text(if (lang == AppLanguage.AR) "المهارات المستهدفة" else "Target Skills") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = goalsSummary,
            onValueChange = { goalsSummary = it },
            label = { Text(if (lang == AppLanguage.AR) "هدفك المهني" else "90-Day Career Goal") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = (if (lang == AppLanguage.AR) "الساعات الأسبوعية المتاحة: " else "Weekly Practice Hours: ") + "${weeklyHours.roundToInt()}h",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(value = weeklyHours, onValueChange = { weeklyHours = it }, valueRange = 2f..30f)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(48.dp)) {
                Text(if (lang == AppLanguage.AR) "عودة" else "Back")
            }
            Button(
                onClick = {
                    val cur = currentSkillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val tgt = targetSkillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onComplete(displayName.ifBlank { initialName }, country, timezone, selectedDomain, "Junior", cur, tgt, goalsSummary, weeklyHours.roundToInt())
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                modifier = Modifier.weight(2f).height(48.dp).testTag("onboarding_save_btn")
            ) {
                Text(if (lang == AppLanguage.AR) "حفظ التغييرات" else "Save Profile")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
