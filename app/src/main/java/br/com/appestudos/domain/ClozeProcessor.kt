package br.com.appestudos.domain

import br.com.appestudos.data.model.RichTextContent

data class ClozeCard(
    val originalText: String,
    val processedText: String,
    val blanks: List<ClozeBlank>,
    val richContent: RichTextContent? = null
)

data class ClozeBlank(
    val id: Int,
    val correctAnswer: String,
    val alternatives: List<String> = emptyList(),
    val hint: String? = null,
    val startPosition: Int,
    val endPosition: Int
)

object ClozeProcessor {
    private val clozePattern = Regex("\\{\\{c(\\d+)::(.*?)(?:::(.*?))?\\}\\}")
    
    fun processText(text: String): ClozeCard {
        val blanks = mutableListOf<ClozeBlank>()
        var processedText = text
        var offset = 0

        clozePattern.findAll(text).forEach { match ->
            val clozeId = match.groupValues[1].toInt()
            val answer = match.groupValues[2]
            val hint = match.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }
            
            val startPos = match.range.first - offset
            val blankText = "_".repeat(minOf(answer.length, 15))
            
            blanks.add(
                ClozeBlank(
                    id = clozeId,
                    correctAnswer = answer,
                    hint = hint,
                    startPosition = startPos,
                    endPosition = startPos + blankText.length
                )
            )
            
            processedText = processedText.replaceFirst(match.value, blankText)
            offset += match.value.length - blankText.length
        }
        
        return ClozeCard(
            originalText = text,
            processedText = processedText,
            blanks = blanks.sortedBy { it.id }
        )
    }
    
    fun validateAnswer(blank: ClozeBlank, userAnswer: String): Boolean {
        val normalizedCorrect = blank.correctAnswer.trim().lowercase()
        val normalizedUser = userAnswer.trim().lowercase()
        return normalizedCorrect == normalizedUser || 
               isSemanticallySimilar(normalizedCorrect, normalizedUser)
    }
    
    private fun isSemanticallySimilar(correct: String, user: String): Boolean {
        if (levenshteinDistance(correct, user) <= 2 && correct.length > 3) {
            return true
        }
        
        val correctWords = correct.split("\\s+".toRegex())
        val userWords = user.split("\\s+".toRegex())
        
        if (correctWords.size == userWords.size) {
            val matchCount = correctWords.zip(userWords).count { (c, u) -> 
                c == u || levenshteinDistance(c, u) <= 1
            }
            return matchCount.toDouble() / correctWords.size >= 0.8
        }
        
        return false
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[m][n]
    }
    
    fun generateHints(answer: String): List<String> {
        return listOf(
            "Primeira letra: ${answer.firstOrNull()?.uppercase() ?: ""}",
            "Quantidade de letras: ${answer.length}",
            "Termina com: ${answer.lastOrNull()?.uppercase() ?: ""}",
            if (answer.contains(" ")) "Contém ${answer.count { it == ' ' } + 1} palavras" else "Uma palavra"
        ).filter { it.isNotBlank() }
    }
}