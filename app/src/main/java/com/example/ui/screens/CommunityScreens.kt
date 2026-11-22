package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.core.localizedPostType
import com.example.data.model.AiAgentContextMode
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.ContextualAiAgentBanner
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.UiState

/**
 * Screen 9: Reddit-inspired Community Feed.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    postsState: UiState<List<CommunityPost>>,
    upvotedPostIds: Set<String>,
    savedPostIds: Set<String>,
    followedTags: Set<String>,
    mutedAuthors: Set<String>,
    onToggleUpvote: (CommunityPost) -> Unit,
    onToggleSavePost: (String) -> Unit,
    onToggleFollowTag: (String) -> Unit,
    onMuteAuthor: (String) -> Unit,
    onCreatePost: (postType: String, title: String, body: String, tags: List<String>) -> Unit,
    onReportPost: (postId: String, reason: String) -> Unit,
    onOpenAiAgent: (AiAgentContextMode, String, String) -> Unit,
    onOpenPost: (CommunityPost) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var sortMode by remember { mutableStateOf("Trending") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var savedOnly by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var reportTargetId by remember { mutableStateOf<String?>(null) }

    val livePosts = (postsState as? UiState.Success)?.data.orEmpty()
    val mergedPosts = remember(livePosts, mutedAuthors, sortMode, selectedTag, savedOnly, savedPostIds) {
        val base = (livePosts + SkillBridgeEcosystemData.seedCommunityPosts)
            .distinctBy { it.id }
            .filter { it.authorName !in mutedAuthors }
            .filter { selectedTag == null || selectedTag in it.tags }
            .filter { !savedOnly || it.id in savedPostIds }
        when (sortMode) {
            "Newest" -> base.sortedByDescending { it.createdAt?.seconds ?: 0L }
            "Top" -> base.sortedByDescending { it.helpfulCount }
            "Unanswered" -> base.filter { it.acceptedCommentId.isBlank() }
            else -> base.sortedByDescending { it.helpfulCount + it.commentCount * 2L }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = SkillBridgeColors.DeepNavy,
                contentColor = SkillBridgeColors.PureWhite,
                modifier = Modifier.testTag("community_new_post_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.newPost)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(strings.communityTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = sortMode == "Trending", onClick = { sortMode = "Trending" }, label = { Text(strings.sortTrending) })
                    FilterChip(selected = sortMode == "Newest", onClick = { sortMode = "Newest" }, label = { Text(strings.sortNew) })
                    FilterChip(selected = sortMode == "Top", onClick = { sortMode = "Top" }, label = { Text(strings.sortTop) })
                    FilterChip(selected = sortMode == "Unanswered", onClick = { sortMode = "Unanswered" }, label = { Text(strings.sortUnanswered) })
                    FilterChip(selected = savedOnly, onClick = { savedOnly = !savedOnly }, label = { Text(strings.savedLabel) })
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Kotlin", "PostgreSQL", "ReverseEng", "Firebase", "DataScience", "TeamUp").forEach { tag ->
                        val isFollowed = tag in followedTags
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = if (selectedTag == tag) null else tag },
                            label = { Text("#$tag" + if (isFollowed) " ★" else "") }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                ContextualAiAgentBanner(
                    mode = AiAgentContextMode.COMMUNITY_COACH,
                    onOpenAgent = {
                        onOpenAiAgent(
                            AiAgentContextMode.COMMUNITY_COACH,
                            strings.communityTitle,
                            "VisiblePosts=${mergedPosts.size}, FilterTag=${selectedTag ?: "All"}"
                        )
                    }
                )
            }

            items(mergedPosts, key = { it.id }) { post ->
                val isUpvoted = post.id in upvotedPostIds
                val isSaved = post.id in savedPostIds
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPost(post) }
                        .testTag("community_post_${post.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                Text(post.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                StatusPill(
                                    text = if (post.authorVerified) strings.verifiedBadge else strings.selfClaimedBadge,
                                    tone = if (post.authorVerified) StatusTone.VERIFIED else StatusTone.NEUTRAL,
                                    icon = if (post.authorVerified) Icons.Filled.Verified else null
                                )
                            }
                            StatusPill(localizedPostType(post.postType, lang), StatusTone.REVIEW)
                        }
                        if (post.id.startsWith("seed_")) {
                            StatusPill(strings.demoCatalogLabel, StatusTone.NEUTRAL)
                        }
                        Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(post.body, style = MaterialTheme.typography.bodyMedium, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            post.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(SkillBridgeRadius.pill),
                                    color = SkillBridgeColors.SoftCyanTint,
                                    modifier = Modifier.clickable { onToggleFollowTag(tag) }
                                ) {
                                    Text("#$tag", style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.DeepNavy, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                            if (post.acceptedCommentId.isNotBlank()) {
                                StatusPill(strings.bestAnswer, StatusTone.VERIFIED, Icons.Filled.CheckCircle)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onToggleUpvote(post) }) {
                                    Icon(
                                        Icons.Filled.ThumbUp,
                                        contentDescription = "Helpful",
                                        tint = if (isUpvoted) SkillBridgeColors.BridgeBlueDark else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("${post.helpfulCount + if (isUpvoted && post.id.startsWith("seed_")) 1 else 0}", style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comments", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${post.commentCount}", style = MaterialTheme.typography.labelLarge)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onToggleSavePost(post.id) }) {
                                    Icon(if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, contentDescription = strings.saveForLater)
                                }
                                IconButton(onClick = { reportTargetId = post.id }) {
                                    Icon(Icons.Filled.Flag, contentDescription = strings.reportContent, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { onMuteAuthor(post.authorName) }) {
                                    Text(if (lang == AppLanguage.AR) "كتم" else "Mute", style = MaterialTheme.typography.labelSmall)
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
        CreatePostSheet(
            onDismiss = { showCreateSheet = false },
            onPublish = { type, title, body, tags ->
                onCreatePost(type, title, body, tags)
                showCreateSheet = false
            }
        )
    }

    reportTargetId?.let { id ->
        var reason by remember { mutableStateOf("Exposed credential / off-scope content") }
        AlertDialog(
            onDismissRequest = { reportTargetId = null },
            title = { Text(strings.reportContent) },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (lang == AppLanguage.AR) "سبب البلاغ" else "Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onReportPost(id, reason)
                    reportTargetId = null
                }) { Text(strings.reportContent) }
            },
            dismissButton = {
                TextButton(onClick = { reportTargetId = null }) { Text(if (lang == AppLanguage.AR) "إلغاء" else "Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreatePostSheet(
    onDismiss: () -> Unit,
    onPublish: (String, String, String, List<String>) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val postTypes = listOf("Technical Question", "Help Request", "Tutorial", "Team Ad", "Code Review", "Post-Contest Solution", "Suggest Challenge")
    var selectedType by remember { mutableStateOf(postTypes.first()) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("Kotlin, Architecture") }
    var showConfirmPublish by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(if (lang == AppLanguage.AR) "نشر سؤال أو مشاركة" else "Create Community Post", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                postTypes.forEach { t ->
                    FilterChip(selected = selectedType == t, onClick = { selectedType = t }, label = { Text(localizedPostType(t, lang)) })
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (lang == AppLanguage.AR) "العنوان" else "Title") },
                modifier = Modifier.fillMaxWidth().testTag("new_post_title_input")
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                minLines = 3,
                label = { Text(if (lang == AppLanguage.AR) "التفاصيل أو المشكلة" else "Details / Context") },
                modifier = Modifier.fillMaxWidth().testTag("new_post_body_input")
            )
            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                label = { Text(if (lang == AppLanguage.AR) "الوسوم" else "Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { showConfirmPublish = true },
                enabled = title.trim().length >= 3 && body.trim().length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_post_btn")
            ) {
                Text(if (lang == AppLanguage.AR) "نشر الآن" else "Publish Post")
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showConfirmPublish) {
        AlertDialog(
            onDismissRequest = { showConfirmPublish = false },
            title = { Text(if (lang == AppLanguage.AR) "تأكيد نشر المشاركة" else "Confirm Community Post") },
            text = {
                Text(
                    if (lang == AppLanguage.AR) "سيظهر منشورك لجميع أعضاء المجتمع. تأكد من خلوه من أي مفاتيح سرية."
                    else "Your post will be visible to the SkillBridge community. Make sure no credentials are included."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmPublish = false
                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onPublish(selectedType, title, body, tags)
                }) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmPublish = false }) { Text(strings.cancelLabel) }
            }
        )
    }
}

/**
 * Screen 10: Post Details with Nested Comments & Best Answer.
 */
