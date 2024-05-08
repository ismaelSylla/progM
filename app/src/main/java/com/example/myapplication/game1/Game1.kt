package com.example.myapplication.game1

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.MainActivity

class Game1 : AppCompatActivity(){
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private val REQUEST_ENABLE_BT = 1
    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSION = 1001
    }
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.game1)

        val button1 = findViewById<Button>(R.id.bouton1)
        val button2 = findViewById<Button>(R.id.bouton2)
        val button3 = findViewById<Button>(R.id.bouton3)
        val button4 = findViewById<Button>(R.id.bouton4)

        button1.setOnClickListener{
            Toast.makeText(this, "Demarrage du jeu de l'arc", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, ModeSolo::class.java)
            startActivity(intent)
            finish()
        }

        button2.setOnClickListener{
            Toast.makeText(this, "Demarrage du jeu en ligne", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, MultiPlayers::class.java)
            startActivity(intent)
            finish()
        }

        button4.setOnClickListener{
            Toast.makeText(this, "Vous avez quitté le jeu de l'arc", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    //Affichage de la fenetre pour soit rejoindre ou creer une partie
    private fun showBLEconnect() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_invite_bluetooth, null)
        //val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        //val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
        val inviteButton = dialogView.findViewById<Button>(R.id.invite)
        val waitButton = dialogView.findViewById<Button>(R.id.attente)


        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        inviteButton.setOnClickListener {
            val intent = Intent(applicationContext, MultiPlayers::class.java)
            Toast.makeText(this, "Invitation", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }

        waitButton.setOnClickListener {

        }

        dialog.show()
    }

}

