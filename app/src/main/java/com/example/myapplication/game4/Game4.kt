package com.example.myapplication.game4

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.MusicService
import com.example.myapplication.R
import com.example.myapplication.game1.Game1
import com.example.myapplication.game2.Game2
import com.example.myapplication.game3.Game3

class Game4 : AppCompatActivity(){
    //private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.game4)
        val button1 = findViewById<Button>(R.id.bouton1)
        val button2 = findViewById<Button>(R.id.bouton2)
        //val button3 = findViewById<Button>(R.id.bouton3)
        val button4 = findViewById<Button>(R.id.bouton4)


        // Initialise le lecteur de musique avec le fichier audio de votre choix
        //mediaPlayer = MediaPlayer.create(this, R.raw.gamemusic)

        // Commence la lecture de la musique
        //mediaPlayer?.start()

        val serviceIntent = Intent(this, MusicService::class.java)
        //startService(serviceIntent)


        button1.setOnClickListener {
            //Toast.makeText(this, "Bouton 1 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, ModeSolo4::class.java)
            startActivity(intent)
            finish()
        }
        button2.setOnClickListener {
            //Toast.makeText(this, "Bouton 2 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Multiplayer4::class.java)
            startActivity(intent)
            finish()
        }
        /*button3.setOnClickListener {
            Toast.makeText(this, "Bouton 3 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Game3::class.java)
            startActivity(intent)
            finish()
        }*/
        button4.setOnClickListener {
            //Toast.makeText(this, "Bouton 4 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game4)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}