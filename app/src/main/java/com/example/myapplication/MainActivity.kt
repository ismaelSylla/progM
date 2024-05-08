package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.widget.Button
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import android.content.Intent
import android.view.LayoutInflater
import android.media.MediaPlayer
import com.example.myapplication.game1.Game1
import com.example.myapplication.game2.Game2
import com.example.myapplication.game3.Game3
import com.example.myapplication.game4.Game4
import com.example.myapplication.game5.Quiz
import com.example.myapplication.game5.QuizActivity
import com.example.myapplication.game6.SonActivity


class MainActivity : AppCompatActivity() {
    //private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val button1 = findViewById<Button>(R.id.bouton1)
        val button2 = findViewById<Button>(R.id.bouton2)
        val button3 = findViewById<Button>(R.id.bouton3)
        val button4 = findViewById<Button>(R.id.bouton4)
        val button5 = findViewById<Button>(R.id.bouton5)
        val button6 = findViewById<Button>(R.id.bouton6)

        // Initialise le lecteur de musique avec le fichier audio de votre choix
        //mediaPlayer = MediaPlayer.create(this, R.raw.gamemusic)

        // Commence la lecture de la musique
        //mediaPlayer?.start()

        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.starting_game, null)
        //val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        //val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
        val modedefi = dialogView.findViewById<Button>(R.id.modedefi)
        val modesimple = dialogView.findViewById<Button>(R.id.modesimple)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        modedefi.setOnClickListener {}
        modesimple.setOnClickListener {
            dialog.dismiss()
        }

        //dialog.show()

        button1.setOnClickListener {
            //Toast.makeText(this, "Bouton 1 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Game1::class.java)
            startActivity(intent)
            finish()
        }
        button2.setOnClickListener {
            //Toast.makeText(this, "Bouton 2 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Game2::class.java)
            startActivity(intent)
            finish()
        }
        button3.setOnClickListener {
            //Toast.makeText(this, "Bouton 3 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Game3::class.java)
            startActivity(intent)
            finish()
        }
        button4.setOnClickListener {
            //Toast.makeText(this, "Bouton 4 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Game4::class.java)
            startActivity(intent)
            finish()
        }
        button5.setOnClickListener {
            //Toast.makeText(this, "Bouton 5 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, QuizActivity::class.java)
            startActivity(intent)
            finish()
        }
        button6.setOnClickListener {
            //Toast.makeText(this, "Bouton 6 cliqué", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, SonActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}