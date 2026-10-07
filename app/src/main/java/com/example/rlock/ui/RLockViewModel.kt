package com.example.rlock.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rlock.data.TemplateRepository
import com.example.rlock.model.AgendaBlock
import com.example.rlock.model.BlockTemplate
import com.example.rlock.model.CustomCategory
import com.example.rlock.model.DailyScorecard
import com.example.rlock.model.MetricGoal
import com.example.rlock.model.SideQuest
import com.example.rlock.model.Subtask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import com.example.rlock.model.RLockBackupData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import android.util.Log

val localTimeAdapter = object : TypeAdapter<LocalTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }
    override fun read(inReader: JsonReader): LocalTime? {
        if (inReader.peek() == com.google.gson.stream.JsonToken.NULL) {
            inReader.nextNull()
            return null
        }
        val str = inReader.nextString()
        return if (str.isNullOrBlank()) null else LocalTime.parse(str, formatter)
    }
}

val gson: Gson = GsonBuilder()
    .registerTypeAdapter(LocalTime::class.java, localTimeAdapter)
    .create()

class RLockViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("rlock_prefs", Context.MODE_PRIVATE)

    private val _agendaBlocks = MutableStateFlow<List<AgendaBlock>>(emptyList())
    val agendaBlocks: StateFlow<List<AgendaBlock>> = _agendaBlocks.asStateFlow()

    private val _metrics = MutableStateFlow<List<MetricGoal>>(emptyList())
    
    private val _sideQuests = MutableStateFlow<List<SideQuest>>(emptyList())
    val sideQuests: StateFlow<List<SideQuest>> = _sideQuests.asStateFlow()
    
    private val _templates = MutableStateFlow<List<BlockTemplate>>(emptyList())
    val templates: StateFlow<List<BlockTemplate>> = _templates.asStateFlow()

    private val _categories = MutableStateFlow<List<CustomCategory>>(emptyList())
    val categories: StateFlow<List<CustomCategory>> = _categories.asStateFlow()

    val focusedBlockId = MutableStateFlow<String?>(null)
    val selectedTab = MutableStateFlow(0)

    fun focusOnBlock(id: String) {
        focusedBlockId.value = id
        selectedTab.value = 0
    }

    fun clearFocusedBlock() {
        focusedBlockId.value = null
    }

    val scorecard: StateFlow<DailyScorecard> = combine(_agendaBlocks, _metrics) { blocks, metrics ->
        DailyScorecard(
            metrics = metrics,
            date = LocalDate.now().toString()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyScorecard(emptyList(), LocalDate.now().toString())
    )

    val categoryMap: StateFlow<Map<String, CustomCategory>> = categories
        .map { list -> list.associateBy { it.id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    init {
        val hasResetFresh = prefs.getBoolean("hasResetFresh_v2", false)
        if (!hasResetFresh) {
            clearAllData()
            prefs.edit().putBoolean("hasResetFresh_v2", true).putBoolean("isFirstLaunch", false).apply()
        } else {
            val savedJson = prefs.getString("backup_data", null)
            if (savedJson != null) {
                restoreFromBackupJson(savedJson)
            } else {
                clearAllData()
            }
        }
    }

    private fun persistData() {
        val json = exportBackupJson()
        prefs.edit().putString("backup_data", json).apply()
    }

    fun addCategory(name: String, emoji: String, colorHex: Long) {
        _categories.update { it + CustomCategory(name = name, emoji = emoji, colorHex = colorHex) }
        persistData()
    }

    fun updateCategory(category: CustomCategory) {
        _categories.update { list ->
            val index = list.indexOfFirst { it.id == category.id }
            if (index != -1) {
                list.toMutableList().apply { set(index, category) }
            } else {
                list + category
            }
        }
        
        // Also update any existing blocks for today with the new category name
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.categoryId == category.id) {
                    block.copy(categoryName = category.name)
                } else {
                    block
                }
            }
        }
        persistData()
    }

    fun deleteCategory(categoryId: String) {
        _categories.update { list -> list.filter { it.id != categoryId } }
        _templates.update { list ->
            list.map {
                if (it.categoryId == categoryId) {
                    it.copy(categoryId = CustomCategory.GENERAL.id, categoryName = CustomCategory.GENERAL.name)
                } else {
                    it
                }
            }
        }
        // Also update any existing blocks for today to GENERAL
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.categoryId == categoryId) {
                    block.copy(categoryId = CustomCategory.GENERAL.id, categoryName = CustomCategory.GENERAL.name)
                } else {
                    block
                }
            }
        }
        persistData()
    }

    fun addTemplateBlock(template: BlockTemplate) {
        _templates.update { it + template }
        addAgendaBlockForTemplate(template)
        persistData()
    }

    fun updateTemplateBlock(template: BlockTemplate) {
        _templates.update { currentTemplates ->
            val index = currentTemplates.indexOfFirst { it.id == template.id }
            if (index != -1) {
                currentTemplates.toMutableList().apply { set(index, template) }
            } else {
                currentTemplates + template
            }
        }
        // Update today's agenda block if it exists
        _agendaBlocks.update { blocks ->
            blocks.map { 
                if (it.templateId == template.id) {
                    it.copy(
                        title = template.title,
                        categoryId = template.categoryId,
                        categoryName = template.categoryName,
                        startTime = template.defaultStart,
                        endTime = template.defaultEnd,
                        shiftable = template.shiftable,
                        fallbackStartTime = template.fallbackStartTime,
                        fallbackEndTime = template.fallbackEndTime,
                        subtasks = template.subtasks.map { subtask ->
                            it.subtasks.find { existing -> existing.id == subtask.id }?.copy(name = subtask.name) 
                                ?: subtask.copy(id = UUID.randomUUID().toString())
                        }.toMutableList(),
                        isNotificationEnabled = template.isNotificationEnabled
                    )
                } else {
                    it
                }
            }
        }
        persistData()
    }

    fun deleteTemplateBlock(templateId: String) {
        _templates.update { currentTemplates ->
            currentTemplates.filter { it.id != templateId }
        }
        // Remove from today's agenda
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.filter { it.templateId != templateId }
        }
        persistData()
    }

    fun saveTemplate(template: BlockTemplate) {
        val currentTemplates = templates.value
        if (currentTemplates.any { it.id == template.id }) {
            updateTemplateBlock(template)
        } else {
            addTemplateBlock(template)
        }
    }

    fun deleteTemplate(templateId: String) {
        deleteTemplateBlock(templateId)
    }

    private fun addAgendaBlockForTemplate(template: BlockTemplate) {
        _agendaBlocks.update { blocks ->
            val newBlock = AgendaBlock(
                templateId = template.id,
                title = template.title,
                categoryId = template.categoryId,
                categoryName = template.categoryName,
                startTime = template.defaultStart,
                endTime = template.defaultEnd,
                shiftable = template.shiftable,
                fallbackStartTime = template.fallbackStartTime,
                fallbackEndTime = template.fallbackEndTime,
                subtasks = template.subtasks.map { it.copy(id = UUID.randomUUID().toString(), isCompleted = false) }.toMutableList(),
                isNotificationEnabled = template.isNotificationEnabled
            )
            (blocks + newBlock).sortedBy { it.startTime }
        }
    }

    /**
     * RLock Collision Engine
     * Inserts an appointment and resolves overlaps with existing blocks.
     */
    fun addAppointment(emoji: String, title: String, start: LocalTime, end: LocalTime) {
        val apptTitle = title.ifBlank { "Appointments" }
        val categoryId = "appointment_${apptTitle.lowercase().replace(" ", "_")}"
        val resolvedEmoji = emoji.ifBlank { "📅" }
        val customCat = CustomCategory(
            id = categoryId,
            name = apptTitle,
            emoji = resolvedEmoji,
            colorHex = 0xFFFFC107
        )
        _categories.update { list ->
            if (list.any { it.id == categoryId || it.name.equals(apptTitle, ignoreCase = true) }) {
                list
            } else {
                list + customCat
            }
        }

        val newAppointment = AgendaBlock(
            title = apptTitle,
            section = "$resolvedEmoji $apptTitle",
            categoryId = categoryId,
            categoryName = apptTitle,
            startTime = start,
            endTime = end,
        )

        _agendaBlocks.update { currentBlocks ->
            val updatedBlocks = mutableListOf<AgendaBlock>()
            updatedBlocks.add(newAppointment)

            for (block in currentBlocks) {
                if (block.categoryId == CustomCategory.APPOINTMENT.id || block.categoryId.contains("appointment", ignoreCase = true)) {
                    updatedBlocks.add(block)
                    continue
                }

                // Check for overlap
                val isOverlapping = block.startTime.isBefore(end) && block.endTime.isAfter(start)

                if (isOverlapping) {
                    if (block.shiftable && block.fallbackStartTime != null && block.fallbackEndTime != null) {
                        // Shift to fallback time
                        val newTitle = block.title
                        updatedBlocks.add(
                            block.copy(
                                title = newTitle,
                                section = "🌆 Evening",
                                startTime = block.fallbackStartTime,
                                endTime = block.fallbackEndTime,
                                isDisplaced = true
                            )
                        )
                    } else {
                        // Not shiftable, shrink or split
                        if (block.startTime.isBefore(start) && block.endTime.isAfter(end)) {
                            // Appointment falls completely inside the block -> SPLIT
                            val firstPart = block.copy(endTime = start, isDisplaced = true)
                            val secondPart = block.copy(id = UUID.randomUUID().toString(), startTime = end, isDisplaced = true)
                            updatedBlocks.add(firstPart)
                            updatedBlocks.add(secondPart)
                        } else if (block.startTime.isBefore(start)) {
                            // Overlaps the end of the block -> SHRINK end
                            updatedBlocks.add(block.copy(endTime = start, isDisplaced = true))
                        } else if (block.endTime.isAfter(end)) {
                            // Overlaps the start of the block -> SHRINK start
                            updatedBlocks.add(block.copy(startTime = end, isDisplaced = true))
                        }
                        // If it is completely eclipsed by the appointment, it is effectively removed/skipped.
                    }
                } else {
                    // No overlap, keep as is
                    updatedBlocks.add(block)
                }
            }

            // Return sorted by start time
            updatedBlocks.sortedBy { it.startTime }
        }
        persistData()
    }

    fun addQuickBlock(
        title: String,
        category: CustomCategory,
        start: LocalTime,
        end: LocalTime,
        shiftable: Boolean = false,
        fallbackStart: LocalTime? = null,
        fallbackEnd: LocalTime? = null
    ) {
        val blockTitle = title.ifBlank { category.name }
        val newBlock = AgendaBlock(
            title = blockTitle,
            categoryId = category.id,
            categoryName = blockTitle,
            startTime = start,
            endTime = end,
            shiftable = shiftable,
            fallbackStartTime = fallbackStart,
            fallbackEndTime = fallbackEnd
        )
        _agendaBlocks.update { current ->
            (current + newBlock).sortedBy { it.startTime }
        }
        persistData()
    }

    fun updateAgendaBlock(blockId: String, title: String, start: LocalTime, end: LocalTime) {
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId) {
                    block.copy(
                        title = title,
                        categoryName = title,
                        startTime = start,
                        endTime = end
                    )
                } else {
                    block
                }
            }.sortedBy { it.startTime }
        }
        persistData()
    }

    fun deleteAgendaBlock(blockId: String) {
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.filter { it.id != blockId }
        }
        persistData()
    }

    fun resetTodayToDefaults() {
        _agendaBlocks.value = _templates.value.map { template ->
            AgendaBlock(
                templateId = template.id,
                title = template.title,
                section = template.section,
                categoryId = template.categoryId,
                categoryName = template.categoryName,
                startTime = template.defaultStart,
                endTime = template.defaultEnd,
                shiftable = template.shiftable,
                fallbackStartTime = template.fallbackStartTime,
                fallbackEndTime = template.fallbackEndTime,
                subtasks = template.subtasks.map { it.copy(id = UUID.randomUUID().toString(), isCompleted = false) }.toMutableList(),
                isNotificationEnabled = template.isNotificationEnabled
            )
        }.sortedBy { it.startTime }
        persistData()
    }

    fun toggleBlockCompletion(blockId: String) {
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId) {
                    val newCompletionState = !block.isCompleted
                    block.copy(
                        isCompleted = newCompletionState,
                        subtasks = block.subtasks.map { it.copy(isCompleted = newCompletionState) }.toMutableList()
                    )
                } else {
                    block
                }
            }
        }
        persistData()
    }

    fun toggleSubtaskCompletion(blockId: String, subtaskId: String) {
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId) {
                    val newSubtasks = block.subtasks.map {
                        if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
                    }
                    val allCompleted = newSubtasks.isNotEmpty() && newSubtasks.all { it.isCompleted }
                    block.copy(subtasks = newSubtasks.toMutableList(), isCompleted = allCompleted)
                } else {
                    block
                }
            }
        }
        persistData()
    }

    fun incrementMetric(metricId: String, delta: Int) {
        _metrics.update { currentMetrics ->
            currentMetrics.map { metric ->
                if (metric.id == metricId) {
                    metric.copy(current = metric.current + delta)
                } else {
                    metric
                }
            }
        }
        persistData()
    }

    fun decrementMetric(metricId: String) {
        _metrics.update { goals ->
            goals.map { goal ->
                if (goal.id == metricId) goal.copy(current = (goal.current - 1).coerceAtLeast(0))
                else goal
            }
        }
        persistData()
    }

    fun updateMetricGoal(metricId: String, name: String, target: Int) {
        _metrics.update { currentMetrics ->
            currentMetrics.map { metric ->
                if (metric.id == metricId) {
                    metric.copy(name = name, target = target)
                } else {
                    metric
                }
            }
        }
        persistData()
    }

    fun addMetricGoal(name: String, target: Int) {
        val newMetric = MetricGoal(name = name, target = target)
        _metrics.update { current -> current + newMetric }
        persistData()
    }

    fun deleteMetricGoal(metricId: String) {
        _metrics.update { current -> current.filter { it.id != metricId } }
        persistData()
    }

    fun addSubtask(blockId: String, subtaskName: String) {
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId) {
                    val newSubtasks = block.subtasks + Subtask(name = subtaskName)
                    block.copy(subtasks = newSubtasks.toMutableList(), isCompleted = false)
                } else {
                    block
                }
            }
        }
        persistData()
    }

    fun addSideQuest(title: String) {
        if (title.isBlank()) return
        val newQuest = SideQuest(title = title)
        _sideQuests.update { current ->
            (listOf(newQuest) + current).sortedBy { it.createdAt }
        }
        persistData()
    }

    fun toggleSideQuest(id: String) {
        _sideQuests.update { current ->
            current.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it }
        }
        persistData()
    }

    fun deleteSideQuest(id: String) {
        _sideQuests.update { current ->
            current.filter { it.id != id }
        }
        persistData()
    }

    fun clearCompletedSideQuests() {
        _sideQuests.update { current ->
            current.filter { !it.isCompleted }
        }
        persistData()
    }

    fun exportBackupJson(): String {
        val backup = RLockBackupData(
            categories = categories.value,
            blockTemplates = templates.value,
            activeAgenda = _agendaBlocks.value,
            sideQuests = _sideQuests.value,
            metricGoals = _metrics.value
        )
        return gson.toJson(backup)
    }

    fun restoreFromBackupJson(jsonString: String): Boolean {
        return try {
            val backup = gson.fromJson(jsonString, RLockBackupData::class.java)
            if (backup != null) {
                _categories.value = backup.categories
                _templates.value = backup.blockTemplates
                _metrics.value = backup.metricGoals
                _agendaBlocks.value = backup.activeAgenda
                _sideQuests.value = backup.sideQuests
                persistData()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("RLockViewModel", "Failed to restore from backup", e)
            false
        }
    }

    fun clearAllData() {
        _categories.value = CustomCategory.defaultCategories
        _templates.value = emptyList()
        _metrics.value = emptyList()
        _agendaBlocks.value = emptyList()
        _sideQuests.value = emptyList()
        persistData()
    }

    fun loadStarterTemplate() {
        val starterCategories = CustomCategory.defaultCategories
        val starterTemplates = listOf(
            BlockTemplate(
                title = "Property maintenance",
                section = "🌅 Morning",
                categoryId = CustomCategory.ROUTINE.id,
                categoryName = CustomCategory.ROUTINE.name,
                defaultStart = LocalTime.of(6, 0),
                defaultEnd = LocalTime.of(8, 0),
                subtasks = mutableListOf(
                    Subtask(name = "Clean"),
                    Subtask(name = "Yard work"),
                    Subtask(name = "Property prep")
                )
            ),
            BlockTemplate(
                title = "Get ready / breakfast / reset",
                section = "🌅 Morning",
                categoryId = CustomCategory.ROUTINE.id,
                categoryName = CustomCategory.ROUTINE.name,
                defaultStart = LocalTime.of(8, 0),
                defaultEnd = LocalTime.of(9, 0)
            ),
            BlockTemplate(
                title = "Series 26 study",
                section = "🌅 Morning",
                categoryId = CustomCategory.STUDY.id,
                categoryName = CustomCategory.STUDY.name,
                defaultStart = LocalTime.of(9, 0),
                defaultEnd = LocalTime.of(10, 0),
                shiftable = true,
                fallbackStartTime = LocalTime.of(20, 0),
                fallbackEndTime = LocalTime.of(22, 0),
                subtasks = mutableListOf(
                    Subtask(name = "Complete study section"),
                    Subtask(name = "QBank/practice questions"),
                    Subtask(name = "Review missed questions")
                )
            ),
            BlockTemplate(
                title = "Standard Prospecting Block",
                section = "📅 Appointments & Prospecting",
                categoryId = CustomCategory.PROSPECTING.id,
                categoryName = CustomCategory.PROSPECTING.name,
                defaultStart = LocalTime.of(11, 30),
                defaultEnd = LocalTime.of(16, 0),
                subtasks = mutableListOf(
                    Subtask(name = "Calls/texts/invites"),
                    Subtask(name = "Follow-ups"),
                    Subtask(name = "Set appointments")
                )
            ),
            BlockTemplate(
                title = "Calls/follow-up",
                section = "🌆 Evening",
                categoryId = CustomCategory.ROUTINE.id,
                categoryName = CustomCategory.ROUTINE.name,
                defaultStart = LocalTime.of(17, 0),
                defaultEnd = LocalTime.of(18, 0)
            )
        )
        val starterMetrics = listOf(
            MetricGoal(name = "New Numbers", target = 10),
            MetricGoal(name = "Calls", target = 25),
            MetricGoal(name = "Appointments", target = 5)
        )

        _categories.value = starterCategories
        _templates.value = starterTemplates
        _metrics.value = starterMetrics
        resetTodayToDefaults()
        _sideQuests.value = emptyList()
        persistData()
    }
}