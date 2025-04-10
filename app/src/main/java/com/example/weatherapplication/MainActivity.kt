package com.example.weatherapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapplication.ui.theme.WeatherApplicationTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response // 显式导入 retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val request_id: String,
    val success: Boolean,
    val message: String,
    val code: Int,
    val data: Weather,
    val time: Long,
    val usage: Int
)

data class Weather(
    val city: String,
    val city_en: String,
    val province: String,
    val province_en: String,
    val city_id: String,
    val date: String,
    val update_time: String,
    val weather: String,
    val weather_code: String,
    val temp: Double,
    val min_temp: Int,
    val max_temp: Int,
    val wind: String,
    val wind_speed: String,
    val wind_power: String,
    val rain: String,
    val rain_24h: String,
    val humidity: String,
    val visibility: String,
    val pressure: String,
    val tail_number: String,
    val air: String,
    val air_pm25: String,
    val sunrise: String,
    val sunset: String,
    val aqi: Aqi,
    val index: List<IndexItem>,
    val alarm: List<Any>,
    val hour: List<HourItem>
)

data class Aqi(
    val air: String,
    val air_level: String,
    val air_tips: String,
    val pm25: String,
    val pm10: String,
    val co: String,
    val no2: String,
    val so2: String,
    val o3: String
)

data class IndexItem(
    val type: String,
    val level: String,
    val name: String,
    val content: String
)

data class HourItem(
    val time: String,
    val temp: Int,
    val wea: String,
    val wea_code: String,
    val wind: String,
    val wind_level: String
)

interface apiService {
    @GET("api/tianqi")
    fun getWeather(
        @Query("token") token: String,
        @Query("city") city: String
    ): retrofit2.Call<WeatherResponse>
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Title(
                        name = "天气",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun RequestApi() {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://v3.alapi.cn/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(apiService::class.java)
    val data = remember { mutableStateOf<Weather?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                val response: Response<WeatherResponse> = apiService.getWeather("hnq0tkp4bowkjcqtbn5xxd4qy1kjoj", "重庆").execute()
                Log.d("WeatherApp", "API 请求响应码: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody == null) {
                        val rawResponse = response.raw().body()?.let {
                            it.string()
                        }
                        Log.e("WeatherApp", "API 返回 null，原始响应: $rawResponse")
                    } else {
                        data.value = responseBody.data
                        Log.d("WeatherApp", "API 响应数据: ${responseBody.data}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WeatherApp", "API 请求失败: ${response.code()}，错误信息: $errorBody")
                }
            }
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
            Log.e("WeatherApp", "网络请求异常: ${e.message}")
        }
    }

    if (isLoading.value) {
        Text("正在加载...")
    } else {
        data.value?.let { weather ->
            TitleWithData(weather)
        } ?: Text("数据加载失败")
    }
}

@Composable
fun TitleWithData(weather: Weather) {
    Column {
        Text(
            text = "今日天气",
            fontSize = 20.sp,
            color = Color(0xB15B73F4),
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "${weather.temp}℃",
            fontSize = 45.sp,
            color = Color(0xFF596FEB),
            textAlign = TextAlign.Center,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            val weatherIconRes = when (weather.weather) {
                "晴" -> R.drawable.sunny
                "多云" -> R.drawable.cloudy
                "雨" -> R.drawable.rainy
                else -> R.drawable.sunny
            }
            Image(
                painter = painterResource(id = weatherIconRes),
                contentDescription = "天气图标",
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp)
            )
            Text(
                text = weather.weather,
                fontSize = 30.sp,
                color = Color(0xFF596FEB),
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            )
        }
        Row {
            Text(
                text = "最高温：${weather.max_temp}℃",
                textAlign = TextAlign.Right,
                color = Color(0xFF9879D0),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "最低温：${weather.min_temp}℃",
                textAlign = TextAlign.Left,
                color = Color(0xFF9879D0),
                modifier = Modifier.weight(1f)
            )
        }
        // 添加空气质量信息展示
        Text(
            text = "空气质量: ${weather.aqi.air} (${weather.aqi.air_level})",
            fontSize = 20.sp,
            color = Color(0xFF7AC4E6),
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "PM2.5: ${weather.aqi.pm25}",
            fontSize = 16.sp,
            color = Color(0xFF73AEB5),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        Text(
            text = "建议: ${weather.aqi.air_tips}",
            fontSize = 16.sp,
            color = Color(0xFF673AB7),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
        Card {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color(0xFF8989E7))
            ) {
                items(weather.index) { indexItem ->
                    Text(
                        text = "${indexItem.name}: ${indexItem.level}\n${indexItem.content}",
                        color = Color(0xFFFCFCFE),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Title(name: String, modifier: Modifier = Modifier) {
    RequestApi()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherApplicationTheme {
        Title("天气")
    }
}