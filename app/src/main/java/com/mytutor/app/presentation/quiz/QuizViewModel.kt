package com.mytutor.app.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytutor.app.data.remote.models.Quiz
import com.mytutor.app.data.remote.models.QuizQuestion
import com.mytutor.app.data.remote.models.QuizResult
import com.mytutor.app.data.remote.repository.QuizRepository
import com.mytutor.app.data.remote.repository.QuizResultRepository
import com.mytutor.app.domain.usecase.SubmitQuizUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val quizResultRepository: QuizResultRepository,
    private val submitQuizUseCase: SubmitQuizUseCase
) : ViewModel() {

    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions

    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult

    private val _allResults = MutableStateFlow<List<QuizResult>>(emptyList())
    val allResults: StateFlow<List<QuizResult>> = _allResults

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadQuiz(courseId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizRepository.getQuizByCourseId(courseId)
            result.fold(
                onSuccess = { _quiz.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun loadQuizQuestions(quizId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizRepository.getQuestionsByQuizId(quizId)
            result.fold(
                onSuccess = { _questions.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun submitAnswers(
        quizId: String,
        studentId: String,
        answers: Map<String, String>
    ) {
        _loading.value = true
        viewModelScope.launch {
            val questions = _questions.value
            val result = submitQuizUseCase(quizId, studentId, questions, answers)
            result.fold(
                onSuccess = { _quizResult.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun getStudentResult(quizId: String, studentId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizResultRepository.getQuizResult(quizId, studentId)
            result.fold(
                onSuccess = { _quizResult.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun getAllResultsForQuiz(quizId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizResultRepository.getAllResultsForQuiz(quizId)
            result.fold(
                onSuccess = { _allResults.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun createQuiz(quiz: Quiz, onSuccess: (String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizRepository.createQuiz(quiz)
            result.fold(
                onSuccess = {
                    _quiz.value = quiz.copy(id = it)
                    onSuccess(it)
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun addQuestionToQuiz(question: QuizQuestion, onSuccess: () -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizRepository.addQuestionToQuiz(question)
            result.fold(
                onSuccess = {
                    _questions.value = _questions.value + question
                    onSuccess()
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }
    fun updateQuiz(quiz: Quiz, onSuccess: () -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizRepository.updateQuiz(quiz)
            result.fold(
                onSuccess = {
                    _quiz.value = quiz
                    onSuccess()
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun deleteQuiz(quizId: String, onSuccess: () -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val result = quizRepository.deleteQuiz(quizId)
            result.fold(
                onSuccess = {
                    _quiz.value = null
                    _questions.value = emptyList()
                    onSuccess()
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

}
