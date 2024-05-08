package com.example.myapplication.game5

class Quiz (var question: String, var answer1: String, var answer2: String, var answer3: String, var correctAnswer: Int) {
    fun isCorrect(answerNumber: Int) : Boolean {
        if(answerNumber == correctAnswer){
            return true
        }
        return false
    }
}