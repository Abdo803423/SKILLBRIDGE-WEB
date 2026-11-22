package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.core.localizedDomain
import com.example.core.localizedTaskStatus
import com.example.data.model.AiAgentContextMode
import com.example.data.model.Team
import com.example.data.model.TeamTask
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.ContextualAiAgentBanner
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.UiState

/**
 * Screen 11: Teams Hub (My Teams + Recruiting Teams + Create Team).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamsHubScreen(
    currentUserId: String,
    myTeamsState: UiState<List<Team>>,
    recruitingTeamsState: UiState<List<Team>>,
    onCreateTeam: (name: String, desc: String, domain: String, level: String, maxMembers: Int, skills: List<String>, roles: List<String>, challenge: String) -> Unit,
    onJoinTeam: (Team) -> Unit,
    onOpenTeamWorkspace: (Team) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var teamPendingJoin by remember { mutableStateOf<Team?>(null) }

    val myLiveTeams = (myTeamsState as? UiState.Success)?.data.orEmpty()
    val recruitingLive = (recruitingTeamsState as? UiState.Success)?.data.orEmpty()

    val displayedTeams = if (selectedTab == 0) {
        (myLiveTeams + SkillBridgeEcosystemData.seedTeams.take(1)).distinctBy { it.id }
    } else {
        (recruitingLive + SkillBridgeEcosystemData.seedTeams).distinctBy { it.id }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = SkillBridgeColors.DeepNavy,
                contentColor = SkillBridgeColors.PureWhite,
                modifier = Modifier.testTag("create_team_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.createTeam)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(strings.teamsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(strings.myTeams) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(strings.recruitingTeams) })
                }
            }

            items(displayedTeams, key = { it.id }) { team ->
                val isMember = currentUserId in team.memberIds || team.id == "seed_team_1"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTeamWorkspace(team) }
                        .testTag("team_card_${team.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(team.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            StatusPill("${team.memberIds.size}/${team.maxMembers} ${strings.membersLabel}", StatusTone.REVIEW, Icons.Filled.Group)
                        }
                        if (team.id.startsWith("seed_")) {
                            StatusPill(strings.demoCatalogLabel, StatusTone.NEUTRAL)
                        }
                        Text("${localizedDomain(team.domain, lang)} • ${localizedCareerLevel(team.level, lang)}", style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.BridgeBlueDark)
                        if (team.challengeTitle.isNotBlank()) {
                            Text(
                                "${if (lang == AppLanguage.AR) "التحدي:" else "Challenge:"} ${team.challengeTitle}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkillBridgeColors.PaidTeal
                            )
                        }
                        Text(team.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            team.rolesNeeded.forEach { role ->
                                StatusPill("${strings.needRolePrefix} $role", StatusTone.PENDING)
                            }
                            team.requiredSkills.forEach { sk ->
                                Surface(shape = RoundedCornerShape(SkillBridgeRadius.pill), color = SkillBridgeColors.SoftCyanTint) {
                                    Text(sk, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { onOpenTeamWorkspace(team) },
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text(strings.sprintBoard)
                            }
                            if (!isMember) {
                                Button(
                                    onClick = { teamPendingJoin = team },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("join_team_${team.id}")
                                ) {
                                    Text(strings.joinTeam)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showCreateSheet) {
        CreateTeamSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, desc, dom, lvl, max, sk, roles, ch ->
                onCreateTeam(name, desc, dom, lvl, max, sk, roles, ch)
                showCreateSheet = false
            }
        )
    }

    teamPendingJoin?.let { pending ->
        AlertDialog(
            onDismissRequest = { teamPendingJoin = null },
            title = { Text(strings.joinTeam) },
            text = {
                Text(
                    if (lang == AppLanguage.AR) "هل تريد الانضمام إلى فريق ${pending.name} للمشاركة في ${pending.challengeTitle}؟"
                    else "Join ${pending.name} to collaborate on ${pending.challengeTitle}?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    val target = pending
                    teamPendingJoin = null
                    onJoinTeam(target)
                }) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { teamPendingJoin = null }) { Text(strings.cancelLabel) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateTeamSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, Int, List<String>, List<String>, String) -> Unit
) {
    val lang = LocalAppLanguage.current
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf(SkillBridgeEcosystemData.domains.first()) }
    var level by remember { mutableStateOf("Mid-level") }
    var rolesText by remember { mutableStateOf("Backend, QA, Presentation") }
    var skillsText by remember { mutableStateOf("Kotlin, SQL, Testing") }
    var challengeTitle by remember { mutableStateOf("Smart City Analytics Dashboard") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(if (lang == AppLanguage.AR) "إنشاء فريق جديد" else "Create SkillBridge Team", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (lang == AppLanguage.AR) "اسم الفريق" else "Team Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text(if (lang == AppLanguage.AR) "وصف المشروع وإعلان الأعضاء" else "Mission & Teammate Ad") }, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SkillBridgeEcosystemData.domains.take(4).forEach { dom ->
                    FilterChip(selected = domain == dom, onClick = { domain = dom }, label = { Text(localizedDomain(dom, lang)) })
                }
            }
            OutlinedTextField(value = rolesText, onValueChange = { rolesText = it }, label = { Text(if (lang == AppLanguage.AR) "الأدوار المطلوبة" else "Roles Needed") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = skillsText, onValueChange = { skillsText = it }, label = { Text(if (lang == AppLanguage.AR) "المهارات المطلوبة" else "Required Skills") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = challengeTitle, onValueChange = { challengeTitle = it }, label = { Text(if (lang == AppLanguage.AR) "التحدي / المسابقة المستهدفة" else "Target Challenge / Contest") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    onCreate(
                        name,
                        desc,
                        domain,
                        level,
                        4,
                        skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        rolesText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        challengeTitle
                    )
                },
                enabled = name.trim().length >= 2 && desc.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (lang == AppLanguage.AR) "إنشاء الفريق" else "Create Team")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Screen 12: Team Workspace (Members, Roles, Sprint Tasks, Points Split, Team Feed).
 */
