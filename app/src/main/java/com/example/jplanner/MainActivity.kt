package com.example.jplanner

import android.R
import android.R.attr.label
import android.R.attr.text
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter

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
                    taskList(modifier = Modifier.padding(innerPadding), myTasks = list)
                }
            }
        }
    }
}

class MyTasks {
    val tasks = mutableStateListOf<Task>()
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

@Preview
@Composable
fun editTaskScreen(modifier: Modifier = Modifier) {
    Surface() {
        var text by remember { mutableStateOf("") }
        Column {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Name:") }
            )
            Row {
                TextField(
                    value =
                )
            }
        }
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