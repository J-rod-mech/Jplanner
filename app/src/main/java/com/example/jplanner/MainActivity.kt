package com.example.jplanner

import android.R
import android.R.attr.label
import android.R.attr.text
import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jplanner.ui.theme.JplannerTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.OutlinedTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    companion object {
        @JvmField
        val list = MyTasks()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JplannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    //taskList(modifier = Modifier.padding(innerPadding), myTasks = list)
                    editTaskScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

class MyTasks {
    val tasks = mutableStateListOf<Task>()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDatePicker(
    placeholderText: String,
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
) {
    var isDatePickerVisible by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    val selectedDateText = (datePickerState.selectedDateMillis?.plus(TimeUnit.DAYS.toMillis(1)))?.let {
        formatDate(it, "MMM dd, yyyy")
    } ?: ""

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            value = selectedDateText,
            onValueChange = {},
            label = { Text(placeholderText) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isDatePickerVisible = !isDatePickerVisible }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select date")
                }
            }
        )

        if (isDatePickerVisible) {
            DatePickerDialog(
                datePickerState = datePickerState,
                onDateSelected = {
                    onDateSelected(it ?: 0)
                },
                onDismiss = { isDatePickerVisible = false },
                tempDate = selectedDateMillis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    datePickerState: DatePickerState,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    tempDate: Long
) {
    DatePickerDialog(
        onDismissRequest = {
            datePickerState.selectedDateMillis = tempDate
                           },
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                datePickerState.selectedDateMillis = tempDate
            }) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}

fun formatDate(millis: Long, pattern: String = "MM-dd-yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)
    return formatter.format(Date(millis))
}

@Preview
@Composable
fun previewTaskList(modifier: Modifier = Modifier) {
    val myTasks = MyTasks()
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = { testReadFile(myTasks = myTasks) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(paddingValues = innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = myTasks.tasks) { task ->
                Task(name = task.getName(), start = task.getStart(), end = task.getEnd())
            }
        }
    }
}

@Composable
fun taskList(modifier: Modifier = Modifier, myTasks: MyTasks) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = { readFile(myTasks = myTasks) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(paddingValues = innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = myTasks.tasks) { task ->
                Task(name = task.getName(), start = task.getStart(), end = task.getEnd())
            }
        }
    }
}

@Composable
fun testReadFile(modifier: Modifier = Modifier, myTasks: MyTasks) {
    Surface(color = MaterialTheme.colorScheme.secondary) {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    myTasks.tasks.add(Planner.testFunc1("best"))
                    myTasks.tasks.add(Planner.testFunc1("test"))
                    myTasks.tasks.add(Planner.testFunc1("rest"))
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("fetch schedule")
            }
        }
    }
}
@Composable
fun readFile(modifier: Modifier = Modifier, myTasks: MyTasks) {
    Surface(color = MaterialTheme.colorScheme.secondary) {
        val context = LocalContext.current
        Button(
            modifier = Modifier.background(MaterialTheme.colorScheme.secondary),
            onClick = {
                FileManager.readFile(context)
            }
        ) {
            Text("fetch schedule")
        }
    }
}

