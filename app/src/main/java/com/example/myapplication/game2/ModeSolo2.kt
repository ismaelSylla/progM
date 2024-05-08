package com.example.myapplication.game2

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import android.view.GestureDetector
import android.view.MotionEvent
import android.annotation.SuppressLint
import android.app.AlertDialog
import kotlin.math.abs
import android.graphics.Point
import android.os.CountDownTimer
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.Random
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import com.example.myapplication.game1.Game1


class ModeSolo2 : AppCompatActivity() {

    private lateinit var chat: ImageView
    lateinit var timerTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var threshold = 1
    private var defilement = 0f
    private var duration = 3000L
    private var score = 0
    private var collisionLeft = false
    private var collisionCenter = false
    private var collisionRight = false
    private lateinit var gestureDetector: GestureDetector
    private var currentImagePosition: Float = 0f
    private lateinit var fireViews: List<ImageView>
    private val fireAnimations = mutableListOf<Animator>()
    private val random = Random()
    private var delayList = arrayOf(500L, 1200L, 1900L, 500L)
    var arrayDuration = arrayOf(2000L,2500L, 3000L,3500L, 4000L)
    private var life = 5
    val handler = Handler() //pour augmenter le score, on l'utilise aussi pour redemaree le score


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //durationList[0] = 1111
        setContentView(R.layout.mode_solo_game2)

        //Definir mon thresHold
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenWidth = size.x
        chat = findViewById(R.id.chat)
        fireViews = listOf(
            findViewById(R.id.fire),
            findViewById(R.id.fire2),
            findViewById(R.id.fire3),
            // Ajoutez les autres ImageView des boules de feu ici
        )

        // Calculez le seuil comme un tiers de la taille de l'écran
        defilement = (screenWidth / 3).toFloat()

        chat = findViewById(R.id.chat)
        gestureDetector = GestureDetector(this, GestureListener())

        moveFire()

        riseDifficulty()
        shuffleDelay()
        score()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.modesolo2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private inner class GestureListener: GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            //val deltaX = e2.x - e1.x
            val deltaX = (e2.x - e1?.x!!) ?: 0f
            if (abs(deltaX) > threshold) {
                if (deltaX > 0) {
                    moveImageRight()
                } else {
                    moveImageLeft()
                }
                return true
            }

