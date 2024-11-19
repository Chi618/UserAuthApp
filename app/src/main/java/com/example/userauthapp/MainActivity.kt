package com.example.userauthapp
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.userauthapp.ui.theme.UserAuthAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserAuthAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        // Username Input Field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = login(username, password)
                    withContext(Dispatchers.Main) {
                        if (result) {
                            message = "Login Success"
                        } else {
                            message = "Login Failed: Invalid credentials"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display message
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

suspend fun login(username: String, password: String): Boolean {
    return try {
        val url = URL("http://10.0.2.2/android_api/login.php") // Replace with correct URL
        val conn = url.openConnection() as HttpURLConnection

        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        val postData = "username=$username&password=$password"
        val writer = OutputStreamWriter(conn.outputStream)
        writer.write(postData)
        writer.flush()

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)
            jsonResponse.getBoolean("success")
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e("LoginError", e.toString())
        false
    }
}
