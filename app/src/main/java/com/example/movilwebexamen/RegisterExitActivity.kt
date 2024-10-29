package com.example.movilwebexamen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

class RegisterExitActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_exit)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        findViewById<TextView>(R.id.exitTimeTextView).text = "Hora: ${timeFormat.format(currentTime)}"
        findViewById<TextView>(R.id.exitDateTextView).text = "Fecha: ${dateFormat.format(currentTime)}"

        val sixPm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
        }
        val timeDifference = (sixPm.timeInMillis - currentTime.time) / (1000 * 60)
        findViewById<TextView>(R.id.delayObservationTextView).text = if (timeDifference > 0) {
            "Minutos Faltantes: $timeDifference"
        } else {
            "Tiempo Completo"
        }

        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "No se encontró el ID de usuario.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<Button>(R.id.confirmExitButton).setOnClickListener {
            enviarRegistroEntrada(userId.toString(), dateFormat.format(currentTime), timeFormat.format(currentTime), currentLatitude.toString(), currentLongitude.toString())
        }

        findViewById<Button>(R.id.exitButton).setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        obtenerUbicacionActual()
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

                // Asegurarse de que el TextView no sea nulo y tiene el ID correcto
                findViewById<TextView>(R.id.exitLocationTextView)?.let { locationTextView ->
                    locationTextView.text = "Ubicación: ${currentLatitude}, ${currentLongitude}"
                } ?: run {
                    Log.e("RegisterExitActivity", "TextView exitLocationTextView es nulo")
                }
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

    private fun enviarRegistroEntrada(userId: String, fecha: String, hora: String, latitud: String, longitud: String) {
        val url = URL("http://192.168.100.102:9191/movilconexion/register_exit.php")
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
                val responseMessage = conn.inputStream.bufferedReader().use { it.readText() }

                runOnUiThread {
                    val jsonResponse = JSONObject(responseMessage)
                    val message = jsonResponse.optString("message", "sin detalles")
                    if (responseCode == HttpURLConnection.HTTP_OK && jsonResponse.getString("status") == "success") {
                        Toast.makeText(this@RegisterExitActivity, "Salida registrada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegisterExitActivity, "Error al registrar salida: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@RegisterExitActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