@Composable
fun PostDetailScreen(
    post: CommunityPost,
    currentUserId: String,
    commentsState: UiState<List<CommunityComment>>,
    onAddComment: (parentCommentId: String, body: String) -> Unit,
    onAcceptBestAnswer: (commentId: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var replyText by remember { mutableStateOf("") }
    var replyingToId by remember { mutableStateOf("") }

    val comments = (commentsState as? UiState.Success)?.data.orEmpty()
    val topLevel = comments.filter { it.parentCommentId.isBlank() }
    val childrenByParent = comments.filter { it.parentCommentId.isNotBlank() }.groupBy { it.parentCommentId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(localizedPostType(post.postType, lang), StatusTone.REVIEW)
                        StatusPill(localizedCareerLevel(post.authorLevel, lang), StatusTone.NEUTRAL)
                        if (post.authorVerified) StatusPill(strings.verifiedBadge, StatusTone.VERIFIED)
                    }
                    Text(post.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(post.body, style = MaterialTheme.typography.bodyLarge)
                    Text("${post.authorName} • ${post.helpfulCount} ${strings.helpfulLabel}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (replyingToId.isNotBlank()) {
                        Text(
                            if (lang == AppLanguage.AR) "الرد متداخل على تعليق محدد" else "Replying nested to comment",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkillBridgeColors.BridgeBlueDark
                        )
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text(if (lang == AppLanguage.AR) "أضف إجابة أو تعليقًا..." else "Write a helpful reply...") },
                        modifier = Modifier.fillMaxWidth().testTag("post_reply_input")
                    )
                    Button(
                        onClick = {
                            onAddComment(replyingToId, replyText)
                            replyText = ""
                            replyingToId = ""
                        },
                        enabled = replyText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("post_reply_submit")
                    ) {
                        Text(if (lang == AppLanguage.AR) "إرسال الرد" else "Post Reply")
                    }
                }
            }
        }

        items(topLevel, key = { it.id }) { parent ->
            CommentCard(
                comment = parent,
                isNested = false,
                canAcceptBest = post.authorId == currentUserId && !parent.isBestAnswer,
                onReply = { replyingToId = parent.id },
                onAcceptBest = { onAcceptBestAnswer(parent.id) }
            )
            childrenByParent[parent.id].orEmpty().forEach { child ->
                Spacer(Modifier.height(6.dp))
                CommentCard(
                    comment = child,
                    isNested = true,
                    canAcceptBest = post.authorId == currentUserId && !child.isBestAnswer,
                    onReply = { replyingToId = parent.id },
                    onAcceptBest = { onAcceptBestAnswer(child.id) }
                )
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun CommentCard(
    comment: CommunityComment,
    isNested: Boolean,
    canAcceptBest: Boolean,
    onReply: () -> Unit,
    onAcceptBest: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var showBestConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isNested) 24.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (comment.isBestAnswer) SkillBridgeColors.VerifiedBg.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (comment.isBestAnswer) SkillBridgeColors.VerifiedGreen else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    StatusPill(localizedCareerLevel(comment.authorLevel, lang), StatusTone.NEUTRAL)
                    if (comment.isBestAnswer) StatusPill(strings.bestAnswer, StatusTone.VERIFIED, Icons.Filled.CheckCircle)
                }
            }
            Text(comment.body, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onReply) { Text(strings.replyLabel) }
                if (canAcceptBest) {
                    OutlinedButton(onClick = { showBestConfirm = true }) { Text(strings.acceptAnswer) }
                }
            }
        }
    }

    if (showBestConfirm) {
        AlertDialog(
            onDismissRequest = { showBestConfirm = false },
            title = { Text(strings.acceptAnswer) },
            text = {
                Text(
                    if (lang == AppLanguage.AR) "تعيين رد ${comment.authorName} كأفضل إجابة؟"
                    else "Mark ${comment.authorName}'s reply as the accepted Best Answer?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showBestConfirm = false
                    onAcceptBest()
                }) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showBestConfirm = false }) { Text(strings.cancelLabel) }
            }
        )
    }
}
