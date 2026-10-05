package com.example.rlock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rlock.data.TemplateRepository
import com.example.rlock.model.AgendaBlock
import com.example.rlock.model.BlockTemplate
import com.example.rlock.model.Category
import com.example.rlock.model.DailyScorecard
import com.example.rlock.model.MetricGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class RLockViewModel(
    private val repository: TemplateRepository
) : ViewModel() {

    private val _agendaBlocks = MutableStateFlow<List<AgendaBlock>>(emptyList())
    val agendaBlocks: StateFlow<List<AgendaBlock>> = _agendaBlocks.asStateFlow()

    private val _metrics = MutableStateFlow<List<MetricGoal>>(repository.getDefaultMetrics())
    
    val scorecard: StateFlow<DailyScorecard> = combine(_agendaBlocks, _metrics) { blocks, metrics ->
        DailyScorecard(
            metrics = metrics,
            date = LocalDate.now().toString()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyScorecard(repository.getDefaultMetrics(), LocalDate.now().toString())
    )

    val templates: StateFlow<List<BlockTemplate>> = repository.getTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initialize today's agenda based on default templates
        _agendaBlocks.value = repository.generateTodayAgenda()
    }

    /**
     * RLock Collision Engine
     * Inserts an appointment and resolves overlaps with existing blocks.
     */
    fun addAppointment(title: String, start: LocalTime, end: LocalTime) {
        val newAppointment = AgendaBlock(
            title = title,
            section = "📅 Appointments & Prospecting",
            category = Category.APPOINTMENT,
            startTime = start,
            endTime = end,
        )

        _agendaBlocks.update { currentBlocks ->
            val updatedBlocks = mutableListOf<AgendaBlock>()
            updatedBlocks.add(newAppointment)

            for (block in currentBlocks) {
                if (block.category == Category.APPOINTMENT) {
                    updatedBlocks.add(block)
                    continue
                }

                // Check for overlap
                val isOverlapping = block.startTime.isBefore(end) && block.endTime.isAfter(start)

                if (isOverlapping) {
                    if (block.shiftable && block.fallbackStartTime != null && block.fallbackEndTime != null) {
                        // Shift to fallback time
                        val newTitle = if (block.title.contains("Series 26", ignoreCase = true)) "Series 26 Flex Study" else block.title
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
    }

    fun addQuickBlock(
        title: String,
        category: Category,
        start: LocalTime,
        end: LocalTime,
        shiftable: Boolean = false,
        fallbackStart: LocalTime? = null,
        fallbackEnd: LocalTime? = null
    ) {
        val newBlock = AgendaBlock(
            title = title,
            category = category,
            startTime = start,
            endTime = end,
            shiftable = shiftable,
            fallbackStartTime = fallbackStart,
            fallbackEndTime = fallbackEnd
        )
        _agendaBlocks.update { current ->
            (current + newBlock).sortedBy { it.startTime }
        }
    }

    fun toggleBlockCompletion(blockId: String) {
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId) {
                    val newCompletionState = !block.isCompleted
                    block.copy(
                        isCompleted = newCompletionState,
                        subtasks = block.subtasks.map { it.copy(isCompleted = newCompletionState) }
                    )
                } else {
                    block
                }
            }
        }
    }

    fun toggleSubtaskCompletion(blockId: String, subtaskId: String) {
        _agendaBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId) {
                    val newSubtasks = block.subtasks.map {
                        if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
                    }
                    val allCompleted = newSubtasks.isNotEmpty() && newSubtasks.all { it.isCompleted }
                    block.copy(subtasks = newSubtasks, isCompleted = allCompleted)
                } else {
                    block
                }
            }
        }
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
    }

    fun addMetricGoal(name: String, target: Int) {
        val newMetric = MetricGoal(name = name, target = target)
        _metrics.update { current -> current + newMetric }
    }

    fun deleteMetricGoal(metricId: String) {
        _metrics.update { current -> current.filter { it.id != metricId } }
    }

    fun saveTemplate(template: BlockTemplate) {
        viewModelScope.launch {
            val currentTemplates = templates.value
            if (currentTemplates.any { it.id == template.id }) {
                repository.updateTemplate(template)
            } else {
                repository.addTemplate(template)
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
        }
    }
}
