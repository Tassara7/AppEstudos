package br.com.appestudos.domain

object EducationalPrompts {
    
    const val FLASHCARD_GENERATOR_SYSTEM = """
        Você é um assistente educacional especializado em criar flashcards de alta qualidade.
        Suas características:
        - Cria conteúdo educativo preciso e bem estruturado
        - Adapta o nível de dificuldade conforme solicitado
        - Fornece explicações claras e contextualizadas
        - Usa exemplos práticos e relevantes
        - Segue as melhores práticas de aprendizagem espaçada
    """
    
    fun generateFlashcardsPrompt(
        topic: String,
        quantity: Int,
        difficulty: String,
        language: String = "português",
        type: String = "tradicional"
    ): String {
        return """
            Crie $quantity flashcards sobre "$topic" em $language.
            
            Configurações:
            - Nível de dificuldade: $difficulty
            - Tipo: $type
            - Foque em conceitos fundamentais e aplicações práticas
            
            Para cada flashcard, forneça:
            1. Pergunta (frente): Clara e específica
            2. Resposta (verso): Completa mas concisa
            3. Explicação adicional: Contexto ou exemplo quando necessário
            4. Tags: 3-5 palavras-chave relevantes
            
            Formato de resposta:
            ```
            FLASHCARD 1:
            Pergunta: [pergunta aqui]
            Resposta: [resposta aqui]
            Explicação: [explicação adicional]
            Tags: tag1, tag2, tag3
            
            FLASHCARD 2:
            ...
            ```
        """.trimIndent()
    }
    
    fun validateAnswerPrompt(
        question: String,
        correctAnswer: String,
        userAnswer: String
    ): String {
        return """
            Analise a resposta do estudante para esta questão educacional:
            
            QUESTÃO: $question
            RESPOSTA CORRETA: $correctAnswer
            RESPOSTA DO ESTUDANTE: $userAnswer
            
            Forneça uma análise detalhada no seguinte formato:
            
            STATUS: [CORRETO/PARCIALMENTE_CORRETO/INCORRETO]
            PONTUAÇÃO: [0-100]
            FEEDBACK: [Explicação educativa sobre a resposta, destacando acertos e erros]
            SUGESTÕES: [Dicas para melhorar ou estudar mais, se necessário]
            
            Critérios de avaliação:
            - CORRETO (90-100): Resposta precisa e completa
            - PARCIALMENTE_CORRETO (50-89): Contém elementos corretos mas incompleta
            - INCORRETO (0-49): Resposta errada ou irrelevante
            
            Seja construtivo e educativo no feedback, sempre incentivando o aprendizado.
        """.trimIndent()
    }
    
    fun generateHintPrompt(
        question: String,
        answer: String,
        hintLevel: Int
    ): String {
        val hintInstruction = when (hintLevel) {
            1 -> "Dê uma dica conceitual geral, sem revelar a resposta"
            2 -> "Forneça uma dica mais específica, mas ainda sem dar a resposta diretamente"
            3 -> "Dê uma dica bem direcionada que ajude significativamente"
            else -> "Forneça orientação para estudar o tópico"
        }
        
        return """
            Para a seguinte questão educacional:
            QUESTÃO: $question
            RESPOSTA: $answer
            
            $hintInstruction
            
            Nível da dica: $hintLevel/3
            
            Características da dica:
            - Educativa e construtiva
            - Progressivamente mais específica conforme o nível
            - Encoraja o pensamento crítico
            - Não revela a resposta diretamente (exceto no nível 3, que pode ser bem específica)
            - Máximo de 2-3 frases
            
            Forneça apenas a dica, sem formatação adicional.
        """.trimIndent()
    }

    fun generateMultipleChoiceOptionsPrompt(
        question: String,
        correctAnswer: String,
        subject: String? = null,
        difficulty: String = "médio"
    ): String {
        val subjectContext = subject?.let { " no contexto de $it" } ?: ""
        
        return """
            Crie 4 alternativas de múltipla escolha para a seguinte questão educacional$subjectContext:
            
            QUESTÃO: $question
            RESPOSTA CORRETA: $correctAnswer
            NÍVEL DE DIFICULDADE: $difficulty
            
            Gere exatamente 4 opções (A, B, C, D):
            - 1 opção correta (baseada na resposta fornecida)
            - 3 opções incorretas (distratores plausíveis e educativos)
            
            Critérios para os distratores:
            - Relacionados ao tópico, mas claramente incorretos
            - Não óbvios demais ou muito fáceis de eliminar
            - Representam erros conceituais comuns
            - Adequados ao nível de dificuldade especificado
            
            Formato de resposta:
            A) [primeira opção]
            Explicação: [breve explicação sobre por que está correta/incorreta]
            
            B) [segunda opção]
            Explicação: [breve explicação sobre por que está correta/incorreta]
            
            C) [terceira opção]
            Explicação: [breve explicação sobre por que está correta/incorreta]
            
            D) [quarta opção]
            Explicação: [breve explicação sobre por que está correta/incorreta]
            
            Importante: Misture a ordem para que a resposta correta não esteja sempre na mesma posição.
        """.trimIndent()
    }

