package com.example.sweetbakes

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.example.sweetbakes.Home.Home
import com.example.sweetbakes.Settings.SettingsScreen
import com.example.sweetbakes.content.BottomSheetContent
import com.example.sweetbakes.content.ToDoContent
import com.example.sweetbakes.data.AppDatabase
import com.example.sweetbakes.model.OrdersItem
import com.example.sweetbakes.model.SettingsEntity
import com.example.sweetbakes.ui.OrdersUI
import com.example.sweetbakes.ui.theme.SweetBakesTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.onAttach(this)
        enableEdgeToEdge()
        setContent {
            val application = LocalContext.current.applicationContext as SweetBakesApplication
            val database = application.database
            val coroutineScope = rememberCoroutineScope()

            val activityContext = remember { this }

            var isDarkMode by remember { mutableStateOf(false) }
            var initialized by remember { mutableStateOf(false) }
            var selectedLanguage by rememberSaveable { mutableStateOf("English") }
            val context = LocalContext.current

            val localizedContext = remember(selectedLanguage) {
                LocaleHelper.setLocale(context, selectedLanguage)
            }

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted -> }

            LaunchedEffect(Unit) {
                isDarkMode = database.settingsDao().getDarkMode() ?: false
                selectedLanguage = database.settingsDao().getLanguage() ?: "English"
                initialized = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            if (initialized) {
                SweetBakesTheme(darkTheme = isDarkMode) {
                    CompositionLocalProvider(LocalContext provides localizedContext) {
                        MainScreen(
                            isDarkMode = isDarkMode,
                            onDarkModeToggle = { newValue ->
                                isDarkMode = newValue
                                coroutineScope.launch {
                                    database.settingsDao().saveSettings(
                                        SettingsEntity(
                                            isDarkMode = newValue,
                                            language = selectedLanguage
                                        )
                                    )
                                }
                            },
                            selectedLanguage = selectedLanguage,
                            onLanguageChange = { newLanguage ->
                                selectedLanguage = newLanguage
                                coroutineScope.launch {
                                    database.settingsDao().saveSettings(
                                        SettingsEntity(
                                            isDarkMode = isDarkMode,
                                            language = newLanguage
                                        )
                                    )
                                }
                            },
                            database = database,
                            localizedContext = localizedContext,
                            activityContext = activityContext
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    database: AppDatabase,
    localizedContext: Context,
    activityContext: Context
) {
    fun getRawResId(fileName: String): Int {
        return when (fileName) {
            "home_dark" -> R.raw.home_dark
            "home_light" -> R.raw.home_light
            "clock_dark" -> R.raw.clock_dark
            "clock_light" -> R.raw.clock_light
            "article_icon_dark" -> R.raw.article_icon_dark
            "article_icon_light" -> R.raw.article_icon_light
            "settings_dark" -> R.raw.settings_dark
            "settings_light" -> R.raw.settings_light
            else -> R.raw.home_dark
        }
    }

    @Composable
    fun BottomNavItem(
        iconFileName: String,
        label: String,
        isSelected: Boolean,
        isDarkMode: Boolean,
        badgeCount: Int? = null,
        onClick: () -> Unit
    ) {
        val themedIconFileName = if (isDarkMode) "${iconFileName}_light" else "${iconFileName}_dark"
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(
                getRawResId(
                    themedIconFileName
                )
            )
        )
        val lottieAnimState = rememberLottieAnimatable()
        val coroutineScope = rememberCoroutineScope()

        val iconAlpha by animateFloatAsState(
            targetValue = if (isSelected) 1f else 0.6f,
            animationSpec = tween(durationMillis = 300)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .clickable(
                    onClick = {
                        onClick()
                        coroutineScope.launch {
                            lottieAnimState.animate(composition)
                        }
                    },
                    indication = LocalIndication.current,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (badgeCount != null && badgeCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = Color.Red) {
                                Text(badgeCount.toString(), color = Color.White)
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { lottieAnimState.progress },
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(alpha = iconAlpha)
                        )
                    }
                } else {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieAnimState.progress },
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(alpha = iconAlpha)
                    )
                }
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = iconAlpha),
                    fontSize = 12.sp
                )
            }
        }
    }

    var selectedItem by rememberSaveable { mutableStateOf(0) }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    var orderList by rememberSaveable { mutableStateOf(emptyList<OrdersItem>()) }
    var showEditPopup by rememberSaveable { mutableStateOf(false) }
    var editingOrder by rememberSaveable { mutableStateOf<OrdersItem?>(null) }

    var editTitle by rememberSaveable { mutableStateOf("") }
    var editDescription by rememberSaveable { mutableStateOf("") }
    var editPriority by rememberSaveable { mutableStateOf("Medium") }
    var editDueDate by rememberSaveable { mutableStateOf("") }
    var editReminders by rememberSaveable { mutableStateOf("") }

    val sharedViewModel: SharedViewModel = viewModel(
        factory = SharedViewModelFactory(localizedContext)
    )

    val pendingOrderCount by sharedViewModel.pendingOrderCount.collectAsState()

    LaunchedEffect(Unit) {
        sharedViewModel.getAllOrders().collect { orders ->
            orderList = orders
            sharedViewModel.refreshAllCounts()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            "home",
                            "Home",
                            isSelected = selectedItem == 0,
                            isDarkMode = isDarkMode
                        ) { selectedItem = 0 }
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            iconFileName = "clock",
                            label = "Orders",
                            isSelected = selectedItem == 1,
                            isDarkMode = isDarkMode,
                            badgeCount = pendingOrderCount,
                            onClick = { selectedItem = 1 }
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        FloatingActionButton(
                            onClick = { showBottomSheet = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Text("+", color = Color.White)
                        }
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            "article_icon",
                            "To Do",
                            isSelected = selectedItem == 2,
                            isDarkMode = isDarkMode
                        ) { selectedItem = 2 }
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            "settings",
                            "Settings",
                            isSelected = selectedItem == 3,
                            isDarkMode = isDarkMode
                        ) { selectedItem = 3 }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (selectedItem) {
                0 -> Home(
                    sharedViewModel = sharedViewModel,
                    onNavigateToOrders = { selectedItem = 1 },
                    database = database
                )

                1 -> OrdersUI(
                    ordersList = orderList,
                    onDeleteOrder = { orderToRemove ->
                        coroutineScope.launch {
                            sharedViewModel.deleteOrder(orderToRemove)
                            orderList = orderList.filterNot { it.id == orderToRemove.id }
                            sharedViewModel.removeCheckedOrder(orderToRemove)
                            sharedViewModel.refreshAllCounts()
                        }
                    },
                    onEditOrder = { orderToEdit ->
                        editingOrder = orderToEdit
                        editTitle = orderToEdit.title
                        editDescription = orderToEdit.description
                        editPriority = orderToEdit.priority
                        editDueDate = orderToEdit.dueDate
                        editReminders = orderToEdit.reminders
                        showEditPopup = true
                    },
                    showEditPopup = showEditPopup,
                    onDismissEditPopup = { showEditPopup = false },
                    editTitle = editTitle,
                    onEditTitleChange = { editTitle = it },
                    editDescription = editDescription,
                    onEditDescriptionChange = { editDescription = it },
                    editPriority = editPriority,
                    onEditPriorityChange = { editPriority = it },
                    editDueDate = editDueDate,
                    onEditDueDateChange = { editDueDate = it },
                    editReminders = editReminders,
                    onEditRemindersChange = { editReminders = it },
                    onSaveEdit = {
                        editingOrder?.let { originalOrder ->
                            coroutineScope.launch {
                                val updatedOrder = originalOrder.copy(
                                    title = editTitle,
                                    description = editDescription,
                                    priority = editPriority,
                                    dueDate = editDueDate,
                                    reminders = editReminders
                                )
                                sharedViewModel.updateOrder(updatedOrder)
                                orderList = orderList.map { order ->
                                    if (order.id == originalOrder.id) updatedOrder else order
                                }
                                val wasChecked =
                                    sharedViewModel.checkedStates.value[originalOrder.id] ?: false
                                sharedViewModel.removeCheckedOrder(originalOrder)
                                if (wasChecked) {
                                    sharedViewModel.addCheckedOrder(updatedOrder)
                                }
                                sharedViewModel.refreshAllCounts()
                            }
                        }
                        showEditPopup = false
                    },
                    onDateClick = {},
                    onTimeClick = {},
                    sharedViewModel = sharedViewModel
                )

                2 -> ToDoContent(
                    context = activityContext,
                    localizedContext = localizedContext
                )
                3 -> SettingsScreen(
                    isDarkMode = isDarkMode,
                    onDarkModeToggle = onDarkModeToggle,
                    selectedLanguage = selectedLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                CompositionLocalProvider(LocalContext provides localizedContext) {
                    BottomSheetContent(
                        context = activityContext,
                        localizedContext = localizedContext,
                        existingOrders = orderList,
                        onOrderAdded = { newOrder ->
                            coroutineScope.launch {
                                sharedViewModel.saveOrder(newOrder)
                                orderList = orderList + newOrder
                                sharedViewModel.refreshAllCounts()
                                showBottomSheet = false
                            }
                        }
                    )
                }
            }
        }
    }
}