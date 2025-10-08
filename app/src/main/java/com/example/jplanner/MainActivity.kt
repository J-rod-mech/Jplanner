package com.example.jplanner

import com.example.jplanner.R
import android.R.attr.label
import android.R.attr.text
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.jplanner.Planner.HALF_HOURS
import com.example.jplanner.Planner.HOUR_DIV
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput

var editName = mutableStateOf("")

var editStart = mutableStateOf(0)
var editStartHour = mutableStateOf(12)
var editStartMin = mutableStateOf(0)
var editStartXM = mutableStateOf("AM")
var editEnd = mutableStateOf(60)
var editEndHour = mutableStateOf(1)
var editEndMin = mutableStateOf(0)
var editEndXM = mutableStateOf("AM")
var editNote = mutableStateOf("")
var editMode = mutableStateOf(false)

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
                    MyApp(
                        modifier = Modifier.padding(innerPadding),
                        myTasks = list
                    )
                }
            }
        }
    }
}

class MyTasks {
    val tasks = mutableStateListOf<Task>()
}

@Composable
fun MyApp(modifier: Modifier = Modifier, myTasks: MyTasks) {
    var showAddTaskScreen by remember { mutableStateOf(false) }
    if (showAddTaskScreen) {
        EditTaskScreen(
            onContinueClicked = {
                showAddTaskScreen = false
            }
        )
    }
    else if (editMode.value) {
        EditTaskScreen(
            newName = editName.value,
            newStartHour = editStartHour.value,
            newStartMin = editStartMin.value,
            newStartXM = editStartXM.value,
            newEndHour = editEndHour.value,
            newEndMin = editEndMin.value,
            newEndXM = editEndXM.value,
            newNote = editNote.value,
            onContinueClicked = {
                editMode.value = false
            }
        )
    }
    else {
        taskList(
            myTasks = myTasks,
            onAddClicked = {
                showAddTaskScreen = true
            }
        )
    }
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
                .height(76.dp)
                .padding(8.dp)
            ,
            value = selectedDateText,
            onValueChange = {},
            label = { Text(placeholderText) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isDatePickerVisible = !isDatePickerVisible }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select a date for your task:")
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
            state = datePickerState
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
                Task(task = task)
            }
        }
    }
}

