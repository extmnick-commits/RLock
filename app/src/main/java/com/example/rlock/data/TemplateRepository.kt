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
    fun resetData(categories: List<CustomCategory>, templates: List<BlockTemplate>, metrics: List<MetricGoal>)
    fun clearAllData()
}

class InMemoryTemplateRepository : TemplateRepository {

    private val _categories = MutableStateFlow<List<CustomCategory>>(listOf(CustomCategory.GENERAL))
    private val _templates = MutableStateFlow<List<BlockTemplate>>(emptyList())
    
    private var currentMetrics = emptyList<MetricGoal>()
    private val defaultMetrics = emptyList<MetricGoal>()

    init {
        // Start completely clean. No hardcoded templates or metrics.
    }

    override fun resetData(categories: List<CustomCategory>, templates: List<BlockTemplate>, metrics: List<MetricGoal>) {
        _categories.value = categories
        _templates.value = templates
        currentMetrics = metrics
    }

    override fun clearAllData() {
        _categories.value = listOf(CustomCategory.GENERAL)
        _templates.value = emptyList()
        currentMetrics = emptyList()
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

    override fun getDefaultMetrics(): List<MetricGoal> = if (currentMetrics.isNotEmpty()) currentMetrics else defaultMetrics

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