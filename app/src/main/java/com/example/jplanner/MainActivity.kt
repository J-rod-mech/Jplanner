package com.example.jplanner

import com.example.jplanner.R
import android.R.attr.label
import android.R.attr.text
import android.os.Bundle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jplanner.ui.theme.JplannerTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.jplanner.Planner.HALF_HOURS
import com.example.jplanner.Planner.HOUR_DIV
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import androidx.compose.material3.RadioButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import androidx.core.net.toUri
import androidx.core.content.edit

var root: DocumentFile? = null
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

var workingDate = mutableStateOf(Planner.zonedDate)
var currDate = mutableStateOf(Planner.zonedDate)

val todayMillis = LocalDate.now()
    .atStartOfDay(ZoneId.of(Planner.timeZone))
    .toInstant()
    .toEpochMilli()
val selectedDate = mutableLongStateOf(todayMillis)
val selectedView = mutableStateOf("Daily")

class MainActivity : ComponentActivity() {
    companion object {
        @JvmField
        val list = MyTasks()
    }

    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                grantLongTermAccess(uri)

                root = DocumentFile.fromTreeUri(this, uri)
                Log.d("SAF", "Root set from picker: $root")
            }
        }

    fun openFolderPicker() {
        folderPicker.launch(null)
    }

    private fun grantLongTermAccess(treeUri: Uri) {
        val flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        contentResolver.takePersistableUriPermission(treeUri, flags)

        val prefs = getSharedPreferences("storage_prefs", MODE_PRIVATE)
        prefs.edit { putString("tree_uri", treeUri.toString()) }
    }

    private fun loadRootFromPrefs() {
        val prefs = getSharedPreferences("storage_prefs", MODE_PRIVATE)
        val uriString = prefs.getString("tree_uri", null)

        if (uriString == null) {
            Log.d("SAF", "No saved URI")
            root = null
            return
        }

        val treeUri = uriString.toUri()

        // Must re-take permissions or Android may block access
        try {
            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: Exception) {
            Log.d("SAF", "Permission already granted")
        }

        root = DocumentFile.fromTreeUri(this, treeUri)
        Log.d("SAF", "Loaded root: $root")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("storage_prefs", MODE_PRIVATE)
        val uriString = prefs.getString("tree_uri", null)
        if (uriString == null) {
            // go here if permissions aren't saved
            openFolderPicker()
        }
        else {
            // go here if permissions are saved
            // openFolderPicker()
            loadRootFromPrefs()
        }
        FileManager.verifyStoragePermissions(this)
        if (root != null) {
            FileManager.readFile(this, root)
        }
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
    var showViewScreen by remember { mutableStateOf(false) }

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
            },
            onViewClicked = {
                showViewScreen = true
            }
        )
        if (showViewScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(alpha = 0.5f))
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
                ViewDialog(onContinueClicked = {
                    Planner.zonedDate = currDate.value
                    showViewScreen = false
                })
            }
        }
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
        OutlinedTextFieldBackground(Color.White) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .padding(8.dp),
                value = selectedDateText,
                onValueChange = {},
                label = { Text(placeholderText) },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { isDatePickerVisible = !isDatePickerVisible }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select a date for your task:"
                        )
                    }
                },
            )
        }

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
                Text("Back")
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

