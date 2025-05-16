import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.quiz.QuizViewModel

@Composable
fun QuizResultsSection(
    courseId: String,
    courseViewModel: CourseViewModel = hiltViewModel(),
    quizViewModel: QuizViewModel = hiltViewModel()
) {
    val quiz by quizViewModel.quiz.collectAsState()
    val results by quizViewModel.allResults.collectAsState()
    val isLoading by quizViewModel.loading.collectAsState()
    val error by quizViewModel.error.collectAsState()
    val enrichedResults = remember { mutableStateListOf<Triple<String, Int, Boolean>>() } // name, score, pass

    LaunchedEffect(courseId) {
        quizViewModel.loadQuiz(courseId)
    }

    LaunchedEffect(quiz?.id) {
        quiz?.id?.let { quizViewModel.getAllResultsForQuiz(it) }
    }

    LaunchedEffect(results) {
        enrichedResults.clear()
        results.forEach { result ->
            val user = courseViewModel.userRepository.getUserById(result.studentId).getOrNull()
            val name = user?.displayName ?: "Student"
            val passThreshold = quiz?.passPercentage ?: 50
            val percentage = (result.score.toFloat() / (quiz?.totalMarks ?: 1)) * 100
            val passed = percentage >= passThreshold
            enrichedResults += Triple(name, result.score, passed)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text("Quiz Results", style = MaterialTheme.typography.titleMedium)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (enrichedResults.isEmpty()) {
            Text("No quiz submissions yet.", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }

        val avgScore = if (enrichedResults.isNotEmpty()) enrichedResults.sumOf { it.second } / enrichedResults.size else 0
        val topScorer = enrichedResults.maxByOrNull { it.second }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            InfoStat("Average Score", "$avgScore")
            topScorer?.let { InfoStat("Topper", it.first) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(enrichedResults) { (name, score, passed) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                        Text("Score: $score / ${quiz?.totalMarks ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = if (passed) "PASS" else "FAIL",
                        color = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun InfoStat(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
