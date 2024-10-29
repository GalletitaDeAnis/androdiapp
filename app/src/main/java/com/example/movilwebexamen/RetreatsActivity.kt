package com.example.movilwebexamen


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RetreatsActivity : AppCompatActivity() {

    private lateinit var permissionsListView: ListView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retreats)

        permissionsListView = findViewById(R.id.permissionsListView)

        // Obtener SharedPreferences para el userName
        sharedPreferences = getSharedPreferences("MiPreferencia", MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "")

        if (userName != null) {
            loadPermissions(userName)
        } else {
            Toast.makeText(this, "No se encontró el nombre de usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPermissions(userName: String) {
        Thread {
            try {
                val url = URL("http://10.0.2.2:9191/movilconexion/obtener_permisos.php?userName=$userName")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = urlConnection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    // Procesar la respuesta JSON
                    val jsonArray = JSONArray(response.toString())
                    val permisos = ArrayList<String>()
                    val colores = ArrayList<Int>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val tipoPermiso = item.getString("tipo_permiso")
                        val estado = item.getString("estado")
                        permisos.add(tipoPermiso)

                        // Asignar color según el estado
                        colores.add(
                            when (estado) {
                                "Aprobado" -> R.color.green
                                "Pendiente" -> R.color.white
                                "Rechazado" -> R.color.red
                                else -> R.color.white
                            }
                        )
                    }

                    runOnUiThread {
                        // Usar el adaptador personalizado
                        val adapter = PermissionAdapter(this, permisos, colores)
                        permissionsListView.adapter = adapter
                    }
                } else {
                    Log.e("PermissionsListActivity", "Error en la conexión: $responseCode")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
