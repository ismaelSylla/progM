package com.example.myapplication.game6

class QuizS (var numero_son: Int, var answer1: String, var answer2: String, var answer3: String, var correctAnswer: Int) {
    fun isCorrect(answerNumber: Int) : Boolean {
        if(answerNumber == correctAnswer){
            return true
        }
        return false
    }
}