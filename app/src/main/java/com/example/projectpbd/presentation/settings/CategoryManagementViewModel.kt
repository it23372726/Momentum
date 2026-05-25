package com.example.projectpbd.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.CategorySource
import com.example.projectpbd.domain.model.CategoryType
import com.example.projectpbd.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CategoryManagementUiState(
    val categories: List<Category> = emptyList(),
    val selectedType: CategoryType = CategoryType.EXPENSE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryManagementUiState())
    val state = _state.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        _state.map { it.selectedType }
            .distinctUntilChanged()
            .flatMapLatest { type ->
                repository.observeCategories(type)
            }
            .onEach { resource ->
                when (resource) {
                    is Resource.Success -> _state.update { it.copy(categories = resource.data, isLoading = false) }
                    is Resource.Error -> _state.update { it.copy(errorMessage = resource.message, isLoading = false) }
                    Resource.Loading -> _state.update { it.copy(isLoading = true) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun setType(type: CategoryType) {
        _state.update { it.copy(selectedType = type) }
    }

    fun addCategory(name: String, iconKey: String?, colorKey: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val category = Category(
                id = "",
                name = name,
                type = _state.value.selectedType,
                source = CategorySource.USER,
                iconKey = iconKey,
                colorKey = colorKey
            )
            val result = repository.addCategory(category)
            handleResult(result, "Category added.")
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            val result = repository.updateCategory(category)
            handleResult(result, "Category updated.")
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            val result = repository.deleteCategory(id)
            handleResult(result, "Category removed.")
        }
    }

    private fun handleResult(result: Resource<Unit>, successMsg: String) {
        when (result) {
            is Resource.Success -> _state.update { it.copy(successMessage = successMsg) }
            is Resource.Error -> _state.update { it.copy(errorMessage = result.message) }
            else -> Unit
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
