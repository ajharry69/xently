package co.ke.xently

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.ui.theme.XentlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App {
                MyScreenContent()
            }
        }
    }
}

@Composable
fun App(content: @Composable () -> Unit) {
    XentlyTheme {
        Surface(color = Color.Yellow) {
            content()
        }
    }
}

@Composable
fun MyScreenContent(names: List<String> = List(1_000) { "Android #${it + 1}" }) {
    val countState: MutableState<Int> = remember {
        mutableStateOf(0)
    }
    Column(modifier = Modifier.fillMaxHeight()) {
        NameList(names, modifier = Modifier.weight(1f))
        Counter(countState.value) { count ->
            countState.value = count
        }
    }
}

@Composable
fun NameList(names: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(names) { name ->
            Greeting(name = name)
            Divider(color = Color.Black, thickness = 0.2.dp, startIndent = 4.dp)
        }
    }
}

@Composable
fun Greeting(name: String) {
    var isSelected by remember {
        mutableStateOf(false)
    }
    val backgroundColor by
    animateColorAsState(targetValue = if (isSelected) Color.Red else Color.Transparent)
    Text(
        text = "Hello $name!",
        modifier = Modifier
            .padding(24.dp)
            .background(color = backgroundColor)
            .clickable {
                isSelected = !isSelected
            },
        style = MaterialTheme.typography.h1.copy(color = Color.Magenta),
    )
}

@Composable
fun Counter(count: Int, updateCount: (Int) -> Unit = { _ -> }) {
    Button(colors = ButtonDefaults.buttonColors(
        backgroundColor = if (count > 5) Color.Green else Color.White,
    ), onClick = { updateCount(count + 1) }) {
        Text(text = "Clicked $count times!")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    App {
        MyScreenContent()
    }
}