@Composable
fun taskList(modifier:
             Modifier = Modifier,
             myTasks: MyTasks,
             onAddClicked: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = { readFile(
            myTasks = myTasks,
            onContinueClicked = onAddClicked
        )}
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(paddingValues = innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = myTasks.tasks) { task ->
                Task(task = task)
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
fun readFile(
    modifier: Modifier = Modifier,
    myTasks: MyTasks,
    onContinueClicked: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.secondary) {
        val context = LocalContext.current
        Row {
            Button(
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary),
                onClick = {
                    FileManager.readFile(context)
                }
            ) {
                Text("fetch schedule")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary),
                onClick = onContinueClicked
            ) {
                Text("add task")
            }
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
@Composable
fun EditTaskScreen(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit,
    newName: String = "",
    newStartHour: Int = 12,
    newStartMin: Int = 0,
    newStartXM: String = "AM",
    newEndHour: Int = if (newStartHour < 12) (if (newStartHour == 11 && newStartXM == "PM") 11 else newStartHour + 1) else 1,
    newEndMin: Int = if (newStartHour == 11 && newStartXM == "PM") 59 else newStartMin,
    newEndXM: String = if (newStartHour != 11) newStartXM else "PM",
    newNote: String = ""
    ) {
    Surface() {
        val context = LocalContext.current
        var name by remember { mutableStateOf(newName) }
        name = if (name.replace("_", " ").trim() == "") name else name.replace("_", " ")
        var startHour by remember { mutableStateOf(newStartHour) }
        var startMin by remember { mutableStateOf(newStartMin) }
        var startXM by remember { mutableStateOf(newStartXM) }
        var endHour by remember { mutableStateOf(newEndHour) }
        var endMin by remember { mutableStateOf(newEndMin) }
        var endXM by remember { mutableStateOf(newEndXM) }
        var note by remember { mutableStateOf(newNote) }
        var nameError by remember { mutableStateOf(false) }
        var nameDelimError by remember { mutableStateOf(false) }
        var timeError by remember { mutableStateOf(false) }
        var noteDelimError by remember { mutableStateOf(false) }
        var nameEdit by remember { mutableStateOf(name != "") }
        var canAdd by remember { mutableStateOf(false) }
        var confirmDelete by remember { mutableStateOf(false) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(16.dp))
            Text(
                text = if (editMode.value) "Editing ${editName.value}:" else "Add New Task:",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                value = name,
                onValueChange = {
                    name = it
                    nameError = name.trim() == ""
                    nameDelimError = name.contains(Planner.DELIM)
                    nameEdit = true
                    canAdd = !(nameError || nameDelimError || timeError || noteDelimError)
                                },
                label = { Text("Name") },
            )
            if (nameError) {
                Text(
                    text = "Name must be filled.",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            else if (nameDelimError) {
                Text(
                    text = "Name cannot include ${Planner.DELIM}.",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Spacer(modifier = Modifier
                .width(8.dp)
                .padding(top = 12.dp)
            )
            Row {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .padding(top = 12.dp, start = 12.dp, end = 6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        text = "Start",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
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
                                val start: Int = (if (startHour < 12) startHour * 60 else 0) + startMin + (if (startXM == "PM") 720 else 0)
                                val end: Int = (if (endHour < 12) endHour * 60 else 0) + endMin + (if (endXM == "PM") 720 else 0)
                                timeError = start >= end
                                canAdd = !(nameError || nameDelimError || timeError || noteDelimError) && nameEdit
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
                                val start: Int = (if (startHour < 12) startHour * 60 else 0) + startMin + (if (startXM == "PM") 720 else 0)
                                val end: Int = (if (endHour < 12) endHour * 60 else 0) + endMin + (if (endXM == "PM") 720 else 0)
                                timeError = start >= end
                                canAdd = !(nameError || nameDelimError || timeError || noteDelimError) && nameEdit
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularList(
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
                                val start: Int = (if (startHour < 12) startHour * 60 else 0) + startMin + (if (startXM == "PM") 720 else 0)
                                val end: Int = (if (endHour < 12) endHour * 60 else 0) + endMin + (if (endXM == "PM") 720 else 0)
                                timeError = start >= end
                                canAdd = !(nameError || nameDelimError || timeError || noteDelimError) && nameEdit
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier
                    .weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .padding(top = 12.dp, start = 6.dp, end = 12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        text = "End",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
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
                                val start: Int = (if (startHour < 12) startHour * 60 else 0) + startMin + (if (startXM == "PM") 720 else 0)
                                val end: Int = (if (endHour < 12) endHour * 60 else 0) + endMin + (if (endXM == "PM") 720 else 0)
                                timeError = start >= end
                                canAdd = !(nameError || nameDelimError || timeError || noteDelimError) && nameEdit
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
                                val start: Int = (if (startHour < 12) startHour * 60 else 0) + startMin + (if (startXM == "PM") 720 else 0)
                                val end: Int = (if (endHour < 12) endHour * 60 else 0) + endMin + (if (endXM == "PM") 720 else 0)
                                timeError = start >= end
                                canAdd = !(nameError || nameDelimError || timeError || noteDelimError) && nameEdit
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularList(
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
                                val start: Int = (if (startHour < 12) startHour * 60 else 0) + startMin + (if (startXM == "PM") 720 else 0)
                                val end: Int = (if (endHour < 12) endHour * 60 else 0) + endMin + (if (endXM == "PM") 720 else 0)
                                timeError = start >= end
                                canAdd = !(nameError || nameDelimError || timeError || noteDelimError) && nameEdit
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            if (timeError) {
                Text(
                    text = "Start time must be earlier than end time.",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Spacer(modifier = Modifier.padding(bottom = 12.dp))

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
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(start = 8.dp, end = 8.dp, top = 12.dp),
                value = note,
                onValueChange = {
                    note = it
                    noteDelimError = note.contains(Planner.DELIM)
                    canAdd = !(nameError || nameDelimError || timeError || noteDelimError) || nameEdit
                                },
                label = { Text("Note") }
            )
            if (noteDelimError) {
                Text(
                    text = "Note cannot include ${Planner.DELIM}.",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.padding(8.dp)) {
                if (editMode.value) {
                    IconButton(
                        onClick = {
                            confirmDelete = true
                        },
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.trash_icon),
                            contentDescription = "trash delete icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onContinueClicked,
                    modifier = Modifier
                        .background(
                            color = Color.LightGray,
                            shape = RoundedCornerShape(25.dp)
                        )
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val start: Int =
                            (if (startHour < 12) startHour * 60 else 0) + startMin + (if (startXM == "PM") 720 else 0)
                        val end: Int =
                            (if (endHour < 12) endHour * 60 else 0) + endMin + (if (endXM == "PM") 720 else 0)
                        if (canAdd) {
                            val fName = name.trim().replace(" ", "_")
                            val fNote = if (note == "") " " else note
                            if (editMode.value) {
                                Planner.replaceTask(Planner.getTaskIdx(editName.value, editStart.value, editEnd.value), fName, " ", start, end, fNote)
                            }
                            else {
                                Planner.insertTask(Planner.newTask(fName, " ", start, end, fNote))
                            }
                            FileManager.writeFile(context)
                            onContinueClicked()
                        }
                        else {
                            nameError = name.trim() == ""
                            nameDelimError = name.contains(Planner.DELIM)
                            timeError = start >= end
                            noteDelimError = note.contains(Planner.DELIM)
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (canAdd) 1f else 0.5f),
                            shape = RoundedCornerShape(25.dp)
                        )
                ) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = if (canAdd) 1f else 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (confirmDelete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        // consume all pointer events so nothing under this box receives them
                        awaitPointerEventScope {
                            while (true) {
                                val evt = awaitPointerEvent()
                                evt.changes.forEach { it.consume() }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                deleteDialog(
                    onContinueClicked = { confirmDelete = false }
                )
            }
        }
    }
}

@Composable
fun deleteDialog(
    onContinueClicked: () -> Unit
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = "Delete ${editName.value}?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 18.dp)
        )
        Row() {
            TextButton(
                onClick = onContinueClicked,
                modifier = Modifier
                    .background(
                        color = Color.LightGray,
                        shape = RoundedCornerShape(25.dp)
                    )
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    Planner.tasks.removeAt(
                        Planner.getTaskIdx(
                            editName.value,
                            editStart.value,
                            editEnd.value
                        )
                    )
                    FileManager.writeFile(context)
                    onContinueClicked()
                    editMode.value = false
                          },
                modifier = Modifier
                    .background(
                        color = Color.Red,
                        shape = RoundedCornerShape(25.dp)
                    )
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
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
            count = Int.MAX_VALUE,
            itemContent = { i ->
                val index = i - 1
                val item = itemsState[(itemsState.size + index + 1) % itemsState.size]
                val REALitem = itemsState[(itemsState.size + index) % itemsState.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeight
                            val parentHalfHeight = (itemHalfHeight * numberOfDisplayedItems)
                            val isSelected =
                                (y > parentHalfHeight - itemHalfHeight && y < parentHalfHeight + itemHalfHeight)

                            if (isSelected && lastSelectedIndex != index) {
                                onItemSelected(index % itemsState.size, REALitem)
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

@Preview(showBackground = true)
@Composable
fun previewCircularList() {
    var initialItem = "AM"
    var test by remember { mutableStateOf(initialItem) }
    var items = listOf("", "AM", "PM", "")

    CircularList(
        width = 50.dp,
        itemHeight = 30.dp,
        numberOfDisplayedItems = 3,
        items = items,
        initialItem = initialItem,
        itemScaleFact = 1f,
        textStyle = MaterialTheme.typography.headlineSmall,
        textColor = MaterialTheme.colorScheme.secondary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        onItemSelected = { i, item ->
            test = item
        }
    )
    Text(
        text = test.toString()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> CircularList(
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
    var targetIndex = items.indexOf(initialItem) - 1
    LaunchedEffect(items) {
        var targetIndex2 = targetIndex + ((Int.MAX_VALUE / 2) / items.size) * items.size
        itemsState = items
        lastSelectedIndex = targetIndex2
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
            count = itemsState.size,
            itemContent = { i ->
                val index = i - 1
                val item = itemsState[(itemsState.size + index + 1) % itemsState.size]
                val REALitem = itemsState[(itemsState.size + index) % itemsState.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeight
                            val parentHalfHeight = (itemHalfHeight * numberOfDisplayedItems)
                            val isSelected =
                                (y > parentHalfHeight - itemHalfHeight && y < parentHalfHeight + itemHalfHeight)
                            if (isSelected && lastSelectedIndex != index) {
                                onItemSelected(index % itemsState.size, REALitem)
                                lastSelectedIndex = index
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
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
fun Task(modifier: Modifier = Modifier, task: Task) {
    Button (
        onClick = {
            editName.value = task.name
            editStart.value = task.start
            editStartHour.value = (task.start % (HALF_HOURS * HOUR_DIV)) / HOUR_DIV
            editStartHour.value = if (editStartHour.value > 0) editStartHour.value else 12
            editStartMin.value = task.start % HOUR_DIV
            editStartXM.value = if (task.start >= HALF_HOURS * HOUR_DIV) "PM" else "AM"
            editEnd.value = task.end
            editEndHour.value = (task.end % (HALF_HOURS * HOUR_DIV)) / HOUR_DIV
            editEndHour.value = if (editEndHour.value > 0) editEndHour.value else 12
            editEndMin.value = task.end % HOUR_DIV
            editEndXM.value = if (task.end >= HALF_HOURS * HOUR_DIV) "PM" else "AM"
            editNote.value = task.note
            editMode.value = true
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = if (task.name.replace("_", " ").trim() == "") task.name else task.name.replace("_", " "),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = Planner.convertNumToTime(task.start) + "-" + Planner.convertNumToTime(task.end),
                modifier = Modifier.padding(end = 4.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}