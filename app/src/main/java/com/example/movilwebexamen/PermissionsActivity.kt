package com.example.movilwebexamen

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log // Importar Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PermissionsActivity : AppCompatActivity() {

    private lateinit var permissionTypeSpinner: Spinner
    private lateinit var permissionReasonEditText: EditText
    private lateinit var requestPermissionButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentDate: String // Declaración de currentDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        // Inicializar vistas
        permissionTypeSpinner = findViewById(R.id.permissionTypeSpinner)
        permissionReasonEditText = findViewById(R.id.permissionReasonEditText)
        requestPermissionButton = findViewById(R.id.requestPermissionButton)

        // Obtener SharedPreferences para el userName
        sharedPreferences = getSharedPreferences("MiPreferencia", MODE_PRIVATE) // Asegúrate de usar la misma clave
        val userName = sharedPreferences.getString("user_name", "") // Cambié "userName" a "user_name"

        // Obtener fecha actual en formato "yyyy-MM-dd"
        currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Configurar botón de solicitud de permiso
        requestPermissionButton.setOnClickListener {
            // Obtener valores del layout
            val permissionType = permissionTypeSpinner.selectedItem.toString()
            val permissionReason = permissionReasonEditText.text.toString()

            // Registrar los valores antes de enviarlos
            Log.d("PermissionsActivity", "userName: $userName")
            Log.d("PermissionsActivity", "permissionType: $permissionType")
            Log.d("PermissionsActivity", "permissionReason: $permissionReason")
            Log.d("PermissionsActivity", "currentDate: $currentDate")

            // Verificar que los campos obligatorios estén llenos
            if (userName.isNullOrEmpty() || permissionType == "Seleccione un tipo de permiso" || permissionReason.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviar los datos al servidor
            sendPermissionRequest(userName, currentDate, permissionType, permissionReason)
        }
    }

    private fun sendPermissionRequest(userName: String, date: String, permissionType: String, reason: String) {
        Thread {
            var outputStream: OutputStreamWriter? = null
            try {
                val url = URL("http://10.0.2.2:9191/movilconexion/pedirpermiso.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                urlConnection.doOutput = true

                // Crear el cuerpo de la solicitud
                val postData = "userName=$userName&date=$date&permissionType=$permissionType&reason=$reason"
                outputStream = OutputStreamWriter(urlConnection.outputStream)
                outputStream.write(postData)
                outputStream.flush()

                val responseCode = urlConnection.responseCode
                runOnUiThread {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@PermissionsActivity, "Permiso solicitado con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PermissionsActivity, "Error al solicitar permiso: Código $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PermissionsActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                // Cerrar el OutputStream si se ha abierto
                try {
                    outputStream?.close()
                } catch (e: Exception) {
                    // Manejo de error al cerrar el OutputStream
                    runOnUiThread {
                        Toast.makeText(this@PermissionsActivity, "Error al cerrar el flujo de salida: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }
}
