package com.example.myapplication.game2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class Game2 : AppCompatActivity(){

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.game2)

        val button1 = findViewById<Button>(R.id.bouton1)
        val button2 = findViewById<Button>(R.id.bouton2)
        val button3 = findViewById<Button>(R.id.bouton3)
        val button4 = findViewById<Button>(R.id.bouton4)

        button1.setOnClickListener{
            Toast.makeText(this, "Demarrage du jeu de l'arc", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, ModeSolo2::class.java)
            startActivity(intent)
            finish()
        }

        button2.setOnClickListener{
            //Toast.makeText(this, "Demarrage du jeu en ligne", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, MultiPlayer2::class.java)
            startActivity(intent)
            finish()
        }

        button4.setOnClickListener{
            //Toast.makeText(this, "Vous avez quitté le jeu de l'arc", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

