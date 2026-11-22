package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AccountRole(val key: String) {
    PLATFORM_ADMIN("PLATFORM_ADMIN"),
    STUDENT("STUDENT"),
    COMPANY("COMPANY"),
    COMPANY_DIRECTOR("COMPANY_DIRECTOR");

    companion object {
        fun fromKey(raw: String): AccountRole =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: STUDENT
    }
}

data class SessionPrincipal(
    val accountId: String,
    val loginId: String,
    val displayName: String,
    val email: String,
    val role: AccountRole,
    val companyId: String = "",
    val isGoogleLinked: Boolean = false
)

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["loginId"], unique = true),
        Index(value = ["role"]),
        Index(value = ["companyId"])
    ]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val loginId: String,
    val displayName: String,
    val email: String,
    val passwordSalt: String,
    val passwordHash: String,
    val role: String,
    val companyId: String = "",
    val isActive: Boolean = true,
    val careerLevel: String = "Junior",
    val domain: String = "Data & AI",
    val country: String = "Egypt",
    val points: Int = 0,
    val streakDays: Int = 0,
    val verifiedCount: Int = 0,
    val skillsCsv: String = "",
    val targetSkillsCsv: String = "",
    val goalsSummary: String = "",
    val weeklyHours: Int = 10,
    val profileCompletion: Int = 85,
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "companies",
    indices = [
        Index(value = ["ownerAccountId"]),
        Index(value = ["verificationStatus"])
    ]
)
data class CompanyEntity(
    @PrimaryKey val id: String,
    val ownerAccountId: String,
    val name: String,
    val city: String,
    val domain: String,
    val verificationStatus: String, // PENDING, VERIFIED, REJECTED
    val verificationTier: String,   // UNVERIFIED, BASIC, BUSINESS, TRUSTED
    val verificationNotes: String,
    val rewardPolicy: String,
    val ipPolicy: String,
    val verifiedAtEpochMs: Long = 0L,
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "project_briefs",
    foreignKeys = [
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["assignedDirectorId"]),
        Index(value = ["approvalStatus"])
    ]
)
data class ProjectBriefEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val companyName: String,
    val createdByAccountId: String,
    val assignedDirectorId: String = "",
    val assignedDirectorName: String = "",
    val title: String,
    val summary: String,
    val domain: String,
    val level: String,
    val skillsCsv: String,
    val rubricCsv: String,
    val deliverablesCsv: String,
    val deadline: String,
    val pointsReward: Int,
    val prizePool: String,
    val approvalStatus: String, // PENDING_APPROVAL, APPROVED, ACTIVE, COMPLETED, REJECTED
    val adminNotes: String = "",
    val isTeamAllowed: Boolean = true,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "project_applications",
    foreignKeys = [
        ForeignKey(
            entity = ProjectBriefEntity::class,
            parentColumns = ["id"],
            childColumns = ["briefId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["briefId"]),
        Index(value = ["studentAccountId"]),
        Index(value = ["status"]),
        Index(value = ["briefId", "studentAccountId"], unique = true)
    ]
)
data class ProjectApplicationEntity(
    @PrimaryKey val id: String,
    val briefId: String,
    val studentAccountId: String,
    val studentName: String,
    val studentLevel: String,
    val studentSkillsCsv: String,
    val coverNote: String,
    val roleRequested: String,
    val status: String, // APPLIED, SHORTLISTED, ACCEPTED, REJECTED
    val decisionNote: String = "",
    val appliedAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "project_tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectBriefEntity::class,
            parentColumns = ["id"],
            childColumns = ["briefId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["briefId"]),
        Index(value = ["assignedStudentId"]),
        Index(value = ["assignedDirectorId"])
    ]
)
data class ProjectTaskEntity(
    @PrimaryKey val id: String,
    val briefId: String,
    val assignedStudentId: String,
    val assignedStudentName: String,
    val assignedDirectorId: String,
    val title: String,
    val milestoneOrder: Int,
    val dueDateLabel: String,
    val isOverdue: Boolean = false,
    val status: String, // TODO, IN_PROGRESS, DONE
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "work_submissions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectBriefEntity::class,
            parentColumns = ["id"],
            childColumns = ["briefId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["briefId"]),
        Index(value = ["studentAccountId"]),
        Index(value = ["directorAccountId"]),
        Index(value = ["status"])
    ]
)
data class WorkSubmissionEntity(
    @PrimaryKey val id: String,
    val briefId: String,
    val briefTitle: String,
    val companyName: String,
    val studentAccountId: String,
    val studentName: String,
    val directorAccountId: String,
    val repoOrDemoUrl: String,
    val understandingReport: String,
    val skillsAppliedCsv: String,
    val status: String, // PENDING_REVIEW, VERIFIED, REVISION_REQUESTED, REJECTED
    val score: Int = 0,
    val pointsAwarded: Int = 0,
    val feedback: String = "",
    val rejectionReason: String = "",
    val verificationCode: String = "",
    val isPublicOnPassport: Boolean = true,
    val submittedAtEpochMs: Long,
    val evaluatedAtEpochMs: Long = 0L
)

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["recipientAccountId"]),
        Index(value = ["isRead"])
    ]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientAccountId: String,
    val category: String, // APPROVAL, APPLICATION, EVALUATION, TASK, SYSTEM
    val titleEn: String,
    val titleAr: String,
    val bodyEn: String,
    val bodyAr: String,
    val isRead: Boolean = false,
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "platform_activities",
    indices = [
        Index(value = ["actorAccountId"]),
        Index(value = ["createdAtEpochMs"])
    ]
)
data class PlatformActivityEntity(
    @PrimaryKey val id: String,
    val actorAccountId: String,
    val actorRole: String,
    val actionType: String,
    val summary: String,
    val targetId: String = "",
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "moderation_reports",
    indices = [
        Index(value = ["status"]),
        Index(value = ["reporterAccountId"])
    ]
)
data class ModerationReportEntity(
    @PrimaryKey val id: String,
    val reporterAccountId: String,
    val targetType: String,
    val targetId: String,
    val reason: String,
    val status: String, // OPEN, RESOLVED, DISMISSED
    val resolutionNote: String = "",
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "device_sessions",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["deviceType"])
    ]
)
data class DeviceSessionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val deviceType: String, // MOBILE | DESKTOP_LAPTOP
    val deviceLabel: String,
    val windowWidthDp: Int,
    val lastActiveEpochMs: Long,
    val revoked: Boolean = false
)

