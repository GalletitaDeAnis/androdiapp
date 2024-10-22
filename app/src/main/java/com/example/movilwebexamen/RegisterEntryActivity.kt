package com.example.movilwebexamen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

class RegisterEntryActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_entry)

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Obtener fecha y hora actual
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
        if (delayInMinutes > 0) {
            delayObservationTextView.text = "Minutos de Retraso: $delayInMinutes"
        } else {
            delayObservationTextView.text = "Sin Retraso"
        }

        // Botón para confirmar entrada
        findViewById<Button>(R.id.confirmEntryButton).setOnClickListener {
            // Aquí puedes agregar lógica para confirmar la entrada
        }

        // Botón para salir
        findViewById<Button>(R.id.exitButton).setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Obtener la ubicación actual (puedes usar fused location provider para obtener la ubicación real)
        val currentLocation = LatLng(-16.500000, -68.150000) // Ejemplo: Ubicación en La Paz, Bolivia
        googleMap.addMarker(MarkerOptions().position(currentLocation).title("Ubicación Actual"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

        // Mostrar la ubicación en el TextView
        findViewById<TextView>(R.id.entryLocationTextView).text = "Ubicación: ${currentLocation.latitude}, ${currentLocation.longitude}"
    }
}
