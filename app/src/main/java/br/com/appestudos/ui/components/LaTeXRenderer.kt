package br.com.appestudos.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import br.com.appestudos.data.model.LatexExpression

@Composable
fun LaTeXRenderer(
    text: String,
    latexExpressions: List<LatexExpression> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    val processedText = remember(text, latexExpressions) {
        processTextWithLatex(text, latexExpressions)
    }
    
    if (latexExpressions.isEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
        )
    } else {
        Column(modifier = modifier) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(false)
                        setBackgroundColor(backgroundColor)
                    }
                },
                update = { webView ->
                    val htmlContent = createLatexHtml(processedText, backgroundColor, textColor)
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun LaTeXInput(
    text: String,
    onTextChange: (String) -> Unit,
    onLatexExpressionDetected: (List<LatexExpression>) -> Unit,
    label: String = "Texto com LaTeX",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RichTextEditor(
            text = text,
            onTextChange = { newText ->
                onTextChange(newText)
                val latexExpressions = extractLatexExpressions(newText)
                onLatexExpressionDetected(latexExpressions)
            },
            label = label,
            placeholder = "Use \$\$ para LaTeX em bloco ou \$ para inline. Ex: \$\$E = mc^2\$\$"
        )
        
        Text(
            text = "Dica: Use \$\$ para fórmulas em bloco e \$ para fórmulas inline",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun extractLatexExpressions(text: String): List<LatexExpression> {
    val expressions = mutableListOf<LatexExpression>()
    
    val blockPattern = Regex("\\$\\$(.*?)\\$\\$")
    val inlinePattern = Regex("(?<!\\$)\\$(?!\\$)(.*?)(?<!\\$)\\$(?!\\$)")
    
    blockPattern.findAll(text).forEach { match ->
        expressions.add(
            LatexExpression(
                start = match.range.first,
                end = match.range.last + 1,
                expression = match.groupValues[1]
            )
        )
    }
    
    inlinePattern.findAll(text).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1
        
        val isOverlapping = expressions.any { existing ->
            start >= existing.start && start <= existing.end ||
            end >= existing.start && end <= existing.end
        }
        
        if (!isOverlapping) {
            expressions.add(
                LatexExpression(
                    start = start,
                    end = end,
                    expression = match.groupValues[1]
                )
            )
        }
    }
    
    return expressions.sortedBy { it.start }
}

private fun processTextWithLatex(text: String, latexExpressions: List<LatexExpression>): String {
    var processedText = text
    
    latexExpressions.sortedByDescending { it.start }.forEach { latex ->
        val before = processedText.substring(0, latex.start)
        val after = processedText.substring(latex.end)
        val latexHtml = if (latex.expression.contains("$$")) {
            "\\[${latex.expression}\\]"
        } else {
            "\\(${latex.expression}\\)"
        }
        processedText = before + latexHtml + after
    }
    
    return processedText
}

private fun createLatexHtml(content: String, backgroundColor: Int, textColor: Int): String {
    val bgHex = String.format("#%06X", (0xFFFFFF and backgroundColor))
    val textHex = String.format("#%06X", (0xFFFFFF and textColor))
    
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script type="text/javascript" async
                src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML">
            </script>
            <script type="text/x-mathjax-config">
                MathJax.Hub.Config({
                    tex2jax: {
                        inlineMath: [['\$','\$'], ['\\\\(','\\\\)']],
                        displayMath: [['\$\$','\$\$'], ['\\\\[','\\\\]']],
                        processEscapes: true
                    },
                    CommonHTML: { scale: 100 },
                    "HTML-CSS": { scale: 100 }
                });
            </script>
            <style>
                body {
                    background-color: ${bgHex};
                    color: ${textHex};
                    font-family: 'Roboto', sans-serif;
                    margin: 8px;
                    padding: 0;
                    font-size: 16px;
                    line-height: 1.5;
                }
                .MathJax {
                    color: ${textHex} !important;
                }
            </style>
        </head>
        <body>
            ${content}
        </body>
        </html>
    """.trimIndent()
}