            return false
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }


    private fun moveImageRight() {
        val screenWidth = resources.displayMetrics.widthPixels
        if (!(chat.x >= 2*screenWidth/3)){
            chat.animate().translationXBy(defilement).setDuration(50).start()

        }
        //chat.animate().translationXBy(defilement).setDuration(100).start()
    }

    private fun moveImageLeft() {
        val screenWidth = resources.displayMetrics.widthPixels
        if (!(chat.x <= screenWidth/3)){
            chat.animate().translationXBy(-defilement).setDuration(50).start()
        }
    }

    private fun moveFire(){
        val chat = findViewById<ImageView>(R.id.chat)
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        val dpValue_fire = 100 // La valeur en dp que vous souhaitez convertir
        val density = resources.displayMetrics.density
        val fireHeight = (dpValue_fire * density + 0.5f).toInt()

        val initialY = -fireHeight.toFloat()
        val finalY = screenHeight.toFloat()

        var fireViews = listOf(
            findViewById<ImageView>(R.id.fire),
            findViewById<ImageView>(R.id.fire2),
            findViewById<ImageView>(R.id.fire3),
            // Ajoutez les autres ImageView des boules de feu ici
        )

        //fireViews = fireViews.shuffled()

        var animators = mutableListOf<Animator>()
        //var delai = arrayOf(500L, 1000L, 1500L, 500L)

        delayList.shuffle()

        fireViews.forEachIndexed { index, imageView ->
            val animator = ObjectAnimator.ofFloat(imageView, "translationY", initialY, finalY)
            animator.duration = duration // Durée de l'animation en millisecondes
            animator.repeatCount = ValueAnimator.RESTART
            animator.interpolator = LinearInterpolator()
            //delayList[0] = delai[index]

            animator.startDelay = delayList[index] // Définit le délai de démarrage de l'animation

            animator.addUpdateListener { animation ->
                val currentValue = animation.animatedValue as Float + fireHeight

                if (chat.y < currentValue && currentValue < chat.y + chat.height) {
                    // Collision détectée avec le chat
                    //println(chat.x)
                    if ( chat.x < screenWidth / 3 && !collisionLeft && index == 0) {
                        collisionLeft = true
                        life -= 1
                        life()

                        imageView.translationY = initialY
                        imageView.visibility = View.INVISIBLE
                        animator.cancel()

                    }else if ( screenWidth / 3 < chat.x && chat.x < 2*screenWidth / 3 && !collisionCenter && index == 1) {
                        collisionCenter = true
                        life -= 1
                        life()
                        imageView.translationY = initialY
                        imageView.visibility = View.INVISIBLE
                        animator.cancel()

                    }else if (chat.x > 2*screenWidth / 3 && !collisionRight && index == 2) {
                        collisionRight = true
                        life -= 1
                        life()

                        imageView.translationY = initialY
                        imageView.visibility = View.INVISIBLE
                        animator.cancel()

                    }
                }
                if(currentValue > chat.y + chat.height){
                    collisionLeft = false
                    collisionRight = false
                    collisionCenter = false
                }
            }

            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    imageView.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    //println("Fin d'animation")
                    //collisionLeft = false
                    //imageView.visibility = View.VISIBLE
                    println("Rends visible merde")
                    val randomValue = (0..5).random()
                    //val randomIndex = delai.indices.random()
                    animation.setDuration(duration)
                    animation.startDelay = delayList[index]

                    animation.start()

                }

                override fun onAnimationCancel(animation: Animator) {
                    // Rien à faire ici
                    println("Collision detecté")
                    imageView.visibility = View.INVISIBLE

                    //animation.start()
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // Rien à faire ici
                }
            })

            animators.add(animator)
        }
        //animators = animators.shuffled().toMutableList()

        animators.forEach { it.start() }

        //doSomethingEveryTwoSeconds(animators)
    }

    private fun shuffleDelay(){
        val handler = Handler()
        val delayMillis = 500L // 0,5 secondes

        val runnable = object : Runnable {
            override fun run() {
                //on diminue les temps de defilement de
                delayList.shuffle()
                handler.postDelayed(this, delayMillis)
            }
        }

        // Planifier la première exécution dans 2 secondes
        handler.postDelayed(runnable, delayMillis)
    }

    private fun riseDifficulty() {
        val handler = Handler()
        val delayMillis = 10000L // 2 secondes

        val runnable = object : Runnable {
            override fun run() {
                //on diminue les temps de defilement de
                if(duration > 1000){
                    duration -= 300
                }
                //arrayDuration = arrayDuration.map { if (it > 1000) it - 100 else 1100 }.toTypedArray()
                // Planifier la prochaine exécution dans 1 secondes
                handler.postDelayed(this, delayMillis)
            }
        }

        // Planifier la première exécution dans 2 secondes
        handler.postDelayed(runnable, delayMillis)
    }

    private fun score() {
        val delayMillis = 500L // 2 secondes

        val runnable = object : Runnable {
            override fun run() {
                //on diminue les temps de defilement de
                println("Score")
                score += 3
                val scoreTextView = findViewById<TextView>(R.id.scoregame2)
                scoreTextView.text = "SCORE\n$score"
                // Planifier la prochaine exécution dans 1 secondes
                handler.postDelayed(this, delayMillis)
            }
        }

        // Planifier la première exécution dans 2 secondes
        handler.postDelayed(runnable, delayMillis)
    }

    private fun stopScoreTimer() {
        // Arrête l'exécution périodique de la fonction score()
        handler.removeCallbacksAndMessages(null)
    }

    private fun life(){
        if (life == 4 ){
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart5)
            heart.visibility = View.INVISIBLE

        }else if(life == 3){
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart4)
            heart.visibility = View.INVISIBLE

        }else if(life == 2){
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart3)
            heart.visibility = View.INVISIBLE

        }else if(life == 1){
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart2)
            heart.visibility = View.INVISIBLE

        }else if(life == 0){
            vibratePhone(this, 200)
            val heart = findViewById<ImageView>(R.id.heart1)
            heart.visibility = View.INVISIBLE
            val dialogView = LayoutInflater.from(this).inflate(R.layout.endgame2, null)
            //val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
            //val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
            val replay = dialogView.findViewById<Button>(R.id.replay)
            val quit = dialogView.findViewById<Button>(R.id.quitgame2)
            stopScoreTimer()

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            replay.setOnClickListener {
                dialog.dismiss()

                score = 0
                val scoreTextView = findViewById<TextView>(R.id.scoregame2)
                scoreTextView.text = "SCORE\n$score"

                life = 5
                score()
                val heart2 = findViewById<ImageView>(R.id.heart2)
                val heart3 = findViewById<ImageView>(R.id.heart3)
                val heart4 = findViewById<ImageView>(R.id.heart4)
                val heart5 = findViewById<ImageView>(R.id.heart5)
                heart.visibility = View.VISIBLE
                heart2.visibility = View.VISIBLE
                heart3.visibility = View.VISIBLE
                heart4.visibility = View.VISIBLE
                heart5.visibility = View.VISIBLE

                duration = 3000

                arrayDuration = arrayOf(2000L,2500L, 3000L,3500, 4000)

            }
            quit.setOnClickListener {
                val intent = Intent(applicationContext, Game2::class.java)
                startActivity(intent)
                finish()
            }

            dialog.show()

        }
    }

    // Fonction pour faire vibrer le téléphone
    private fun vibratePhone(context: Context, milliseconds: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Vérifie si le périphérique prend en charge la vibration
        if (vibrator.hasVibrator()) {
            // Vibre pendant le nombre de millisecondes spécifié
            vibrator.vibrate(milliseconds)
        }
    }
}

