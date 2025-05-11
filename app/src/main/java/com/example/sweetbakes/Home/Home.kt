package com.example.sweetbakes.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetbakes.R
import com.example.sweetbakes.SharedViewModel
import com.example.sweetbakes.data.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun Home(
    sharedViewModel: SharedViewModel,
    onNavigateToOrders: () -> Unit,
    database: AppDatabase
) {
    val tabs = listOf("Dashboard", "Ingredients Storage")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenHeightDp > configuration.screenWidthDp

    val restockViewModel: RestockViewModel = viewModel(
        factory = RestockViewModelFactory(database)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isPortrait) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SweetBakes",
                    modifier = Modifier
                        .padding(start = 8.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
                Image(
                    painter = painterResource(id = R.drawable.applogo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(start = 8.dp)
                        .graphicsLayer(scaleX = -1f)
                )
            }
        }

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> DashboardUI(
                    sharedViewModel = sharedViewModel,
                    onNavigateToOrders = onNavigateToOrders
                )
                1 -> RestockUI(restockViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)
    Home(
        sharedViewModel = SharedViewModel(context),
        onNavigateToOrders = {},
        database = database
    )
}