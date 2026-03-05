package com.example.m4t13

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m4t13.ui.theme.M4t13Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val viewModel: ExchangeRateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            M4t13Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExchangeRateScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

class ExchangeRateViewModel : ViewModel() {
    private val _usdToRubRate = MutableStateFlow(generateRate())
    val usdToRubRate: StateFlow<Double> = _usdToRubRate

    private val _previousRate = MutableStateFlow<Double?>(null)
    val previousRate: StateFlow<Double?> = _previousRate

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                refreshRate()
            }
        }
    }

    fun refreshRate() {
        _previousRate.value = _usdToRubRate.value
        _usdToRubRate.value = generateRate()
    }

    private fun generateRate(): Double = Random.nextDouble(from = 88.5, until = 92.5)
}

@Composable
fun ExchangeRateScreen(
    viewModel: ExchangeRateViewModel,
    modifier: Modifier = Modifier
) {
    val rate by viewModel.usdToRubRate.collectAsState()
    val previousRate by viewModel.previousRate.collectAsState()

    ExchangeRateContent(
        rate = rate,
        previousRate = previousRate,
        onRefresh = viewModel::refreshRate,
        modifier = modifier
    )
}

@Composable
private fun ExchangeRateContent(
    rate: Double,
    previousRate: Double?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trendSymbol = when {
        previousRate == null -> ""
        rate > previousRate -> "▲"
        rate < previousRate -> "▼"
        else -> "→"
    }

    val trendColor = when {
        previousRate == null -> MaterialTheme.colorScheme.onBackground
        rate > previousRate -> Color(0xFF2E7D32)
        rate < previousRate -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "USD/RUB",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "${"%.2f".format(rate)} ₽  $trendSymbol",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = trendColor,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )

        Button(onClick = onRefresh) {
            Text(text = "Обновить сейчас")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExchangeRateScreenPreview() {
    M4t13Theme {
        ExchangeRateContent(
            rate = 90.73,
            previousRate = 89.54,
            onRefresh = {}
        )
    }
}
