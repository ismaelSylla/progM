package com.example.myapplication.game5

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


import android.media.MediaPlayer
import com.example.myapplication.MainActivity
import com.example.myapplication.R

import kotlin.random.Random

class QuizActivity : AppCompatActivity() {
    var tab_nbr_q: IntArray = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)


    var recup_ind_q: Int = Random.nextInt(tab_nbr_q.size)
    var quizs = ArrayList<Quiz>()
    var numberOfGoodAnswers: Int = 0
    var compte_q: Int = 0

    var random = Random.nextInt(10)

    lateinit var mediaPlayer: MediaPlayer
    lateinit var mediaPlayer2: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.quiz)

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.succes)
        mediaPlayer2 = MediaPlayer.create(this, R.raw.fail)

        quizs.add(Quiz("Quel est le plus grand désert du monde ?", "Sahara", "Antarctique", "Gobi", 1))
        quizs.add(Quiz("Qui a écrit \"Le Petit Prince\" ?", "Victor Hugo", " Antoine de Saint-Exupéry", "Jules Verne", 2))
        quizs.add(Quiz("Quel est le plus grand océan du monde ?", "Atlantique", "Indien", "Pacifique", 3))
        quizs.add(Quiz("Qui a peint \"La Joconde\" ?", "Michel-Ange", "Léonard de Vinci", "Vincent van Gogh", 2))
        quizs.add(Quiz("Quelle est la capitale du Canada ?", "Toronto", "Montréal", "Ottawa", 3))

        quizs.add(Quiz("Quel est le plus grand mammifère terrestre ?", "Éléphant", "Baleine bleue", "Girafe", 1))
        quizs.add(Quiz("Qui a découvert la pénicilline ?", "Alexander Fleming", "Louis Pasteur", "Isaac Newton", 1))
        quizs.add(Quiz("Quel est le plus haut sommet du monde ?", "K2", "Mont Everest", "Mont Kilimandjaro", 2))
        quizs.add(Quiz("Qui a écrit \"Hamlet\" ?", "William Shakespeare", "Charles Dickens", "Jane Austen", 1))
        quizs.add(Quiz("Quelle est la plus grande planète du système solaire ?", "Uranus", "Saturne", "Jupiter", 3))


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.quiz)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //showQuestion(quizs.get(currentQuizIndex))
        showQuestion(quizs.get(tab_nbr_q[recup_ind_q]-1))
    }

    fun showQuestion(quiz: Quiz){
        compte_q = compte_q + 1
        var texte = "Question " + compte_q
        val titleQuestion = findViewById<TextView>(R.id.titleQuestion)
        val txtQuestion = findViewById<TextView>(R.id.txtQuestion)
        val answer1 = findViewById<TextView>(R.id.answer1)
        val answer2 = findViewById<TextView>(R.id.answer2)
        val answer3 = findViewById<TextView>(R.id.answer3)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
// Pour augmenter la progression de 10
        if(compte_q > 1) {
            progressBar.incrementProgressBy(20)
        }


        titleQuestion.setText(texte)
        txtQuestion.setText(quiz.question)
        answer1.setText(quiz.answer1)
        answer2.setText(quiz.answer2)
        answer3.setText(quiz.answer3)
    }

    fun handleAnwser(answerID: Int){
        //val quiz = quizs.get(currentQuizIndex)
        //val quiz = quizs.get(random)
        val quiz = quizs.get(tab_nbr_q[recup_ind_q] - 1)

        if(quiz.isCorrect(answerID)){
            numberOfGoodAnswers ++
            Toast.makeText(this, "+1", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "+0", Toast.LENGTH_SHORT).show()
        }

        //currentQuizIndex++
        //if(currentQuizIndex == quizs.size){

        if(compte_q == 5){
            var alert = AlertDialog.Builder(this)
            alert.setTitle("Partie terminée")
            if(numberOfGoodAnswers >= 3) {
                mediaPlayer.start()
                alert.setMessage("-----\nVotre score: " + numberOfGoodAnswers + "/" + 5 + "\n-----\n" +
                        "Bien joué !")
            } else {
                mediaPlayer2.start()
                alert.setMessage("-----\nVotre score: " + numberOfGoodAnswers + "/" + 5 + "\n-----\n" +
                        "Pas bon !")
            }
            alert.setPositiveButton("OK") { dialogInterface: DialogInterface?, i: Int ->
                val intent = Intent( applicationContext, MainActivity::class.java )
                startActivity(intent)
                finish()
            }
            alert.show()
        }
        else {/*
            random = Random.nextInt(15)
            //showQuestion(quizs.get(currentQuizIndex))
            showQuestion(quizs.get(random))*/
            tab_nbr_q = remove(tab_nbr_q, recup_ind_q)
            recup_ind_q = Random.nextInt(tab_nbr_q.size)
            showQuestion(quizs.get(tab_nbr_q[recup_ind_q]-1))
        }
    }

    fun remove(arr: IntArray, index: Int): IntArray {
        if (index < 0 || index >= arr.size) {
            return arr
        }
        val result = arr.toMutableList()
        result.removeAt(index)
        return result.toIntArray()
    }

    fun onOne(view: View){
        handleAnwser(1)
    }

    fun onTwo(view: View){
        handleAnwser(2)
    }

    fun onTree(view: View){
        handleAnwser(3)
    }
}