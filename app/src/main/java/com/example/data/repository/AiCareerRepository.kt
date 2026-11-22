package com.example.data.repository

import android.util.Log
import com.example.data.model.CoachMessage
import com.example.data.model.GeneratedGapItem
import com.example.data.model.GeneratedMilestoneItem
import com.example.data.model.RoadmapMilestone
import com.example.data.model.SkillCategoryScore
import com.example.data.model.SkillGapAnalysisResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class AiCareerRepository {

    private val analysisSchema = Schema.obj(
        properties = mapOf(
            "readinessScore" to Schema.integer(description = "Estimated readiness percentage 0-100"),
            "executiveSummary" to Schema.string(description = "Concise 2-3 sentence assessment and strategy"),
            "confidencePercent" to Schema.integer(description = "Model confidence 50-98"),
            "aiExplanation" to Schema.string(description = "Transparent explanation of why these gaps and recommendations were selected"),
            "technicalScore" to Schema.integer(description = "0-100 Technical Skills score"),
            "softSkillsScore" to Schema.integer(description = "0-100 Soft Skills score"),
            "domainScore" to Schema.integer(description = "0-100 Domain Knowledge score"),
            "problemSolvingScore" to Schema.integer(description = "0-100 Problem Solving score"),
            "communicationScore" to Schema.integer(description = "0-100 Communication & Collaboration score"),
            "matchedSkills" to Schema.array(
                items = Schema.string(),
                description = "Skills the user already possesses that apply to the target role"
            ),
            "skillGaps" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "skillName" to Schema.string(),
                        "priority" to Schema.enumeration(listOf("HIGH", "MEDIUM", "LOW")),
                        "currentLevel" to Schema.string(),
                        "targetLevel" to Schema.string(),
                        "whyItMatters" to Schema.string()
                    )
                ),
                description = "Top 3-6 prioritized skill gaps"
            ),
            "milestones" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "phaseOrder" to Schema.integer(),
                        "title" to Schema.string(),
                        "description" to Schema.string(),
                        "estimatedHours" to Schema.integer(),
                        "practiceProject" to Schema.string(),
                        "resourceTip" to Schema.string()
                    )
                ),
                description = "Ordered 4-6 actionable learning milestones"
            )
        )
    )

    private val analysisModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = analysisSchema
                temperature = 0.35f
            }
        )
    }

    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "You are SkillBridge AI, a pragmatic, responsible career mentor, technical interviewer, and work-simulation client/PM. " +
                        "Never guarantee employment or fabricate credentials. Encourage verified practice, ethical engineering, and clear reasoning. " +
                        "Respond in the same language (Arabic or English) as the user prompt when requested."
                )
            }
        )
    }

    suspend fun analyzeSkillGap(
        targetRole: String,
        experienceLevel: String,
        currentSkills: List<String>,
        backgroundNotes: String,
        weeklyHours: Int,
        isArabic: Boolean = false
    ): SkillGapAnalysisResult {
        val prompt = buildString {
            appendLine("Perform a realistic, explainable skill gap analysis and practical roadmap.")
            appendLine("Language for output strings: ${if (isArabic) "Arabic" else "English"}")
            appendLine("Target Role: $targetRole")
            appendLine("Target Transition / Level: $experienceLevel")
            appendLine("Weekly Study Capacity: $weeklyHours hours/week")
            appendLine("Current Skills: ${if (currentSkills.isEmpty()) "None specified yet" else currentSkills.joinToString(", ")}")
            if (backgroundNotes.isNotBlank()) {
                appendLine("Additional Background / Evidence Notes: ${backgroundNotes.trim()}")
            }
            appendLine("Include category scores (technical, softSkills, domain, problemSolving, communication), an explicit aiExplanation detailing why recommendations were chosen, 3-6 prioritized skillGaps, and 4-6 sequential milestones with practical challenge deliverables.")
        }

        val response = withTimeout(60_000L) {
            analysisModel.generateContent(prompt)
        }
        val rawJson = response.text ?: throw IllegalStateException("AI returned an empty analysis.")
        return parseAnalysisJson(rawJson, currentSkills)
    }

    fun streamMilestoneTutor(
        targetRole: String,
        milestone: RoadmapMilestone,
        isArabic: Boolean = false
    ): Flow<String> = flow {
        val prompt = buildString {
            appendLine("Respond in ${if (isArabic) "Arabic" else "English"}.")
            appendLine("Target role: $targetRole.")
            appendLine("Create a focused 3-step practical micro-sprint for:")
            appendLine("Milestone: ${milestone.title}")
            appendLine("Objective: ${milestone.description}")
            appendLine("Practice Deliverable: ${milestone.practiceProject}")
            appendLine("Include: (1) Core concepts, (2) 30-min hands-on verification drill, (3) One rubric self-check question.")
        }
        val sb = StringBuilder()
        withTimeout(60_000L) {
            coachModel.generateContentStream(prompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    sb.append(text)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("AI tutor returned an empty response.")
        }
    }

    fun streamWorkSimulationReply(
        challengeTitle: String,
        userDecision: String,
        isArabic: Boolean = false
    ): Flow<String> = flow {
        val prompt = buildString {
            appendLine("Respond in ${if (isArabic) "Arabic" else "English"}.")
            appendLine("Roleplay as a demanding Engineering Manager & Product Owner during a live project simulation for: '$challengeTitle'.")
            appendLine("The candidate's decision/response to the mid-sprint requirement shift is:")
            appendLine(userDecision)
            appendLine("Evaluate their engineering trade-offs, communication clarity, and risk awareness out of 10, then introduce one realistic follow-up edge case.")
        }
        val sb = StringBuilder()
        withTimeout(60_000L) {
            coachModel.generateContentStream(prompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    sb.append(text)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("Work simulation returned an empty response.")
        }
    }

    fun streamCoachResponse(
        mode: String,
        targetRole: String,
        matchedSkills: List<String>,
        topGaps: List<String>,
        history: List<CoachMessage>,
        userPrompt: String
    ): Flow<String> = flow {
        val contextHeader = buildString {
            appendLine("[Context]")
            appendLine("Mode: ${if (mode == "INTERVIEW") "Mock Technical/Behavioral Interviewer (ask one sharp question or evaluate with score out of 10)" else "SkillBridge Career & Practice Coach"}")
            appendLine("Target Role: ${targetRole.ifBlank { "Engineering & Data Career Growth" }}")
            if (matchedSkills.isNotEmpty()) appendLine("Existing Strengths: ${matchedSkills.joinToString(", ")}")
            if (topGaps.isNotEmpty()) appendLine("Priority Skill Gaps: ${topGaps.joinToString(", ")}")
        }

        val chatHistory = buildList {
            add(content("user") { text(contextHeader) })
            add(content("model") { text("Understood. I have your SkillBridge profile context ready.") })
            history.takeLast(10).forEach { msg ->
                val role = if (msg.sender == "AI") "model" else "user"
                add(content(role) { text(msg.content) })
            }
        }

        val chat = coachModel.startChat(history = chatHistory)
        val sb = StringBuilder()
        withTimeout(60_000L) {
            chat.sendMessageStream(userPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    sb.append(text)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("Coach returned an empty response.")
        }
    }

    fun streamContextualAgent(
        mode: com.example.data.model.AiAgentContextMode,
        isArabic: Boolean,
        contextSummary: String,
        allowPersonalization: Boolean,
        allowContextSharing: Boolean,
        history: List<com.example.data.model.AiAgentMessage>,
        userPrompt: String
    ): Flow<String> = flow {
        val guardHeader = buildString {
            appendLine("[SkillBridge AI Agent Session]")
            appendLine("Language: ${if (isArabic) "Arabic" else "English"}")
            appendLine("Agent Role: ${mode.title.en} (${mode.purpose.en})")
            appendLine("Mandatory Boundaries: ${mode.boundaries.en}")
            if (allowPersonalization && allowContextSharing && contextSummary.isNotBlank()) {
                appendLine("Allowed Grounded Screen & User Context:")
                appendLine(contextSummary)
            } else {
                appendLine("Context Sharing Disabled: Use general software engineering & SkillBridge guidelines only; do not assume private profile metrics.")
            }
            appendLine("Never invent fake verified badges, scores, or certificates. Be concise, practical, and structured.")
        }

        val chatHistory = buildList {
            add(content("user") { text(guardHeader) })
            add(content("model") { text("Ready. I will operate strictly within these SkillBridge boundaries and respond in ${if (isArabic) "Arabic" else "English"}.") })
            history.takeLast(8).forEach { msg ->
                val role = if (msg.sender == "AI") "model" else "user"
                add(content(role) { text(msg.content) })
            }
        }

        val chat = coachModel.startChat(history = chatHistory)
        val sb = StringBuilder()
        withTimeout(60_000L) {
            chat.sendMessageStream(userPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    sb.append(text)
                    emit(sb.toString())
                }
            }
        }
        if (sb.isBlank()) {
            throw IllegalStateException("AI Agent returned an empty response.")
        }
    }

    private fun parseAnalysisJson(
        jsonString: String,
        currentSkills: List<String>
    ): SkillGapAnalysisResult {
        try {
            val json = JSONObject(jsonString)
            val readinessScore = json.optInt("readinessScore", 74).coerceIn(0, 100)
            val executiveSummary = json.optString("executiveSummary", "")
            val confidence = json.optInt("confidencePercent", 86).coerceIn(50, 99)
            val explanation = json.optString(
                "aiExplanation",
                "Recommendations were prioritized using your declared skills, weekly study budget, and target role rubric."
            )
            val categories = SkillCategoryScore(
                technical = json.optInt("technicalScore", 86).coerceIn(0, 100),
                softSkills = json.optInt("softSkillsScore", 78).coerceIn(0, 100),
                domainKnowledge = json.optInt("domainScore", 84).coerceIn(0, 100),
                problemSolving = json.optInt("problemSolvingScore", 89).coerceIn(0, 100),
                communication = json.optInt("communicationScore", 81).coerceIn(0, 100)
            )
            val matchedArr = json.optJSONArray("matchedSkills")
            val matchedSkills = buildList {
                if (matchedArr != null) {
                    for (i in 0 until matchedArr.length()) {
                        val s = matchedArr.optString(i).trim()
                        if (s.isNotEmpty()) add(s)
                    }
                }
            }

            val gapsArr = json.optJSONArray("skillGaps")
            val skillGaps = buildList {
                if (gapsArr != null) {
                    for (i in 0 until gapsArr.length()) {
                        val obj = gapsArr.optJSONObject(i) ?: continue
                        add(
                            GeneratedGapItem(
                                skillName = obj.optString("skillName", "Key Skill"),
                                priority = obj.optString("priority", "MEDIUM"),
                                currentLevel = obj.optString("currentLevel", "Beginner"),
                                targetLevel = obj.optString("targetLevel", "Proficient"),
                                whyItMatters = obj.optString("whyItMatters", "Important for target role.")
                            )
                        )
                    }
                }
            }

            val msArr = json.optJSONArray("milestones")
            val milestones = buildList {
                if (msArr != null) {
                    for (i in 0 until msArr.length()) {
                        val obj = msArr.optJSONObject(i) ?: continue
                        add(
                            GeneratedMilestoneItem(
                                phaseOrder = obj.optInt("phaseOrder", i + 1),
                                title = obj.optString("title", "Milestone ${i + 1}"),
                                description = obj.optString("description", "Master core concepts."),
                                estimatedHours = obj.optInt("estimatedHours", 5).coerceIn(1, 200),
                                practiceProject = obj.optString("practiceProject", "Build a mini project."),
                                resourceTip = obj.optString("resourceTip", "Focus on hands-on practice.")
                            )
                        )
                    }
                }
            }

            return SkillGapAnalysisResult(
                readinessScore = readinessScore,
                executiveSummary = executiveSummary,
                matchedSkills = matchedSkills,
                skillGaps = skillGaps,
                milestones = milestones,
                categoryScores = categories,
                aiExplanation = explanation,
                confidencePercent = confidence,
                inputsUsed = listOfNotNull(
                    "Skills: ${currentSkills.take(3).joinToString(", ").ifEmpty { "Profile baseline" }}",
                    "Rubric Benchmarks",
                    "Weekly Study Hours"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse skill gap analysis JSON", e)
            throw IllegalStateException("Could not parse AI roadmap response.", e)
        }
    }

    private companion object {
        const val TAG = "AiCareerRepo"
    }
}
