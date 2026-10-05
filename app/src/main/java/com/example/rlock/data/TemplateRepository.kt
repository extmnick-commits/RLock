package com.example.rlock.data

import com.example.rlock.model.AgendaBlock
import com.example.rlock.model.BlockTemplate
import com.example.rlock.model.CustomCategory
import com.example.rlock.model.MetricGoal
import com.example.rlock.model.Subtask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.util.UUID

interface TemplateRepository {
    fun getCategories(): Flow<List<CustomCategory>>
    fun addCategory(category: CustomCategory)
    fun updateCategory(category: CustomCategory)
    fun deleteCategory(categoryId: String)
    
    fun getTemplates(): Flow<List<BlockTemplate>>
    fun getDefaultMetrics(): List<MetricGoal>
    fun generateTodayAgenda(): List<AgendaBlock>
    fun updateTemplate(template: BlockTemplate)
    fun addTemplate(template: BlockTemplate)
    fun deleteTemplate(templateId: String)
}

class InMemoryTemplateRepository : TemplateRepository {

    private val _categories = MutableStateFlow<List<CustomCategory>>(CustomCategory.defaultCategories)
    private val _templates = MutableStateFlow<List<BlockTemplate>>(emptyList())
    
    private val defaultMetrics = listOf(
        MetricGoal(name = "New Numbers", target = 10),
        MetricGoal(name = "Calls", target = 25),
        MetricGoal(name = "Appointments", target = 5)
    )

    init {
        // Pre-populate with default templates using category id/name
        _templates.value = listOf(
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
    }

    override fun getCategories(): Flow<List<CustomCategory>> = _categories.asStateFlow()

    override fun addCategory(category: CustomCategory) {
        _categories.update { it + category }
    }

    override fun updateCategory(category: CustomCategory) {
        _categories.update { list ->
            val index = list.indexOfFirst { it.id == category.id }
            if (index != -1) {
                list.toMutableList().apply { set(index, category) }
            } else {
                list + category
            }
        }
    }

    override fun deleteCategory(categoryId: String) {
        _categories.update { list -> list.filter { it.id != categoryId } }
        // Update orphaned templates to general
        _templates.update { list ->
            list.map {
                if (it.categoryId == categoryId) {
                    it.copy(categoryId = CustomCategory.GENERAL.id, categoryName = CustomCategory.GENERAL.name)
                } else {
                    it
                }
            }
        }
    }

    override fun getTemplates(): Flow<List<BlockTemplate>> = _templates.asStateFlow()

    override fun getDefaultMetrics(): List<MetricGoal> = defaultMetrics

    override fun generateTodayAgenda(): List<AgendaBlock> {
        return _templates.value.map { template ->
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
    }

    override fun updateTemplate(template: BlockTemplate) {
        _templates.update { currentTemplates ->
            val index = currentTemplates.indexOfFirst { it.id == template.id }
            if (index != -1) {
                currentTemplates.toMutableList().apply { set(index, template) }
            } else {
                currentTemplates + template
            }
        }
    }

    override fun addTemplate(template: BlockTemplate) {
        _templates.update { currentTemplates ->
            currentTemplates + template
        }
    }

    override fun deleteTemplate(templateId: String) {
        _templates.update { currentTemplates ->
            currentTemplates.filter { it.id != templateId }
        }
    }
}