    fun generateClozePrompt(
        topic: String,
        text: String? = null,
        difficulty: String = "médio"
    ): String {
        return if (text != null) {
            """
                Transforme o texto a seguir em um exercício de lacunas (cloze) sobre "$topic".
                Nível: $difficulty
                
                Texto original:
                "$text"
                
                Instruções:
                1. Identifique 3-7 conceitos-chave para transformar em lacunas
                2. Use a sintaxe: {{c1::resposta::dica_opcional}}
                3. Numere as lacunas sequencialmente (c1, c2, c3...)
                4. Inclua dicas úteis mas não óbvias
                5. Mantenha o texto fluido e natural
                
                Retorne apenas o texto processado com as lacunas.
            """.trimIndent()
        } else {
            """
                Crie um texto educativo com lacunas (cloze) sobre "$topic".
                Nível: $difficulty
                
                Requisitos:
                1. Texto de 2-4 parágrafos sobre o tópico
                2. 5-10 lacunas estratégicamente posicionadas
                3. Use a sintaxe: {{c1::resposta::dica_opcional}}
                4. Foque em conceitos fundamentais
                5. Inclua dicas educativas nas lacunas importantes
                
                Exemplo de formato:
                A fotossíntese é o processo pelo qual as {{c1::plantas::seres autotróficos}} convertem {{c2::luz solar::energia luminosa}} em energia química.
                
                Retorne apenas o texto com lacunas.
            """.trimIndent()
        }
    }
    
    fun generateMultipleChoicePrompt(
        question: String,
        topic: String,
        difficulty: String = "médio"
    ): String {
        return """
            Crie uma questão de múltipla escolha sobre "$topic".
            Nível: $difficulty
            
            Pergunta base: "$question"
            
            Requisitos:
            1. Uma pergunta clara e específica
            2. 4 alternativas (A, B, C, D)
            3. Uma resposta correta
            4. Três distratores plausíveis mas incorretos
            5. Explicação da resposta correta
            6. Breve explicação por que as outras estão erradas
            
            Formato de resposta:
            ```
            PERGUNTA: [pergunta reformulada se necessário]
            
            A) [alternativa A]
            B) [alternativa B]
            C) [alternativa C]
            D) [alternativa D]
            
            RESPOSTA CORRETA: [letra]
            
            EXPLICAÇÃO:
            Correta: [por que está correta]
            Distratores: 
            - [letra]: [por que está incorreta]
            - [letra]: [por que está incorreta]
            - [letra]: [por que está incorreta]
            ```
        """.trimIndent()
    }
    
    fun validateAnswerPrompt(
        question: String,
        correctAnswer: String,
        userAnswer: String,
        context: String? = null
    ): String {
        return """
            Analise se a resposta do estudante está correta ou parcialmente correta.
            
            Pergunta: "$question"
            Resposta esperada: "$correctAnswer"
            Resposta do estudante: "$userAnswer"
            ${if (context != null) "Contexto: $context" else ""}
            
            Avalie considerando:
            1. Correção conceitual
            2. Sinônimos e variações válidas
            3. Erros de ortografia menores
            4. Resposta parcialmente correta
            
            Forneça:
            1. Status: CORRETO, PARCIALMENTE_CORRETO, INCORRETO
            2. Pontuação: 0-100
            3. Feedback: Explicação construtiva e educativa
            4. Sugestões: Como melhorar a resposta (se aplicável)
            
            Formato:
            ```
            STATUS: [status]
            PONTUAÇÃO: [0-100]
            FEEDBACK: [feedback detalhado]
            SUGESTÕES: [sugestões de melhoria]
            ```
        """.trimIndent()
    }
    
    fun generateHintPrompt(
        question: String,
        answer: String,
        context: String? = null,
        hintLevel: Int = 1
    ): String {
        return """
            Gere uma dica progressiva para ajudar o estudante.
            
            Pergunta: "$question"
            Resposta: "$answer"
            ${if (context != null) "Contexto: $context" else ""}
            Nível da dica: $hintLevel (1=sutil, 2=moderada, 3=óbvia)
            
            Níveis de dica:
            - Nível 1: Dica conceitual ou direcionamento geral
            - Nível 2: Dica mais específica com exemplos
            - Nível 3: Dica que quase revela a resposta
            
            Forneça apenas a dica apropriada para o nível solicitado.
            Seja educativo e incentive o raciocínio do estudante.
        """.trimIndent()
    }
    
    fun generateExplanationPrompt(
        question: String,
        answer: String,
        topic: String
    ): String {
        return """
            Crie uma explicação educativa detalhada.
            
            Pergunta: "$question"
            Resposta: "$answer"
            Tópico: "$topic"
            
            A explicação deve:
            1. Explicar por que a resposta está correta
            2. Fornecer contexto adicional relevante
            3. Incluir exemplos práticos quando possível
            4. Conectar com conceitos relacionados
            5. Ser clara e educativa
            
            Mantenha um tom didático e acessível.
        """.trimIndent()
    }
    
    fun chatTutorSystemMessage(): String {
        return """
            Você é um tutor educacional inteligente e paciente. Suas características:
            
            1. PEDAGOGIA: Usa métodos socráticos - faz perguntas para guiar o aprendizado
            2. ADAPTABILIDADE: Ajusta explicações ao nível do estudante
            3. ENCORAJAMENTO: Sempre positivo e motivador
            4. PRECISÃO: Informações sempre corretas e atualizadas
            5. CLAREZA: Explicações claras e estruturadas
            
            Diretrizes:
            - Nunca dê respostas diretas imediatamente
            - Faça perguntas que levem o estudante ao entendimento
            - Use analogias e exemplos práticos
            - Celebre o progresso e esforço
            - Identifique e corrija conceitos errôneos
            - Sugira recursos adicionais quando apropriado
            
            Responda sempre em português brasileiro de forma educativa e acessível.
        """.trimIndent()
    }
}