package com.example.myapplication.game4

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.game1.Game1

class ModeSolo4 : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var shakeCount = 0
    private lateinit var scoreTextView: TextView
    private  var lastX = 0f
    private  var lastY = 0f
    private  var lastZ = 0f
    private lateinit var countDownTimer: CountDownTimer
    private var finish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mode_solo_game4)

        scoreTextView = findViewById(R.id.scoreTextView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lastUpdate = System.currentTimeMillis()

        startCountDownTimer()

        val quitButton = findViewById<Button>(R.id.quit)

        quitButton.setOnClickListener{
            val intent = Intent(applicationContext, Game4::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.modesolo4)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startCountDownTimer() {
        val timerTextView = findViewById<TextView>(R.id.timer)

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Met à jour le texte du TextView avec le temps restant
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Temps restant : $secondsLeft s"
            }

            override fun onFinish() {
                showGameOverDialog()
            }
        }
        // Démarre le compte à rebours
        countDownTimer.start()
    }

    private fun showGameOverDialog() {
        finish = true
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_over, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.text_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.text_message)
        val quitButton = dialogView.findViewById<Button>(R.id.button_quit)
        val resumeButton = dialogView.findViewById<Button>(R.id.button_resume)

        titleTextView.text = "Fin de la partie"
        messageTextView.text = "Score: $shakeCount"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        quitButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(applicationContext, Game4::class.java)
            startActivity(intent)
            finish()
        }

        resumeButton.setOnClickListener {
            dialog.dismiss()
            startCountDownTimer()
            val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
            finish=false
            shakeCount = 0
            scoreTextView.text = "Score $shakeCount"
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastUpdate) > SHAKE_INTERVAL) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000
                if (speed > SHAKE_THRESHOLD && !finish) {
                    shakeCount++
                    scoreTextView.text = "Score: $shakeCount"
                }
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrête le compte à rebours pour éviter les fuites de mémoire
        countDownTimer.cancel()

    }

    companion object {
        private const val SHAKE_THRESHOLD = 500
        private const val SHAKE_INTERVAL = 500
    }
}
