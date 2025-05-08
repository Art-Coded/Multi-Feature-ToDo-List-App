package com.example.sweetbakes.Home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import com.example.sweetbakes.R
import com.example.sweetbakes.data.IngredientEntity

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RestockUI(viewModel: RestockViewModel) {
    val infiniteTransition = rememberInfiniteTransition()
    val shakeAnimation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val context = LocalContext.current
    val ingredients by viewModel.ingredients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showEditMode by viewModel.showEditMode.collectAsState()
    val showAddIngredientDialog by viewModel.showAddIngredientDialog.collectAsState()
    val ingredientName by viewModel.ingredientName.collectAsState()
    val ingredientQuantity by viewModel.ingredientQuantity.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val ingredientToDelete by viewModel.ingredientToDelete.collectAsState()
    val showEditIngredientDialog by viewModel.showEditIngredientDialog.collectAsState()
    val ingredientToEdit by viewModel.ingredientToEdit.collectAsState()
    val editName by viewModel.editName.collectAsState()
    val editQuantity by viewModel.editQuantity.collectAsState()

    val filteredIngredients = remember(ingredients, searchQuery) {
        if (searchQuery.isBlank()) {
            ingredients
        } else {
            ingredients.filter { ingredient ->
                ingredient.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(context.getString(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                modifier = Modifier.weight(1f).height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
            ) {
                IconButton(
                    onClick = { viewModel.toggleEditMode() },
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            rotationZ = if (showEditMode) shakeAnimation else 0f
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = if (showEditMode) "Disable Edit Mode" else "Enable Edit Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.showAddDialog() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Ingredient",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = context.getString(R.string.name),
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = context.getString(R.string.quantity),
                modifier = Modifier.weight(1f).padding(end = 55.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (filteredIngredients.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            "${context.getString(R.string.not_found)} '$searchQuery'"
                        else context.getString(R.string.no_ingredients),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(filteredIngredients, key = { it.name }) { ingredient ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart) {
                                viewModel.setIngredientToDelete(ingredient)
                                viewModel.showDeleteDialog()
                                false
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart),
                        dismissThresholds = { FractionalThreshold(0.5f) },
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Red)
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    ) {
                        IngredientItem(
                            ingredient = ingredient,
                            onQuantityChange = { newQuantity ->
                                viewModel.updateIngredientQuantity(ingredient, newQuantity)
                            },
                            showEditIcon = showEditMode,
                            onEditClick = {
                                viewModel.setIngredientToEdit(ingredient)
                                viewModel.setEditName(ingredient.name)
                                viewModel.setEditQuantity(ingredient.quantity.toString())
                                viewModel.showEditDialog()
                            }
                        )
                    }

                    if (ingredient != filteredIngredients.last()) {
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showAddIngredientDialog) {
            AddEditIngredientDialog(
                title = context.getString(R.string.add_ingredient),
                name = ingredientName,
                onNameChange = { if (it.length <= 18) viewModel.setIngredientName(it) },
                quantity = ingredientQuantity,
                onQuantityChange = { viewModel.setIngredientQuantity(it) },
                onDismiss = { viewModel.hideAddDialog() },
                onSave = {
                    val quantity = ingredientQuantity.toIntOrNull() ?: 0
                    if (ingredientName.isNotBlank() && quantity > 0 && ingredientName.length <= 18) {
                        viewModel.addIngredient(
                            IngredientEntity(
                                name = ingredientName,
                                quantity = quantity
                            )
                        )
                        viewModel.setIngredientName("")
                        viewModel.setIngredientQuantity("0")
                        viewModel.hideAddDialog()
                    }
                }
            )
        }

        if (showEditIngredientDialog && ingredientToEdit != null) {
            AddEditIngredientDialog(
                title = "Edit Ingredient",
                name = editName,
                onNameChange = { if (it.length <= 18) viewModel.setEditName(it) },
                quantity = editQuantity,
                onQuantityChange = { viewModel.setEditQuantity(it) },
                onDismiss = { viewModel.hideEditDialog() },
                onSave = {
                    val quantity = editQuantity.toIntOrNull() ?: 0
                    if (editName.isNotBlank() && quantity >= 0 && editName.length <= 18) {
                        viewModel.updateIngredient(
                            oldIngredient = ingredientToEdit!!,
                            newIngredient = IngredientEntity(
                                name = editName,
                                quantity = quantity
                            )
                        )
                        viewModel.hideEditDialog()
                    }
                }
            )
        }

        if (showDeleteDialog && ingredientToDelete != null) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteDialog() },
                title = { Text(text = context.getString(R.string.confirm_deletion)) },
                text = { Text(context.getString(R.string.delete_confirmation, ingredientToDelete?.name ?: "?")) },
                confirmButton = {
                    Button(
                        onClick = {
                            ingredientToDelete?.let { viewModel.deleteIngredient(it) }
                            viewModel.hideDeleteDialog()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AddEditIngredientDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(LocalContext.current.getString(R.string.ingredient_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    isError = name.length > 18,
                    supportingText = {
                        if (name.length > 18) {
                            Text(
                                text = LocalContext.current.getString(R.string.title_characters),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "${name.length}/18",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                )

                QuantityInput(
                    quantity = quantity,
                    onQuantityChange = onQuantityChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = name.isNotBlank() && name.length <= 18 && quantity.toIntOrNull() != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: IngredientEntity,
    onQuantityChange: (Int) -> Unit,
    showEditIcon: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (showEditIcon) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Ingredient",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = ingredient.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (ingredient.quantity > 0) {
                            onQuantityChange(ingredient.quantity - 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Quantity",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${ingredient.quantity}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = {
                        onQuantityChange(ingredient.quantity + 1)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Quantity",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityInput(
    quantity: String,
    onQuantityChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val currentQuantity = quantity.toIntOrNull() ?: 0
                if (currentQuantity > 0) {
                    onQuantityChange((currentQuantity - 1).toString())
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease Quantity",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedTextField(
            value = quantity,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                    onQuantityChange(newValue)
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            label = { Text("Quantity") }
        )

        IconButton(
            onClick = {
                val currentQuantity = quantity.toIntOrNull() ?: 0
                onQuantityChange((currentQuantity + 1).toString())
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase Quantity",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}