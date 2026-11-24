package com.example.food_shelf_life_predictor.features.inventory

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food_shelf_life_predictor.core.storage.ImageStorage
import com.example.food_shelf_life_predictor.core.ui.components.AppBottomBar
import com.example.food_shelf_life_predictor.core.ui.components.AppTab
import com.example.food_shelf_life_predictor.core.ui.theme.AppBackground
import com.example.food_shelf_life_predictor.features.calendar.CalendarContent
import com.example.food_shelf_life_predictor.features.inventory.components.AddFoodOptionsSheet
import com.example.food_shelf_life_predictor.features.inventory.components.AddFoodTypeSheet
import com.example.food_shelf_life_predictor.features.inventory.components.FoodItemCard
import com.example.food_shelf_life_predictor.features.inventory.components.HomeHeader
import com.example.food_shelf_life_predictor.features.inventory.components.LocationFilterRow
import com.example.food_shelf_life_predictor.features.inventory.components.PackagedFoodBottomSheet
import com.example.food_shelf_life_predictor.features.inventory.components.PredictionResultSheet
import com.example.food_shelf_life_predictor.features.inventory.components.PriorityHeader
import com.example.food_shelf_life_predictor.features.inventory.components.ScanLoadingDialog
import com.example.food_shelf_life_predictor.features.inventory.components.SummaryCards
import com.example.food_shelf_life_predictor.features.inventory.scan.ScanState
import com.example.food_shelf_life_predictor.features.inventory.scan.ScanViewModel
import com.example.food_shelf_life_predictor.features.notifications.NotificationsScreen
import com.example.food_shelf_life_predictor.features.notifications.NotificationsViewModel
import com.example.food_shelf_life_predictor.features.profile.ProfileContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun InventoryScreen(
    onLogout: () -> Unit,
    viewModel: InventoryViewModel = viewModel(),
    scanViewModel: ScanViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel()
) {
    val foods by viewModel.visibleFoods.collectAsState()
    val selected by viewModel.selectedLocation.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val allFoods by viewModel.allFoods.collectAsState()
    val scanState by scanViewModel.state.collectAsState()
    val hasUnread by notificationsViewModel.hasUnread.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var tab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showType by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    BackHandler(enabled = showNotifications) { showNotifications = false }

    fun startScan(uri: Uri) {
        scope.launch {
            val path = runCatching {
                withContext(Dispatchers.IO) { ImageStorage.saveCompressed(context, uri) }
            }.getOrNull()
            if (path == null) snackbarHostState.showSnackbar("Gagal membaca foto")
            else scanViewModel.analyze(path)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { startScan(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) startScan(uri)
    }

    LaunchedEffect(scanState) {
        (scanState as? ScanState.Error)?.let {
            snackbarHostState.showSnackbar(it.message)
            scanViewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errors.collect { message ->
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = "Coba lagi")
            if (result == SnackbarResult.ActionPerformed) viewModel.refresh()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomBar(
                selected = tab,
                onTabSelected = { tab = it },
                onCameraClick = { showType = true }
            )
        }
    ) { innerPadding ->
        val pageModifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = innerPadding.calculateBottomPadding())

        when (tab) {
            AppTab.HOME -> LazyColumn(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    HomeHeader(
                        hasUnread = hasUnread,
                        onNotificationClick = { showNotifications = true }
                    )
                }
                item { SummaryCards(total = summary.total, urgent = summary.urgent) }
                item {
                    LocationFilterRow(
                        selected = selected,
                        total = summary.total,
                        counts = summary.countByLocation,
                        onSelect = viewModel::selectLocation
                    )
                }
                item { PriorityHeader() }

                if (foods.isEmpty()) {
                    item {
                        Text(
                            "Belum ada makanan di sini",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    }
                }

                items(foods, key = { it.id }) { item ->
                    FoodItemCard(
                        item = item,
                        onFinished = {
                            viewModel.markFinished(item.id)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "Makanan ditandai habis",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoFinished(item.id)
                                } else {
                                    viewModel.confirmFinished(item.id)
                                }
                            }
                        }
                    )
                }
            }

            AppTab.CALENDAR -> CalendarContent(
                foods = allFoods,
                hasUnread = hasUnread,
                onNotificationClick = { showNotifications = true },
                modifier = pageModifier
            )

            AppTab.PROFILE -> ProfileContent(
                totalItems = summary.total,
                hasUnread = hasUnread,
                onNotificationClick = { showNotifications = true },
                onLogout = onLogout,
                modifier = pageModifier
            )
        }
    }

    if (showNotifications) {
        NotificationsScreen(
            onBack = { showNotifications = false },
            onSchedule = {
                showNotifications = false
                tab = AppTab.CALENDAR
            }
        )
    }

    if (showType) {
        AddFoodTypeSheet(
            onDismiss = { showType = false },
            onFresh = { showType = false; showOptions = true },
            onPackaged = { showType = false; showSheet = true }
        )
    }

    if (showOptions) {
        AddFoodOptionsSheet(
            onDismiss = { showOptions = false },
            onCamera = {
                showOptions = false
                val uri = ImageStorage.createCameraUri(context)
                cameraUri = uri
                cameraLauncher.launch(uri)
            },
            onGallery = {
                showOptions = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    when (val s = scanState) {
        is ScanState.Loading -> ScanLoadingDialog(onCancel = scanViewModel::cancel)
        is ScanState.Result -> PredictionResultSheet(
            state = s,
            onFoodChange = scanViewModel::selectFood,
            onLocationChange = scanViewModel::selectLocation,
            onSave = { scanViewModel.save { item -> viewModel.addFood(item) } },
            onRetake = { scanViewModel.cancel(); showOptions = true },
            onDismiss = scanViewModel::cancel
        )
        else -> Unit
    }

    if (showSheet) {
        PackagedFoodBottomSheet(
            onDismiss = { showSheet = false },
            onSave = { name, location, date ->
                viewModel.addPackagedFood(name, location, date)
                showSheet = false
            }
        )
    }
}