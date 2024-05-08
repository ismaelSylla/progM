package com.example.myapplication.game6

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import kotlin.random.Random

class SonActivity : AppCompatActivity() {
    var tab_nbr_son: IntArray = intArrayOf(1, 2, 3, 4, 5, 6)

    var quizson = ArrayList<QuizS>()
    var recup_nbr_son: Int = Random.nextInt(tab_nbr_son.size)
    var numberOfGoodAnswers: Int = 0
    var compte_q: Int = 0
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 15000
    lateinit var mediaPlayer: MediaPlayer

    lateinit var mediaPlayer_suc: MediaPlayer
    lateinit var mediaPlayer_fail: MediaPlayer

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_son)

        mediaPlayer = MediaPlayer()

        // Initialize MediaPlayer
        mediaPlayer_suc = MediaPlayer.create(this, R.raw.succes)
        mediaPlayer_fail = MediaPlayer.create(this, R.raw.fail)

        quizson.add(QuizS(1, "Billie Jean", "Beat It", "Soon", 2))
        quizson.add(QuizS(2, "Hello", "Can't Let Go", "All I Ask", 1))
        quizson.add(QuizS(3, "Ma beauté", "Tu vas me manquer", "Bella", 3))

        quizson.add(QuizS(4, "Generation Chilley", "Coup du marteau", "Un pas de danse", 2))
        quizson.add(QuizS(5, "O'cho", "Lollipop", "Valise", 1))
        quizson.add(QuizS(6, "Du ferme", "Placebo", "Incassable", 3))


        showQuestion(quizson.get(tab_nbr_son[recup_nbr_son]-1))
    }

    fun remove(arr: IntArray, index: Int): IntArray {
        if (index < 0 || index >= arr.size) {
            return arr
        }
        val result = arr.toMutableList()
        result.removeAt(index)
        return result.toIntArray()
    }

    fun playSon(view: View){
        val soundResourceId = when (tab_nbr_son[recup_nbr_son]) {
            1 -> R.raw.son1_beat
            2 -> R.raw.son2_adele
            3 -> R.raw.son3_bella
            4 -> R.raw.son4_coup
            5 -> R.raw.son5_ocho
            else -> R.raw.son6_incassable
        }
        mediaPlayer = MediaPlayer.create(this, soundResourceId)
        mediaPlayer.start()
        startTimer()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                showTimeUpAlert()
            }
        }.start()
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val temps = findViewById<TextView>(R.id.temps)
        temps.setText(String.format("%02ds",seconds))
        //temps.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun showTimeUpAlert() {
        val temp = findViewById<TextView>(R.id.temps)
        temp.setText("15s")
        countDownTimer?.cancel()
        timeLeftInMillis = 15000
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        tab_nbr_son = remove(tab_nbr_son, recup_nbr_son)
        recup_nbr_son = Random.nextInt(tab_nbr_son.size)
        showQuestion(quizson.get(tab_nbr_son[recup_nbr_son]-1))
    }

    fun handleAnwser(answerID: Int) {
        val temp = findViewById<TextView>(R.id.temps)
        temp.setText("15s")
        countDownTimer?.cancel()
        timeLeftInMillis = 15000
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        val quiz = quizson.get(tab_nbr_son[recup_nbr_son] - 1)

        if (quiz.isCorrect(answerID)) {
            numberOfGoodAnswers++
            Toast.makeText(this, "+1 point", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "0 point", Toast.LENGTH_SHORT).show()
        }

        if(compte_q == 4){
            var alert = AlertDialog.Builder(this)
            alert.setTitle("Partie terminée")
            if(numberOfGoodAnswers >= 2) {
                mediaPlayer_suc.start()
                alert.setMessage("-----\nVotre score: " + numberOfGoodAnswers + "/" + 4 + "\n-----\n" +
                        "Bien joué !")
            } else {
                mediaPlayer_fail.start()
                alert.setMessage("-----\nVotre score: " + numberOfGoodAnswers + "/" + 4 + "\n-----\n" +
                        "Pas Bon !")
            }
            alert.setPositiveButton("OK") { dialogInterface: DialogInterface?, i: Int ->
                val intent = Intent( applicationContext, MainActivity::class.java )
                startActivity(intent)
                finish()
            }
            alert.show()
        }
        else {
            tab_nbr_son = remove(tab_nbr_son, recup_nbr_son)
            recup_nbr_son = Random.nextInt(tab_nbr_son.size)
            showQuestion(quizson.get(tab_nbr_son[recup_nbr_son]-1))
        }
    }

    fun onOne(view: View){
        handleAnwser(1)
    }

    fun onTwo(view: View){
        handleAnwser(2)
    }

    fun onThree(view: View){
        handleAnwser(3)
    }

    fun showQuestion(quiz: QuizS){
        compte_q = compte_q + 1
        val answer1 = findViewById<TextView>(R.id.answerS1)
        val answer2 = findViewById<TextView>(R.id.answerS2)
        val answer3 = findViewById<TextView>(R.id.answerS3)
        answer1.setText(quiz.answer1)
        answer2.setText(quiz.answer2)
        answer3.setText(quiz.answer3)
    }
}