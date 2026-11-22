package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillBridgeDao {
    // --- Accounts & Auth ---
    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun countAccounts(): Int

    @Query("SELECT * FROM accounts WHERE loginId = :loginId LIMIT 1")
    suspend fun getAccountByLoginId(loginId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    fun observeAccount(accountId: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts ORDER BY createdAtEpochMs DESC")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE role = 'STUDENT' ORDER BY points DESC")
    fun observeStudentAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE role = 'COMPANY_DIRECTOR' AND companyId = :companyId ORDER BY displayName ASC")
    fun observeCompanyDirectors(companyId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE role = 'PLATFORM_ADMIN'")
    suspend fun getAdminAccounts(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Upsert
    suspend fun upsertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    // --- Companies ---
    @Query("SELECT * FROM companies ORDER BY createdAtEpochMs DESC")
    fun observeAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE id = :companyId LIMIT 1")
    fun observeCompany(companyId: String): Flow<CompanyEntity?>

    @Query("SELECT * FROM companies WHERE id = :companyId LIMIT 1")
    suspend fun getCompanyById(companyId: String): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(companies: List<CompanyEntity>)

    @Upsert
    suspend fun upsertCompany(company: CompanyEntity)

    @Update
    suspend fun updateCompany(company: CompanyEntity)

    // --- Project Briefs ---
    @Query("SELECT * FROM project_briefs ORDER BY createdAtEpochMs DESC")
    fun observeAllBriefs(): Flow<List<ProjectBriefEntity>>

    @Query("SELECT * FROM project_briefs WHERE approvalStatus IN ('APPROVED', 'ACTIVE') ORDER BY createdAtEpochMs DESC")
    fun observePublishedBriefs(): Flow<List<ProjectBriefEntity>>

    @Query("SELECT * FROM project_briefs WHERE companyId = :companyId ORDER BY createdAtEpochMs DESC")
    fun observeCompanyBriefs(companyId: String): Flow<List<ProjectBriefEntity>>

    @Query("SELECT * FROM project_briefs WHERE assignedDirectorId = :directorId ORDER BY updatedAtEpochMs DESC")
    fun observeDirectorBriefs(directorId: String): Flow<List<ProjectBriefEntity>>

    @Query("SELECT * FROM project_briefs WHERE id = :briefId LIMIT 1")
    suspend fun getBriefById(briefId: String): ProjectBriefEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBriefs(briefs: List<ProjectBriefEntity>)

    @Upsert
    suspend fun upsertBrief(brief: ProjectBriefEntity)

    @Update
    suspend fun updateBrief(brief: ProjectBriefEntity)

    // --- Applications ---
    @Query("SELECT * FROM project_applications ORDER BY appliedAtEpochMs DESC")
    fun observeAllApplications(): Flow<List<ProjectApplicationEntity>>

    @Query("SELECT * FROM project_applications WHERE studentAccountId = :studentId ORDER BY appliedAtEpochMs DESC")
    fun observeStudentApplications(studentId: String): Flow<List<ProjectApplicationEntity>>

    @Query(
        """
        SELECT a.* FROM project_applications a
        INNER JOIN project_briefs b ON a.briefId = b.id
        WHERE b.companyId = :companyId
        ORDER BY a.appliedAtEpochMs DESC
        """
    )
    fun observeCompanyApplications(companyId: String): Flow<List<ProjectApplicationEntity>>

    @Query("SELECT * FROM project_applications WHERE briefId = :briefId AND studentAccountId = :studentId LIMIT 1")
    suspend fun getApplicationForBriefAndStudent(briefId: String, studentId: String): ProjectApplicationEntity?

    @Query("SELECT * FROM project_applications WHERE id = :applicationId LIMIT 1")
    suspend fun getApplicationById(applicationId: String): ProjectApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(applications: List<ProjectApplicationEntity>)

    @Upsert
    suspend fun upsertApplication(application: ProjectApplicationEntity)

    // --- Tasks ---
    @Query("SELECT * FROM project_tasks WHERE assignedStudentId = :studentId ORDER BY milestoneOrder ASC, createdAtEpochMs DESC")
    fun observeStudentTasks(studentId: String): Flow<List<ProjectTaskEntity>>

    @Query("SELECT * FROM project_tasks WHERE assignedDirectorId = :directorId ORDER BY milestoneOrder ASC, createdAtEpochMs DESC")
    fun observeDirectorTasks(directorId: String): Flow<List<ProjectTaskEntity>>

    @Query("SELECT * FROM project_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): ProjectTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ProjectTaskEntity>)

    @Upsert
    suspend fun upsertTask(task: ProjectTaskEntity)

    // --- Work Submissions ---
    @Query("SELECT * FROM work_submissions ORDER BY submittedAtEpochMs DESC")
    fun observeAllSubmissions(): Flow<List<WorkSubmissionEntity>>

    @Query("SELECT * FROM work_submissions WHERE studentAccountId = :studentId ORDER BY submittedAtEpochMs DESC")
    fun observeStudentSubmissions(studentId: String): Flow<List<WorkSubmissionEntity>>

    @Query("SELECT * FROM work_submissions WHERE directorAccountId = :directorId ORDER BY submittedAtEpochMs DESC")
    fun observeDirectorSubmissions(directorId: String): Flow<List<WorkSubmissionEntity>>

    @Query("SELECT * FROM work_submissions WHERE id = :submissionId LIMIT 1")
    suspend fun getSubmissionById(submissionId: String): WorkSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<WorkSubmissionEntity>)

    @Upsert
    suspend fun upsertSubmission(submission: WorkSubmissionEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE recipientAccountId = :accountId ORDER BY createdAtEpochMs DESC")
    fun observeNotifications(accountId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientAccountId = :accountId")
    suspend fun markAllNotificationsRead(accountId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId AND recipientAccountId = :accountId")
    suspend fun markNotificationRead(notificationId: String, accountId: String)

    @Upsert
    suspend fun upsertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    // --- Platform Activity & Reports ---
    @Query("SELECT * FROM platform_activities ORDER BY createdAtEpochMs DESC LIMIT 100")
    fun observeAllActivities(): Flow<List<PlatformActivityEntity>>

    @Query("SELECT * FROM moderation_reports ORDER BY createdAtEpochMs DESC")
    fun observeAllReports(): Flow<List<ModerationReportEntity>>

    @Query("SELECT * FROM moderation_reports WHERE id = :reportId LIMIT 1")
    suspend fun getReportById(reportId: String): ModerationReportEntity?

    @Upsert
    suspend fun upsertActivity(activity: PlatformActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<PlatformActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: PlatformActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ModerationReportEntity>)

    @Upsert
    suspend fun upsertReport(report: ModerationReportEntity)

    // --- Cross-Device Sessions (Mobile & Desktop/Laptop) ---
    @Query("SELECT * FROM device_sessions WHERE accountId = :accountId AND revoked = 0 ORDER BY lastActiveEpochMs DESC")
    fun observeDeviceSessions(accountId: String): Flow<List<DeviceSessionEntity>>

    @Query("SELECT * FROM device_sessions WHERE id = :sessionId LIMIT 1")
    fun observeDeviceSession(sessionId: String): Flow<DeviceSessionEntity?>

    @Query("SELECT * FROM device_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getDeviceSessionById(sessionId: String): DeviceSessionEntity?

    @Upsert
    suspend fun upsertDeviceSession(session: DeviceSessionEntity)

    @Query("UPDATE device_sessions SET revoked = 1 WHERE id = :sessionId")
    suspend fun revokeDeviceSession(sessionId: String)
}
