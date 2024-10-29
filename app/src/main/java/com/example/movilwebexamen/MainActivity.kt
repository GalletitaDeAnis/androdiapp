package com.example.movilwebexamen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.movilwebexamen.ui.theme.MovilWebExamenTheme
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtener el nombre del usuario desde SharedPreferences
        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "Usuario Desconocido") // Valor por defecto

        // Configurar el saludo
        val tvGreeting: TextView = findViewById(R.id.tv_greeting)
        tvGreeting.text = "Buenos días, $userName"


        val btnRegisterEntry: Button = findViewById(R.id.btn_register_entry)
        val btnRegisterExit: Button = findViewById(R.id.btn_register_exit)
        val btnRetreats: Button = findViewById(R.id.btn_retreats)
        val btnPermissions: Button = findViewById(R.id.btn_permissions)
        val btnLogout: Button = findViewById(R.id.btn_logout)

        btnRegisterEntry.setOnClickListener {
            val intent = Intent(this, RegisterEntryActivity::class.java)
            startActivity(intent)
        }

        btnRegisterExit.setOnClickListener {
            val intent = Intent(this, RegisterExitActivity::class.java)
            startActivity(intent)
        }

        btnRetreats.setOnClickListener {
            val intent = Intent(this, RetreatsActivity::class.java)
            startActivity(intent)
        }

        btnPermissions.setOnClickListener {
            val intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Lógica para cerrar sesión
            finish()
        }
    }
}