@Composable
fun TeamWorkspaceScreen(
    team: Team,
    tasksState: UiState<List<TeamTask>>,
    onCreateTask: (title: String, assignee: String, role: String) -> Unit,
    onAdvanceTask: (TeamTask) -> Unit,
    onOpenAiAgent: (Team, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val tasks = (tasksState as? UiState.Success)?.data.orEmpty()
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskRole by remember { mutableStateOf("Backend") }

    val doneCount = tasks.count { it.status == "DONE" }
    val progress = if (tasks.isEmpty()) (team.sprintProgress / 100f) else doneCount.toFloat() / tasks.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(team.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                    Text("${localizedDomain(team.domain, lang)} • ${team.challengeTitle}", style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.SoftCyan)
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)),
                        color = SkillBridgeColors.LimeGreen,
                        trackColor = SkillBridgeColors.PureWhite.copy(alpha = 0.2f)
                    )
                    Text(
                        if (lang == AppLanguage.AR) "نسبة إنجاز السباق: ${(progress * 100).toInt()}%"
                        else "${(progress * 100).toInt()}% Sprint Completion",
                        style = MaterialTheme.typography.labelMedium,
                        color = SkillBridgeColors.LimeGreen
                    )
                }
            }
        }

        item {
            ContextualAiAgentBanner(
                mode = AiAgentContextMode.TEAM_SPRINT_PLANNER,
                onOpenAgent = {
                    onOpenAiAgent(
                        team,
                        "Team=${team.name}, Challenge=${team.challengeTitle}, Tasks=${tasks.size}, Done=$doneCount"
                    )
                }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (lang == AppLanguage.AR) "أعضاء الفريق والأدوار" else "Members & Verified Roles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    team.memberNames.forEach { m ->
                        Text("• $m", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    InfoNoteBanner(
                        text = "${strings.rewardSplitTitle}: " +
                            if (lang == AppLanguage.AR) "تُوزع المكافآت بالتساوي أو حسب نسبة المهام الموثّقة (${100 / team.memberNames.size.coerceAtLeast(1)}٪ مبدئيًا لكل عضو) مع حماية كل مساهمة في سجل الخبرة."
                            else "Points & prize pool split transparently (${100 / team.memberNames.size.coerceAtLeast(1)}% baseline per active contributor) based on verified sprint deliverables."
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.sprintBoard, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text(if (lang == AppLanguage.AR) "مهمة جديدة" else "New sprint task") },
                            modifier = Modifier.weight(2f)
                        )
                        OutlinedTextField(
                            value = newTaskRole,
                            onValueChange = { newTaskRole = it },
                            label = { Text(if (lang == AppLanguage.AR) "الدور" else "Role") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            onCreateTask(newTaskTitle, team.captainName, newTaskRole)
                            newTaskTitle = ""
                        },
                        enabled = newTaskTitle.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (lang == AppLanguage.AR) "إضافة للوحة المهام" else "Add Sprint Task")
                    }
                }
            }
        }

        items(tasks, key = { it.id }) { task ->
            val tone = when (task.status) {
                "DONE" -> StatusTone.VERIFIED
                "IN_PROGRESS" -> StatusTone.REVIEW
                else -> StatusTone.PENDING
            }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onAdvanceTask(task) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${task.assigneeName} • ${task.role}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusPill(localizedTaskStatus(task.status, lang), tone, Icons.Filled.TaskAlt)
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
