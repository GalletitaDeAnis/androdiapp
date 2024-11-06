package com.example.movilwebexamen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class RegisterEntryActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_entry)

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Obtener fecha y hora actual
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val entryTimeTextView = findViewById<TextView>(R.id.entryTimeTextView)
        val entryDateTextView = findViewById<TextView>(R.id.entryDateTextView)
        val delayObservationTextView = findViewById<TextView>(R.id.delayObservationTextView)

        entryTimeTextView.text = "Hora: ${timeFormat.format(currentTime)}"
        entryDateTextView.text = "Fecha: ${dateFormat.format(currentTime)}"

        // Calcular minutos de retraso a partir de las 8:00 AM
        val eightAm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }

        val delayInMinutes = (currentTime.time - eightAm.timeInMillis) / (1000 * 60)
        delayObservationTextView.text = if (delayInMinutes > 0) {
            "Minutos de Retraso: $delayInMinutes"
        } else {
            "Sin Retraso"
        }

        // Recuperar el ID de usuario de SharedPreferences
        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1) // Devuelve -1 si no se encuentra

        if (userId != -1) {
            // El ID de usuario está disponible
            Log.d("TAG", "ID de usuario recuperado: $userId")
        } else {
            // El ID de usuario no está disponible
            Log.d("TAG", "No se encontró el ID de usuario.")
            Toast.makeText(this, "No se encontró el ID de usuario.", Toast.LENGTH_SHORT).show()
            finish() // Cerrar la actividad si el ID no está disponible
            return
        }

        // Botón para confirmar entrada
        findViewById<Button>(R.id.confirmEntryButton).setOnClickListener {
            // Obtener la ubicación actual (simulada en este ejemplo)
            val latitud = currentLatitude // Ejemplo de latitud
            val longitud = currentLongitude // Ejemplo de longitud

            // Obtener la fecha y hora actuales en el formato correcto
            val fecha = dateFormat.format(currentTime)
            val hora = timeFormat.format(currentTime)

            // Llamar a la función para enviar los datos al servidor
            enviarRegistroEntrada(userId.toString(), fecha, hora, latitud.toString(), longitud.toString())
        }

        // Botón para salir
        findViewById<Button>(R.id.exitButton).setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        obtenerUbicacionActual()
        // Obtener la ubicación actual (puedes usar fused location provider para obtener la ubicación real)
        val currentLocation = LatLng(currentLatitude , currentLongitude) // Latitud y longitud
        googleMap.addMarker(MarkerOptions().position(currentLocation).title("Ubicación Actual"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

        // Mostrar la ubicación en el TextView
        findViewById<TextView>(R.id.entryLocationTextView).text = "Ubicación: ${currentLocation.latitude}, ${currentLocation.longitude}"
    }

    private fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no están concedidos
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
                val currentLocation = LatLng(currentLatitude, currentLongitude)

                googleMap.addMarker(MarkerOptions().position(currentLocation).title("Ubicación Actual"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

                // Mostrar la ubicación en el TextView
                findViewById<TextView>(R.id.entryLocationTextView).text = "Ubicación: ${currentLatitude}, ${currentLongitude}"
            } ?: run {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }


    // Esta función está fuera de onCreate para mejorar la estructura del código
    private fun enviarRegistroEntrada(userId: String, fecha: String, hora: String, latitud: String, longitud: String) {
        val url = URL("http://10.0.2.2:9191/movilconexion/register_entry.php")
        //val url = URL("http://192.168.100.102:9191/movilconexion/register_entry.php")
        val postData = "user_id=$userId&fecha=$fecha&hora=$hora&latitud=$latitud&longitud=$longitud"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                conn.outputStream.use { outputStream ->
                    outputStream.write(postData.toByteArray())
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    runOnUiThread {
                        if (jsonResponse.getString("status") == "success") {
                            Toast.makeText(this@RegisterEntryActivity, "Entrada registrada exitosamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RegisterEntryActivity, "Error al registrar entrada: ${jsonResponse.optString("message", "sin detalles")}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegisterEntryActivity, "Error de conexión: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@RegisterEntryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
