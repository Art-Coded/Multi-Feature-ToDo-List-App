package com.example.sweetbakes.Home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetbakes.R
import com.example.sweetbakes.SharedViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import java.util.Locale

@Composable
fun DashboardUI(
    sharedViewModel: SharedViewModel = viewModel(),
    onNavigateToOrders: () -> Unit
) {
    val pendingOrderCount by sharedViewModel.pendingOrderCount.collectAsState()
    val highPriorityOrderCount by sharedViewModel.highPriorityOrderCount.collectAsState()
    val checkedOrders by sharedViewModel.checkedOrders.collectAsState()
    val completedOrderCount by sharedViewModel.completedOrderCount.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        sharedViewModel.refreshAllCounts()
    }

    val totalOrders = pendingOrderCount + completedOrderCount
    val progress = if (totalOrders > 0) {
        completedOrderCount.toFloat() / totalOrders.toFloat()
    } else {
        0f
    }

    val currentDate = remember { LocalDate.now() }
    val formattedDate = remember(currentDate) {
        currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(Locale.ENGLISH))
    }

    LaunchedEffect(pendingOrderCount, highPriorityOrderCount, completedOrderCount) {
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "As of $formattedDate, ${LocalContext.current.getString(R.string.dashboard_asof1)} ${(progress * 100).toInt()}${LocalContext.current.getString(R.string.dashboard_asof2)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedCircularProgressBar(progress = progress)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TaskCard(
                        title = "Pending",
                        count = pendingOrderCount,
                        color = Color(0xFFFFB347)
                    )
                    TaskCard(
                        title = "Completed",
                        count = completedOrderCount,
                        color = Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TaskCard(
                        title = "High Priority",
                        count = highPriorityOrderCount,
                        color = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_2),
                        contentDescription = "Sweet Bakes Icon",
                        modifier = Modifier
                            .size(140.dp)
                            .graphicsLayer(scaleX = -1f),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val annotatedText = buildAnnotatedString {
                        append(context.getString(R.string.check_out))
                        append(" ")

                        val startIndex = length
                        append("Orders")
                        val endIndex = length

                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.secondary,
                                textDecoration = TextDecoration.Underline
                            ),
                            start = startIndex,
                            end = endIndex
                        )

                        addStringAnnotation(
                            tag = "Orders",
                            annotation = "navigate_to_orders",
                            start = startIndex,
                            end = endIndex
                        )

                        append(" tab!")
                    }

                    ClickableText(
                        text = annotatedText,
                        style = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "Orders", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    onNavigateToOrders()
                                }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExpandableBox(
            title = context.getString(R.string.view),
            content = {
                if (checkedOrders.isNotEmpty()) {
                    Column {
                        checkedOrders.forEachIndexed { index, order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "${order.title}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "${order.description}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Priority: ${order.priority}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (index < checkedOrders.size - 1) {
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = context.getString(R.string.expanded),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

@Composable
fun ExpandableBox(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Image(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
                    ),
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
fun TaskCard(
    title: String,
    count: Int,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(70.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AnimatedCircularProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Int = 100,
    strokeWidth: Float = 10f,
    progressColor: Color = Color(0xFFFFA500),
    backgroundColor: Color = Color.LightGray
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = animatedProgress * 360f

            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}