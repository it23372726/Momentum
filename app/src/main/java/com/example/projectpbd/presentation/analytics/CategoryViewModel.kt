package com.example.projectpbd.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.CategorySource
import com.example.projectpbd.domain.model.CategoryType
import com.example.projectpbd.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryUiState())
    val state: StateFlow<CategoryUiState> = _state.asStateFlow()

    fun observeCategories(type: CategoryType? = null) {
        viewModelScope.launch {
            repository.observeCategories(type).collect { resource ->
                when (resource) {
                    is Resource.Success -> _state.update { it.copy(categories = resource.data, isLoading = false) }
                    is Resource.Error -> _state.update { it.copy(error = resource.message, isLoading = false) }
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun addCategory(name: String, type: CategoryType) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "Category name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, success = false) }
            val category = Category(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                type = type,
                source = CategorySource.USER,
                createdAt = System.currentTimeMillis()
            )
            when (val result = repository.addCategory(category)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, success = true) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, success = false) }
    }
}
