package com.example.myapplication.game1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import android.view.MotionEvent
import android.widget.TextView
import android.widget.ImageView
import android.os.CountDownTimer
import android.widget.Button
import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.app.AlertDialog
import android.view.LayoutInflater


class ModeSolo : AppCompatActivity(){

    lateinit var timerTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var horizontalBias: Float = 0.547f
    private var score: Int = 0
    private var currentImagePosition: Float = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mode_solo_game1)

        val imageView: ImageView = findViewById(R.id.mafleche)

        startCountDownTimer()
        moveImage()

        imageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Action lorsque l'utilisateur commence à toucher l'image
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculer le déplacement horizontal
                    val deltaX = event.rawX - view.width / 2 - view.x
                    val parentLayout = findViewById<ConstraintLayout>(R.id.modesolo)
                    val parentWidth = parentLayout.width.toFloat()

                    //val imageView = findViewById<ImageView>(R.id.mafleche)
                    val halfImageWidth = imageView.width / 2
                    horizontalBias =(view.x + halfImageWidth) / parentWidth

                    // Mettre à jour la position horizontale de l'image
                    view.x += deltaX

                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Action lorsque l'utilisateur arrête de toucher l'image
                    true
                }
                else -> false
            }
        }

        val quit = findViewById<Button>(R.id.quit)
        val launch = findViewById<Button>(R.id.launch)

        quit.setOnClickListener{
            Toast.makeText(this, "Demarrage du jeu de l'arc", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Game1::class.java)
            startActivity(intent)
            finish()
        }

        launch.setOnClickListener{
            //Toast.makeText(this, "Lancer de l'Arc", Toast.LENGTH_SHORT).show()
            launch()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.modesolo)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun moveImage() {
        val imageView = findViewById<ImageView>(R.id.imageView1)
        val screenWidth = resources.displayMetrics.widthPixels

        val dpValue = 145 // La valeur en dp que vous souhaitez convertir
        val density = resources.displayMetrics.density
        val imageWidth = (dpValue * density + 0.5f).toInt()

        val initialX = -imageWidth.toFloat()
        val finalX = screenWidth.toFloat()

        val animator = ObjectAnimator.ofFloat(imageView, "translationX", initialX, finalX)
        animator.duration = 5000 // Durée de l'animation en millisecondes
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            currentImagePosition = animation.animatedValue as Float

            if (currentValue >= finalX) {
                // Si l'image est arrivée à la fin de l'écran, inverse la direction
                animator.setFloatValues(finalX, initialX)
                println("Update droit->gauche")
            } else if (currentValue <= initialX) {
                // Si l'image est arrivée au début de l'écran, inverse la direction
                animator.setFloatValues(initialX, finalX)
                println("Update gauche->droit")
            }
        }

        animator.start()
    }

    private fun startCountDownTimer() {
        timerTextView = findViewById(R.id.time)

        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Met à jour le texte du TextView avec le temps restant
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "$secondsLeft s"
            }

            override fun onFinish() {
                showGameOverDialog()
            }
        }
        // Démarre le compte à rebours
        countDownTimer.start()
    }

    /*private fun showGameOverDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Partie terminée")
            .setMessage("Que voulez-vous faire ?")
            .setPositiveButton("Quitter") { dialog, _ ->
                dialog.dismiss()
                finish() // Quitte l'application
            }
            .setNegativeButton("Reprendre") { dialog, _ ->
                dialog.dismiss()
                //startCountDownTimer() // Reprend la partie en redémarrant le compte à rebours
            }
            .setCancelable(false) // Empêche de fermer le dialogue en cliquant en dehors
            .show()
    }*/

    private fun showGameOverDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_over, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
        val quitButton = dialogView.findViewById<Button>(R.id.button_quit)
        val resumeButton = dialogView.findViewById<Button>(R.id.button_resume)

        titleTextView.text = "Partie terminée"
        messageTextView.text = "Que voulez-vous faire ?"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        quitButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(applicationContext, Game1::class.java)
            startActivity(intent)
            finish()
        }

        resumeButton.setOnClickListener {
            dialog.dismiss()
            startCountDownTimer()
            val scoreTextView = findViewById<TextView>(R.id.score)
            score = 0
            scoreTextView.text = "$score"
        }

        dialog.show()
    }

    private fun launch(){
        val redDot = ImageView(this)
        redDot.setImageResource(R.drawable.red_dot)

        //println("horrizontal Bias:"+ horizontalBias)
        // Récupérer les LayoutParams du parent
        val parentLayout = findViewById<ConstraintLayout>(R.id.modesolo)

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.verticalBias = 0.525f
        params.horizontalBias = horizontalBias


        redDot.layoutParams = params

        parentLayout.addView(redDot)
        scoring()

        val timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Ne fait rien pendant le compte à rebours
            }

            override fun onFinish() {
                // Supprime le point rouge après 1,5 secondes
                parentLayout.removeView(redDot)
            }
        }
        timer.start()
    }

    private fun scoring(){
        val parentLayout = findViewById<ConstraintLayout>(R.id.modesolo)
        val parentWidth = parentLayout.width.toFloat()
        val dpValue = 145 // La valeur en dp de ma cible
        val density = resources.displayMetrics.density
        val imageWidth = (dpValue * density + 0.5f).toInt()
        val posPoint = horizontalBias*parentWidth
        val posCible = currentImagePosition+imageWidth/2

        val scoreTextView = findViewById<TextView>(R.id.score)


        if(posPoint < (posCible + 1*imageWidth/12) && posPoint > (posCible - 1*imageWidth/12)){
            score += 5
        }else if(posPoint < (posCible + 2*imageWidth/12) && posPoint > (posCible - 2*imageWidth/12)){
            score += 4
        }else if(posPoint < (posCible + 3*imageWidth/12) && posPoint > (posCible - 3*imageWidth/12)){
            score += 3
        }else if(posPoint < (posCible + 4*imageWidth/12) && posPoint > (posCible - 4*imageWidth/12)){
            score += 2
        }else if(posPoint < (posCible + 5*imageWidth/12) && posPoint > (posCible - 5*imageWidth/12)){
            score += 1
        }else if(posPoint < (posCible + 6*imageWidth/12) && posPoint > (posCible - 6*imageWidth/12)){
            score += 0
        }
        scoreTextView.text = "$score"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrête le compte à rebours pour éviter les fuites de mémoire
        countDownTimer.cancel()

    }
}