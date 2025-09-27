package com.example.jplanner

import android.R
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JplannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FileManager.readFile(this)
                    displayTasks(
                        modifier = Modifier.padding(innerPadding),
                        tasks = Planner.tasks
                    )
                }
            }
        }
    }
}

@Composable
fun readFile(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Button(
        onClick = { FileManager.readFile(context) }
    ) {
        Text("fetch schedule")
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
)
@Composable
fun displayTasks(modifier: Modifier = Modifier, tasks: List<Task> = Planner.tasks) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = tasks) { task ->
            Task(name = task.getName(), start = task.getStart(), end = task.getEnd())
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

@Preview(
showBackground = true
)
@Composable
fun showTask(modifier: Modifier = Modifier) {
    Task(name = "sample", start = 1, end = 2)
}