/*
@Composable
fun addTask(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        IconButton(

        ) {
            Icon(

            )
        }
    }
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun editTaskScreen(modifier: Modifier = Modifier) {
    Surface() {
        var name by remember { mutableStateOf("") }
        var startHour by remember { mutableStateOf(12) }
        var startMin by remember { mutableStateOf(0) }
        var startXM by remember { mutableStateOf("AM") }
        var endHour by remember { mutableStateOf(12) }
        var endMin by remember { mutableStateOf(0) }
        var endXM by remember { mutableStateOf("AM") }
        var date by remember { mutableStateOf(0L) }
        Column {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name:") },
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(
                        text = "Start:",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfiniteCircularList(
                            width = 50.dp,
                            itemHeight = 40.dp,
                            numberOfDisplayedItems = 3,
                            items = (1..12).toMutableList(),
                            initialItem = startHour,
                            textStyle = MaterialTheme.typography.headlineSmall,
                            textColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            onItemSelected = { i, item ->
                                startHour = item
                            }
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .height(40.dp)
                        )
                        InfiniteCircularList(
                            width = 50.dp,
                            itemHeight = 40.dp,
                            numberOfDisplayedItems = 3,
                            items = (0..59).toMutableList(),
                            initialItem = startMin,
                            textStyle = MaterialTheme.typography.headlineSmall,
                            textColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            onItemSelected = { i, item ->
                                startMin = item
                            }
                        )
                        InfiniteCircularList(
                            width = 50.dp,
                            itemHeight = 30.dp,
                            numberOfDisplayedItems = 3,
                            items = listOf("", "AM", "PM", ""),
                            initialItem = startXM,
                            itemScaleFact = 1f,
                            textStyle = MaterialTheme.typography.headlineSmall,
                            textColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            onItemSelected = { i, item ->
                                startXM = item
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier
                    .weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(
                        text = "End:",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfiniteCircularList(
                            width = 50.dp,
                            itemHeight = 40.dp,
                            numberOfDisplayedItems = 3,
                            items = (1..12).toMutableList(),
                            initialItem = endHour,
                            textStyle = MaterialTheme.typography.headlineSmall,
                            textColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            onItemSelected = { i, item ->
                                endHour = item
                            }
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .height(40.dp)
                        )
                        InfiniteCircularList(
                            width = 50.dp,
                            itemHeight = 40.dp,
                            numberOfDisplayedItems = 3,
                            items = (0..59).toMutableList(),
                            initialItem = endMin,
                            textStyle = MaterialTheme.typography.headlineSmall,
                            textColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            onItemSelected = { i, item ->
                                endMin = item
                            }
                        )
                        InfiniteCircularList(
                            width = 50.dp,
                            itemHeight = 30.dp,
                            numberOfDisplayedItems = 3,
                            items = listOf("", "AM", "PM", ""),
                            initialItem = endXM,
                            itemScaleFact = 1f,
                            textStyle = MaterialTheme.typography.headlineSmall,
                            textColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            onItemSelected = { i, item ->
                                endXM = item
                            }
                        )
                    }
                }
            }

            val todayMillis = LocalDate.now()
                .atStartOfDay(ZoneId.of(Planner.timeZone))
                .toInstant()
                .toEpochMilli()
            val selectedDate = remember { mutableLongStateOf(todayMillis) }

            MaterialDatePicker(
                placeholderText = "Date",
                selectedDateMillis = selectedDate.value,
                onDateSelected = { date ->
                    selectedDate.value = date
                },
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> InfiniteCircularList(
    width: Dp,
    itemHeight: Dp,
    numberOfDisplayedItems: Int = 3,
    items: List<T>,
    initialItem: T,
    itemScaleFact: Float = 1.5f,
    textStyle: TextStyle,
    textColor: Color,
    selectedTextColor: Color,
    onItemSelected: (index: Int, item: T) -> Unit = { _, _ -> }
) {
    val itemHalfHeight = LocalDensity.current.run { itemHeight.toPx() / 2f }
    val scrollState = rememberLazyListState(0)
    var lastSelectedIndex by remember {
        mutableStateOf(0)
    }
    var itemsState by remember {
        mutableStateOf(items)
    }
    LaunchedEffect(items) {
        var targetIndex = items.indexOf(initialItem) - 1
        targetIndex += ((Int.MAX_VALUE / 2) / items.size) * items.size
        itemsState = items
        lastSelectedIndex = targetIndex
        scrollState.scrollToItem(targetIndex)
    }
    LazyColumn(
        modifier = Modifier
            .width(width)
            .height(itemHeight * numberOfDisplayedItems),
        state = scrollState,
        flingBehavior = rememberSnapFlingBehavior(
            lazyListState = scrollState
        )
    ) {
        items(
            count = if (items[0] is String) itemsState.size else Int.MAX_VALUE,
            itemContent = { i ->
                val item = itemsState[i % itemsState.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeight
                            val parentHalfHeight = (itemHalfHeight * numberOfDisplayedItems)
                            val isSelected =
                                (y > parentHalfHeight - itemHalfHeight && y < parentHalfHeight + itemHalfHeight)
                            val index = i - 1
                            if (isSelected && lastSelectedIndex != index) {
                                onItemSelected(index % itemsState.size, item)
                                lastSelectedIndex = index
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item is String) item else String.format("%02d", item),
                        style = textStyle,
                        color = if (lastSelectedIndex == i) {
                            selectedTextColor
                        } else {
                            textColor
                        },
                        fontSize = if (lastSelectedIndex == i) {
                            textStyle.fontSize * itemScaleFact
                        } else {
                            textStyle.fontSize
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun Task(modifier: Modifier = Modifier, name: String, start: Int, end: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = Planner.convertNumToTime(start, end),
                modifier = Modifier.padding(end = 4.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}