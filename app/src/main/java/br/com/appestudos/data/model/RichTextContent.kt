package br.com.appestudos.data.model

data class RichTextContent(
    val text: String,
    val formatting: List<TextFormatting> = emptyList(),
    val latexExpressions: List<LatexExpression> = emptyList()
)

data class TextFormatting(
    val start: Int,
    val end: Int,
    val type: FormattingType,
    val value: String? = null
)

enum class FormattingType {
    BOLD,
    ITALIC,
    UNDERLINE,
    COLOR,
    SIZE,
    HIGHLIGHT
}

data class LatexExpression(
    val start: Int,
    val end: Int,
    val expression: String
)