@Composable
fun taskList(
    modifier: Modifier = Modifier,
    myTasks: MyTasks,
    onAddClicked: () -> Unit,
    onViewClicked: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = { bottomBar(
            onContinueClicked = onAddClicked,
            onViewClicked = onViewClicked
        )},
        topBar = { topBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues = innerPadding),
        ) {
            if (selectedView.value == "Daily") {
                val untickedTasks = SnapshotStateList<Task>()
                val tickedTasks = SnapshotStateList<Task>()
                myTasks.tasks.forEach { task ->
                    if (task.isComplete) {
                        tickedTasks.add(task)
                    }
                    else {
                        untickedTasks.add(task)
                    }
                }
                items(items = untickedTasks + tickedTasks, key = { it.toString() }) { task ->
                    Task(
                        task = task,
                        date = currDate.value
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
            else {
                items(items = FileManager.upcomingList(context, root)) { dayTasks ->
                    for (i in 1..<dayTasks.size) {
                        Task(
                            task = dayTasks[i],
                            date = dayTasks[0].name
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun bottomBar(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit,
    onViewClicked: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.secondary) {
        Row {
            IconButton(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary)
                    .width(54.dp)
                    .height(54.dp)
                    .padding(6.dp),
                onClick = onViewClicked
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Select viewing option:",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary),
                onClick = onContinueClicked
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    )
            }
        }
    }
}

@Composable
fun topBar(
) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.secondary
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (selectedView.value == "Daily") 8.dp else 24.dp)
        ) {
            if (selectedView.value == "Daily") {
                val format = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                val newDate = format.parse(currDate.value)
                MaterialDatePicker(
                    placeholderText = "",
                    selectedDateMillis = newDate.time,
                    onDateSelected = { date ->
                        selectedDate.value = date
                        currDate.value = (date.plus(TimeUnit.DAYS.toMillis(1))).let {
                            formatDate(it)
                        }
                        workingDate.value = currDate.value
                        Planner.zonedDate = currDate.value
                        FileManager.readFile(context, root)
                    },
                )
            }
        }
    }
}

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
    val context = LocalContext.current
    Surface() {
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
        note = if (note.trim() == "") "" else note
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(16.dp))
            val displayName = if (editName.value.replace("_", " ").trim() == "") editName.value else editName.value.replace("_", " ")
            Text(
                text = if (editMode.value) "Editing ${displayName}:" else "Add New Task:",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            OutlinedTextFieldBackground(Color.White) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .padding(8.dp),
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
            }
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
            val format = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
            val newDate = format.parse(currDate.value)
            MaterialDatePicker(
                placeholderText = "Date",
                selectedDateMillis = if (editMode.value) selectedDate.value else newDate.time,
                onDateSelected = { date ->
                    workingDate.value = (date.plus(TimeUnit.DAYS.toMillis(1))).let {
                        formatDate(it) }
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextFieldBackground(MaterialTheme.colorScheme.onPrimary) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                    value = note,
                    onValueChange = {
                        note = it
                        noteDelimError = note.contains(Planner.DELIM)
                        canAdd =
                            !(nameError || nameDelimError || timeError || noteDelimError) && nameEdit
                    },
                    label = { Text("Note") }
                )
            }
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
                    onClick = {
                        workingDate.value = currDate.value
                        onContinueClicked()
                              },
                    modifier = Modifier
                        .background(
                            color = Color.LightGray,
                            shape = RoundedCornerShape(25.dp)
                        )
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
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
                            val fNote = if (note.trim() == "") " " else note.trim()
                            if (editMode.value) {
                                FileManager.readFile(context, root)
                                val index = Planner.getTaskIdx(
                                    editName.value,
                                    editStart.value,
                                    editEnd.value
                                )
                                System.out.println("moving at $index")
                                Planner.tasks.removeAt(index)
                                FileManager.writeFile(context, root)
                            }
                            Planner.zonedDate = workingDate.value
                            FileManager.readFile(context, root)
                            Planner.insertTask(fName, " ", start, end, fNote)
                            FileManager.writeFile(context, root)
                            workingDate.value = currDate.value
                            Planner.zonedDate = currDate.value
                            FileManager.readFile(context, root)
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
                    .background(Color.DarkGray.copy(alpha = 0.5f))
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
                    onContinueClicked = {
                        workingDate.value = currDate.value
                        Planner.zonedDate = currDate.value
                        FileManager.readFile(context, root)
                        confirmDelete = false
                    }
                )
            }
        }
    }
}

