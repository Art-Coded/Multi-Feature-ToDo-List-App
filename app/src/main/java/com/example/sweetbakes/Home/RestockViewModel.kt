package com.example.sweetbakes.Home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sweetbakes.data.AppDatabase
import com.example.sweetbakes.data.IngredientEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RestockViewModel(database: AppDatabase) : ViewModel() {
    private val ingredientDao = database.ingredientDao()

    // State flows for all mutable states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddIngredientDialog = MutableStateFlow(false)
    val showAddIngredientDialog: StateFlow<Boolean> = _showAddIngredientDialog.asStateFlow()

    private val _showEditMode = MutableStateFlow(false)
    val showEditMode: StateFlow<Boolean> = _showEditMode.asStateFlow()

    private val _ingredientName = MutableStateFlow("")
    val ingredientName: StateFlow<String> = _ingredientName.asStateFlow()

    private val _ingredientQuantity = MutableStateFlow("0")
    val ingredientQuantity: StateFlow<String> = _ingredientQuantity.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _ingredientToDelete = MutableStateFlow<IngredientEntity?>(null)
    val ingredientToDelete: StateFlow<IngredientEntity?> = _ingredientToDelete.asStateFlow()

    private val _showEditIngredientDialog = MutableStateFlow(false)
    val showEditIngredientDialog: StateFlow<Boolean> = _showEditIngredientDialog.asStateFlow()

    private val _ingredientToEdit = MutableStateFlow<IngredientEntity?>(null)
    val ingredientToEdit: StateFlow<IngredientEntity?> = _ingredientToEdit.asStateFlow()

    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()

    private val _editQuantity = MutableStateFlow("")
    val editQuantity: StateFlow<String> = _editQuantity.asStateFlow()

    private val _ingredients = MutableStateFlow<List<IngredientEntity>>(emptyList())
    val ingredients: StateFlow<List<IngredientEntity>> = _ingredients.asStateFlow()

    init {
        viewModelScope.launch {
            ingredientDao.getAllIngredients().collectLatest { ingredients ->
                _ingredients.value = ingredients
            }
        }
    }

    // Public functions to update state
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleEditMode() {
        _showEditMode.value = !_showEditMode.value
    }

    fun showAddDialog() {
        _showAddIngredientDialog.value = true
    }

    fun hideAddDialog() {
        _showAddIngredientDialog.value = false
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
        _ingredientToDelete.value = null
    }

    fun setIngredientToDelete(ingredient: IngredientEntity?) {
        _ingredientToDelete.value = ingredient
    }

    fun showEditDialog() {
        _showEditIngredientDialog.value = true
    }

    fun hideEditDialog() {
        _showEditIngredientDialog.value = false
        _ingredientToEdit.value = null
    }

    fun setIngredientToEdit(ingredient: IngredientEntity?) {
        _ingredientToEdit.value = ingredient
    }

    fun setEditName(name: String) {
        _editName.value = name
    }

    fun setEditQuantity(quantity: String) {
        _editQuantity.value = quantity
    }

    fun setIngredientName(name: String) {
        _ingredientName.value = name
    }

    fun setIngredientQuantity(quantity: String) {
        _ingredientQuantity.value = quantity
    }

    fun addIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            ingredientDao.insertIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            ingredientDao.deleteIngredient(ingredient)
        }
    }

    fun updateIngredientQuantity(ingredient: IngredientEntity, newQuantity: Int) {
        viewModelScope.launch {
            val updatedIngredient = ingredient.copy(quantity = newQuantity)
            ingredientDao.updateIngredient(updatedIngredient)
        }
    }

    fun updateIngredient(oldIngredient: IngredientEntity, newIngredient: IngredientEntity) {
        viewModelScope.launch {
            if (oldIngredient.name != newIngredient.name) {
                // If name changed, we need to delete the old and insert new
                ingredientDao.deleteIngredient(oldIngredient)
                ingredientDao.insertIngredient(newIngredient)
            } else {
                ingredientDao.updateIngredient(newIngredient)
            }
        }
    }
}