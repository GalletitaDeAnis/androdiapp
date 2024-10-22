package com.example.movilwebexamen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnRegister: Button = findViewById(R.id.btn_register)

        btnLogin.setOnClickListener {
            // Lógica de validación de inicio de sesión
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            // Redirige a la actividad de registro
            val intent = Intent(this, NewEmployeeActivity::class.java)
            startActivity(intent)
        }
    }
}
