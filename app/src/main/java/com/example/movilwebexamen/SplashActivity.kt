package com.example.movilwebexamen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Redirigir a LoginActivity después de un retraso
        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Cierra SplashActivity
        }, 2000) // 2000 milisegundos = 2 segundos
    }
}
