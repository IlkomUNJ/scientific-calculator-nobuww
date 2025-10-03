package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    val displayText = remember { mutableStateOf("0") }
    val scientific = remember { mutableStateOf(false) }
    val previewResult = remember { mutableStateOf("") }
    val history = remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(displayText.value) {
        if (displayText.value.isNotEmpty() && displayText.value != "0") {
            val result = evaluate(displayText.value)
            previewResult.value = result
        } else {
            previewResult.value = ""
        }
    }

    val onButtonClick: (String) -> Unit = { buttonText ->
        if (buttonText == "C") {
            displayText.value = "0"
            previewResult.value = ""
        } else if (buttonText == "AC") {
            history.value = listOf()
        } else if (buttonText == "=") {
            if (previewResult.value.isNotEmpty() && previewResult.value != "Error") {
                val currentHistory = history.value
                val newEntry = "${displayText.value} = ${previewResult.value}"
                val newHistory = listOf(newEntry) + currentHistory
                history.value = newHistory.take(3)
                displayText.value = previewResult.value
            }
        } else if (buttonText == "1/x") {
            displayText.value += "^(-1)"
        } else if (buttonText == "Sc") {
            scientific.value = !scientific.value
        } else if (buttonText == "x^y") {
            displayText.value += "^"
        } else if (buttonText == "<-") {
            displayText.value = displayText.value.dropLast(1)
            if (displayText.value.isEmpty()) {
                displayText.value = "0"
            }
        } else {
            val addition = if (buttonText in functions) "(" else ""
            if (displayText.value == "0" && buttonText != ".") {
                displayText.value = buttonText + addition
            } else {
                displayText.value += buttonText + addition
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (scientific.value) 2.3f else 3f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                history.value.forEach { entry ->
                    Text(
                        text = entry,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = displayText.value,
                    fontSize = 56.sp,
                    textAlign = TextAlign.End,
                    lineHeight = 58.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (previewResult.value.isNotEmpty()) {
                    Text(
                        text = "= ${previewResult.value}",
                        fontSize = 32.sp,
                        textAlign = TextAlign.End,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(4f),
        ) {
            var buttonRows = listOf(
                listOf(if (displayText.value == "0") "AC" else "C", "<-", "x^y", "/"),
                listOf("7", "8", "9", "x"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("Sc", "0", ".", "=")
            )
            if (scientific.value) {
                buttonRows = listOf(
                    listOf("log", "ln", "sin", "cos", "tan"),
                    listOf("sqrt", "%", "asin", "acos", "atan"),
                    listOf("x!", if (displayText.value == "0") "AC" else "C", "<-", "x^y", "/"),
                    listOf("1/x", "7", "8", "9", "x"),
                    listOf("(", "4", "5", "6", "-"),
                    listOf(")", "1", "2", "3", "+"),
                    listOf("Sc", "e", "0", ".", "=")
                )
            }
            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    row.forEach { buttonText ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            CalculatorButton(
                                text = buttonText,
                                scientific = scientific.value,
                                modifier = Modifier,
                                onClick = {
                                    onButtonClick(buttonText)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(text: String, scientific: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOperator = text in listOf("+", "-", "C", "AC", "/", "x", "<-", "x^y", "Sc")
    val grey = text in listOf(
        "log", "ln", "sin", "cos", "tan", "sqrt", "%", "asin", "acos", "atan",
        "x!", "1/x", "(", ")"
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (text == "=") Color(0xFFED8114.toInt()) else Color(0xFF242424)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (scientific) 20.sp else 24.sp,
            color = if (isOperator) Color(0xFFED8114.toInt()) else if (grey) Color(0xFF828383) else Color.White,
            softWrap = false,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        CalculatorApp()
    }
}