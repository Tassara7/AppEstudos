# PRD – Plataforma de Aprendizagem Inteligente (Guia + Checklist)

Este documento serve como **fonte de verdade** para o desenvolvimento do projeto.  
A IA deve **sempre se basear neste arquivo** e marcar o checklist conforme for implementando as funcionalidades.  

---

## ✅ Checklist de Requisitos

### Arquitetura e Padrões
- [x] Projeto em **Kotlin** (Android + servidor).
- [x] Seguir **MVVM (Model-View-ViewModel)**.
- [x] Interface 100% em **Jetpack Compose** com **Material Design 3**.
- [x] Tema claro/escuro e animações fluidas.
- [x] Código **sem comentários**.
- [x] Organização em pacotes e classes bem estruturados.

### Sistema Avançado de Flashcards
- [x] Implementar **Frente e Verso** (estrutura de dados e UI).
- [x] Implementar **Cloze/Omissão** (estrutura de dados e UI).
- [x] Implementar **Digite a Resposta** (estrutura de dados e UI).
- [x] Implementar **Múltipla Escolha** (estrutura de dados e UI).
- [x] Importação/exportação de flashcards.
- [ ] Preview em tempo real na criação.
- [ ] **Funcionalidade de estudo** (executar sessões de flashcards).

### GPS Inteligente
- [x] Usuário pode favoritar até **7 localizações** (estrutura de dados).
- [x] **Geofencing** (LocationService implementado).
- [x] Relatórios de desempenho por local (UI criada).
- [ ] **Rotação inteligente** de conteúdos por localização (lógica).

### Repetição Espaçada Avançada com IA
- [x] Algoritmo baseado em **SM-2** (SpacedRepetitionScheduler).
- [ ] Ajuste adaptativo via IA.
- [ ] Predição de dificuldade de conteúdos.
- [ ] Sugestões de horários ideais para revisão.

### Assistente de Estudos com IA
- [x] Estrutura para IA (AIManager, GoogleAIService, GroqService).
- [ ] IA gera flashcards automaticamente (implementação funcional).
- [ ] IA valida respostas abertas com feedback completo.
- [ ] IA fornece dicas progressivas inteligentes.
- [ ] IA recomenda tópicos, duração e tipo de flashcards.
- [ ] IA atua como **chat educacional tutor**.
- [x] Multi-LLM com fallback (estrutura criada, sem implementação).

### Armazenamento e Backend
- [x] **Local:** SQLite/Room para offline-first.
- [ ] **Servidor:** API REST em **Ktor** (CRUD de flashcards, compartilhamento, autenticação).
- [ ] **Nuvem:** Firebase para recursos sociais e colaboração.
- [ ] Sincronização híbrida (merge inteligente).

### Recursos Multimídia
- [x] Editor de texto rico (estrutura básica).
- [ ] Integração com imagens, LaTeX e áudio (funcional).
- [ ] Armazenamento híbrido local + Firebase.

### Qualidade e Entregáveis
- [ ] Testes automatizados.
- [ ] Integração contínua (CI).
- [x] Código limpo e documentado.
- [ ] **Apresentação com até 10 slides**.
- [ ] **Demonstração ao vivo ou vídeo (3-5 min)**.
- [ ] Relatório crítico sobre uso de IA.
- [ ] Reflexão obrigatória sobre impacto educacional da IA.

---

## 📊 Fluxo de Desenvolvimento (Mermaid)

```mermaid
flowchart TD

A[Início do Projeto] --> B[Definir Arquitetura MVVM + Jetpack Compose]
B --> C[Implementar Sistema de Flashcards]
C --> C1[Frente e Verso]
C --> C2[Cloze]
C --> C3[Digite a Resposta (IA)]
C --> C4[Múltipla Escolha (IA)]

C --> D[Implementar GPS Inteligente]
D --> D1[Favoritar 7 locais]
D --> D2[Geofencing]
D --> D3[Analytics por localização]

D --> E[Repetição Espaçada com IA]
E --> E1[Algoritmo SM-2 + Adaptativo]
E --> E2[Sugestões personalizadas]

E --> F[Assistente de Estudos IA]
F --> F1[Geração de Conteúdo]
F --> F2[Validação de Respostas]
F --> F3[Dicas Progressivas]
F --> F4[Recomendações]
F --> F5[Chat Tutor]

F --> G[Arquitetura Híbrida]
G --> G1[SQLite Local]
G --> G2[Servidor Ktor API REST]
G --> G3[Firebase Sincronização]

G --> H[Multimídia e UI Avançada]
H --> H1[Imagens/Áudio/LaTeX]
H --> H2[Material Design 3]

H --> I[Testes e Qualidade]
I --> I1[Testes automatizados]
I --> I2[Integração contínua]
I --> I3[Validação de requisitos]

I --> J[Entrega Final]
J --> J1[Apresentação (slides + vídeo)]
J --> J2[Relatório crítico sobre IA]
J --> J3[Demonstração do App]

J --> K[Fim do Projeto]
```

---

📌 **Instrução para IA:**  
Sempre siga o checklist e o fluxo acima.  
- Antes de gerar código, confira se a etapa correspondente está marcada no checklist.  
- Ao concluir uma funcionalidade, marque-a como concluída (`[x]`).  
- Não avance para a próxima etapa sem finalizar a anterior.  
