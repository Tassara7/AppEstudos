package br.com.appestudos.data.service

import android.util.Log
import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.DifficultyLevel
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.FlashcardType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Date

data class FlashcardExportData(
    val deckId: Long,
    val deckName: String,
    val flashcards: List<Flashcard>
)

class FlashcardImportExportService {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    suspend fun exportDeckToJson(deck: Deck, flashcards: List<Flashcard>, file: File): Result<String> {
        return try {
            val exportData = FlashcardExportData(
                deckId = deck.id,
                deckName = deck.name,
                flashcards = flashcards
            )
            
            FileWriter(file).use { writer ->
                gson.toJson(exportData, writer)
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Log.e("ImportExportService", "Erro ao exportar para JSON", e)
            Result.failure(e)
        }
    }

    suspend fun exportDeckToCsv(deck: Deck, flashcards: List<Flashcard>, file: File): Result<String> {
        return try {
            FileWriter(file).use { writer ->
                // Cabeçalho CSV
                writer.write("Tipo,Frente,Verso,Cloze Conteudo,Cloze Respostas,Pergunta MC,Opcoes MC,Resposta Correta,Indice Resposta,Explicacao,Tags\n")
                
                flashcards.forEach { flashcard ->
                    val row = listOf(
                        flashcard.type.name,
                        flashcard.frontContent.let { escapeCsv(it) },
                        flashcard.backContent.let { escapeCsv(it) },
                        flashcard.clozeContent?.let { escapeCsv(it) } ?: "",
                        flashcard.clozeAnswers?.joinToString(";")?.let { escapeCsv(it) } ?: "",
                        flashcard.multipleChoiceQuestion?.let { escapeCsv(it) } ?: "",
                        flashcard.multipleChoiceOptions?.joinToString(";")?.let { escapeCsv(it) } ?: "",
                        flashcard.correctAnswer.let { escapeCsv(it) },
                        flashcard.correctAnswerIndex?.toString() ?: "",
                        flashcard.explanation?.let { escapeCsv(it) } ?: "",
                        flashcard.tags.joinToString(";").let { escapeCsv(it) }
                    ).joinToString(",")
                    
                    writer.write("$row\n")
                }
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Log.e("ImportExportService", "Erro ao exportar para CSV", e)
            Result.failure(e)
        }
    }

    suspend fun importFromJson(file: File, deckId: Long): Result<List<Flashcard>> {
        return try {
            val type = object : TypeToken<FlashcardExportData>() {}.type
            val exportData: FlashcardExportData = FileReader(file).use { reader ->
                gson.fromJson(reader, type)
            }
            
            val flashcards = exportData.flashcards.map { flashcard ->
                flashcard.copy(
                    id = 0L, // Novo ID será gerado
                    deckId = deckId
                )
            }
            
            Result.success(flashcards)
        } catch (e: Exception) {
            Log.e("ImportExportService", "Erro ao importar JSON", e)
            Result.failure(e)
        }
    }

    suspend fun importFromCsv(file: File, deckId: Long): Result<List<Flashcard>> {
        return try {
            val flashcards = mutableListOf<Flashcard>()
            
            FileReader(file).use { reader ->
                val lines = reader.readLines()
                
                // Pular cabeçalho
                lines.drop(1).forEach { line ->
                    val fields = parseCsvLine(line)
                    if (fields.size >= 11) {
                        val flashcard = createFlashcardFromCsv(fields, deckId)
                        flashcards.add(flashcard)
                    }
                }
            }
            
            Result.success(flashcards)
        } catch (e: Exception) {
            Log.e("ImportExportService", "Erro ao importar CSV", e)
            Result.failure(e)
        }
    }

    private fun createFlashcardFromCsv(fields: List<String>, deckId: Long): Flashcard {
        val type = try {
            FlashcardType.valueOf(fields[0])
        } catch (e: Exception) {
            FlashcardType.FRONT_AND_VERSO
        }
        
        return Flashcard(
            id = 0L,
            deckId = deckId,
            type = type,
            frontContent = fields[1],
            backContent = fields[2],
            clozeContent = fields[3].takeIf { it.isNotBlank() },
            clozeAnswers = fields[4].takeIf { it.isNotBlank() }?.split(";"),
            multipleChoiceQuestion = fields[5].takeIf { it.isNotBlank() },
            multipleChoiceOptions = fields[6].takeIf { it.isNotBlank() }?.split(";"),
            correctAnswer = fields[7],
            correctAnswerIndex = fields[8].takeIf { it.isNotBlank() }?.toIntOrNull(),
            explanation = fields[9].takeIf { it.isNotBlank() },
            tags = fields[10].takeIf { it.isNotBlank() }?.split(";") ?: emptyList(),
            nextReviewDate = Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000)),
            interval = 1,
            easeFactor = 2.5f,
            repetitions = 0,
            difficulty = DifficultyLevel.MEDIUM
        )
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\"" 
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            
            when {
                char == '"' && !inQuotes -> {
                    inQuotes = true
                }
                char == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // Pular próximo quote
                    } else {
                        inQuotes = false
                    }
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        
        result.add(current.toString())
        return result
    }
}

enum class ExportFormat {
    JSON, CSV
}