@Composable
fun ViewDialog(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit
) {
    val context = LocalContext.current
    val radioOptions = listOf("Daily", "Upcoming")
    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(
        modifier
            .selectableGroup()
            .background(
                color = Color.White.copy(alpha = 1f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedView.value),
                        onClick = {
                            selectedView.value = text
                            FileManager.readFile(context, root)
                            onContinueClicked()
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedView.value),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
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
        val displayName = if (editName.value.replace("_", " ").trim() == "") editName.value else editName.value.replace("_", " ")
        Text(
            text = "Delete ${displayName}?",
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
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    Planner.zonedDate = workingDate.value
                    FileManager.readFile(context, root)
                    val index = Planner.getTaskIdx(
                            editName.value,
                    editStart.value,
                    editEnd.value
                    )
                    System.out.println("removing at $index")
                    Planner.tasks.removeAt(index)
                    System.out.println("size: ${Planner.tasks.size}")
                    FileManager.writeFile(context, root)
                    System.out.println("reading...")
                    FileManager.readFile(context, root)
                    System.out.println("size: ${Planner.tasks.size}")
                    onContinueClicked()
                    editMode.value = false
                          },
                modifier = Modifier
                    .background(
                        color = Color(0xFFF06292),
                        shape = RoundedCornerShape(25.dp)
                    )
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

@Composable
fun OutlinedTextFieldBackground(
    color: Color,
    content: @Composable () -> Unit
) {
    // This box just wraps the background and the OutlinedTextField
    Box {
        // This box works as background
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp) // adding some space to the label
                .background(
                    color,
                    // rounded corner to match with the OutlinedTextField
                    shape = RoundedCornerShape(4.dp)
                )
        )
        // OutlineTextField will be the content...
        content()
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
fun Task(
    modifier: Modifier = Modifier,
    task: Task,
    date: String
) {
    val context = LocalContext.current
    var isChecked by remember { mutableStateOf(task.isComplete) }
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
            val format = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
            val newDate = format.parse(date)
            selectedDate.value = newDate.time
            Planner.zonedDate = date
            workingDate.value = date
            editMode.value = true
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isChecked) Color.LightGray else MaterialTheme.colorScheme.primary,
            contentColor = if (isChecked) Color.DarkGray else Color.White
        ),
        shape = RoundedCornerShape(12.dp))
     {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
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
                    isChecked = !isChecked
                    if (isChecked) {
                        task.tick()
                    }
                    else {
                        task.untick()
                    }
                    Planner.zonedDate = date
                    FileManager.readFile(context, root)
                    val index = Planner.getTaskIdx(
                        editName.value,
                        editStart.value,
                        editEnd.value
                    )
                    System.out.println("ticking at $index")
                    Planner.tasks.removeAt(index)
                    Planner.insertTask(task)
                    FileManager.writeFile(context, root)
                    Planner.zonedDate = currDate.value
                },
                colors = CheckboxColors(
                    checkedCheckmarkColor = Color.DarkGray,
                    uncheckedCheckmarkColor = Color.White,
                    checkedBoxColor = Color.LightGray,
                    uncheckedBoxColor = MaterialTheme.colorScheme.primary,
                    disabledCheckedBoxColor = Color.DarkGray,
                    disabledUncheckedBoxColor = Color.DarkGray,
                    disabledIndeterminateBoxColor = Color.DarkGray,
                    checkedBorderColor = Color.DarkGray,
                    uncheckedBorderColor = Color.White,
                    disabledBorderColor = Color.White,
                    disabledUncheckedBorderColor = Color.White,
                    disabledIndeterminateBorderColor = Color.White
                )
            )
            Text(
                text = if (task.name.replace("_", " ").trim() == "") task.name else task.name.replace("_", " "),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            val time = Planner.convertNumToTime(task.start) + "-" + Planner.convertNumToTime(task.end)
            Text(
                text = if (selectedView.value == "Daily") time else date,
                modifier = Modifier.padding(end = 4.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}