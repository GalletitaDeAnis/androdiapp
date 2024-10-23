package com.example.movilwebexamen

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                LoginTask().execute(username, password)
            } else {
                Toast.makeText(this, "Por favor, ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, NewEmployeeActivity::class.java)
            startActivity(intent)
        }
    }

    private inner class LoginTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val username = params[0]
            val password = params[1]

            // Cambia la URL a la de tu servidor
            //val url = URL("http://192.168.100.102:9191/movilconexion/login.php")
            //val url = URL("http://localhost:9191/movilconexion/login.php")
            val url = URL("http://10.0.2.2:9191/movilconexion/login.php")

            Log.d("LoginActivity", "Username: $username, Password: $password")
            val postData = "username=$username&password=$password"
            Log.d("LoginActivity", "Post Data: $postData") // Agrega esta línea para depuración

            return try {
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val outputStream = DataOutputStream(conn.outputStream)
                outputStream.writeBytes(postData)
                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = BufferedInputStream(conn.inputStream)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    reader.forEachLine { response.append(it) }
                    response.toString()
                } else {
                    "Error: $responseCode"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Exception: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            result?.let {
                try {
                    val jsonResponse = JSONObject(it)
                    val status = jsonResponse.getString("status")

                    if (status == "success") {
                        val userId = jsonResponse.getInt("user_id") // Obtener el ID de usuario

                        // Guardar el ID de usuario en SharedPreferences
                        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("user_id", userId) // Almacenar el ID
                        editor.apply()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
