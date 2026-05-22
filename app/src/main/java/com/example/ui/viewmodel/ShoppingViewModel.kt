package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.AislesAIServiceResponse
import com.example.data.api.Content
import com.example.data.api.GeminiRetrofitClient
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.entities.AisleCategory
import com.example.data.entities.Categorizer
import com.example.data.entities.ShoppingItem
import com.example.data.entities.ShoppingTrip
import com.example.data.repository.ShoppingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface Screen {
    object TripList : Screen
    data class TripDetail(val tripId: Long) : Screen
}

class ShoppingViewModel(private val repository: ShoppingRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.TripList)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedTrip = MutableStateFlow<ShoppingTrip?>(null)
    val selectedTrip: StateFlow<ShoppingTrip?> = _selectedTrip.asStateFlow()

    val allTrips: StateFlow<List<ShoppingTrip>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTripItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val activeTripItems: StateFlow<List<ShoppingItem>> = _activeTripItems.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _aiSuggestions = MutableStateFlow<List<com.example.data.api.AISuggestionItem>>(emptyList())
    val aiSuggestions: StateFlow<List<com.example.data.api.AISuggestionItem>> = _aiSuggestions.asStateFlow()

    private val _userApiKey = MutableStateFlow("")
    val userApiKey: StateFlow<String> = _userApiKey.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedTrip.collectLatest { trip ->
                if (trip != null) {
                    repository.getItemsForTrip(trip.id).collect { items ->
                        _activeTripItems.value = items
                    }
                } else {
                    _activeTripItems.value = emptyList()
                }
            }
        }
    }

    fun setApiKey(key: String) {
        _userApiKey.value = key
    }

    fun navigateToDetail(tripId: Long) {
        viewModelScope.launch {
            val trip = repository.getTrip(tripId)
            _selectedTrip.value = trip
            _currentScreen.value = Screen.TripDetail(tripId)
            _aiSuggestions.value = emptyList()
            _aiError.value = null
        }
    }

    fun navigateToDashboard() {
        _selectedTrip.value = null
        _currentScreen.value = Screen.TripList
        _aiSuggestions.value = emptyList()
        _aiError.value = null
    }

    fun createNewTrip(title: String, budget: Double, notes: String = "") {
        viewModelScope.launch {
            val cleanedTitle = title.ifBlank { "Lista de Compras" }
            val trip = ShoppingTrip(title = cleanedTitle, budgetLimit = budget, notes = notes)
            val newId = repository.insertTrip(trip)
            navigateToDetail(newId)
        }
    }

    fun updateTripNotes(tripId: Long, notes: String) {
        viewModelScope.launch {
            val trip = repository.getTrip(tripId)
            if (trip != null) {
                val updated = trip.copy(notes = notes)
                repository.updateTrip(updated)
                _selectedTrip.value = updated
            }
        }
    }

    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            repository.deleteTripWithItems(tripId)
            if (_selectedTrip.value?.id == tripId) {
                navigateToDashboard()
            }
        }
    }

    fun finalizeTrip(tripId: Long) {
        viewModelScope.launch {
            val trip = repository.getTrip(tripId)
            if (trip != null) {
                val updated = trip.copy(isCompleted = true)
                repository.updateTrip(updated)
                navigateToDashboard()
            }
        }
    }

    fun reopenTrip(tripId: Long) {
        viewModelScope.launch {
            val trip = repository.getTrip(tripId)
            if (trip != null) {
                val updated = trip.copy(isCompleted = false)
                repository.updateTrip(updated)
                navigateToDetail(tripId)
            }
        }
    }

    fun addItem(name: String, targetQuantity: Int, priority: String, estimatedPrice: Double) {
        val trip = _selectedTrip.value ?: return
        viewModelScope.launch {
            val autoCategory = Categorizer.autoCategorize(name).name
            val item = ShoppingItem(
                tripId = trip.id,
                name = name.trim(),
                category = autoCategory,
                priority = priority,
                targetQuantity = targetQuantity,
                estimatedPrice = estimatedPrice,
                actualQuantity = targetQuantity,
                actualPrice = estimatedPrice
            )
            repository.insertItem(item)
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun toggleItemCheck(item: ShoppingItem, isChecked: Boolean) {
        viewModelScope.launch {
            val updated = item.copy(
                isChecked = isChecked,
                actualPrice = if (item.actualPrice <= 0.0) item.estimatedPrice else item.actualPrice
            )
            repository.updateItem(updated)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItemById(itemId)
        }
    }

    fun acceptSuggestion(suggestionName: String, categoryName: String, priorityName: String) {
        val trip = _selectedTrip.value ?: return
        viewModelScope.launch {
            val item = ShoppingItem(
                tripId = trip.id,
                name = suggestionName,
                category = categoryName,
                priority = priorityName,
                targetQuantity = 1,
                actualQuantity = 1
            )
            repository.insertItem(item)
            _aiSuggestions.value = _aiSuggestions.value.filterNot { it.name.lowercase() == suggestionName.lowercase() }
        }
    }

    fun dismissSuggestion(suggestionName: String) {
        _aiSuggestions.value = _aiSuggestions.value.filterNot { it.name.lowercase() == suggestionName.lowercase() }
    }

    fun runAISmartAssistant() {
        val trip = _selectedTrip.value ?: return
        val currentItems = _activeTripItems.value
        
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            
            val appApiKey = try { com.example.BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
            val resolvedApiKey = _userApiKey.value.trim().ifEmpty { appApiKey }
            
            if (resolvedApiKey.isEmpty() || resolvedApiKey == "MY_GEMINI_API_KEY") {
                _aiError.value = "Chave de API do Gemini não configurada. Configure o segredo GEMINI_API_KEY no painel de Segredos ou insira sua chave nas configurações abaixo."
                _aiLoading.value = false
                return@launch
            }

            val itemsDescription = if (currentItems.isEmpty()) {
                "(Lista de compras vazia)"
            } else {
                currentItems.joinToString("\n") { item ->
                    "- ${item.name} (Quantidade: ${item.targetQuantity}, Categoria Atual: ${item.category})"
                }
            }

            val prompt = """
                Você é um organizador e assistente inteligente de compras de supermercado para o mercado brasileiro.
                O usuário possui a lista de compras atual para uma ida ao supermercado.

                Itens Atuais na Lista:
                $itemsDescription

                Sua tarefa consiste em duas partes:
                1. Identificar qualquer item cuja categoria atual seja "OUTROS" ou "Outros" e sugerir uma categoria correta entre as seguintes aceitas:
                   - "HORTIFRUTI" (Frutas, legumes, verduras, temperos frescos)
                   - "ACOUGUE" (Carnes bovinas, suínas, frango, peixes, frutos do mar, frios in-natura)
                   - "LATICINIOS" (Leite, queijo, manteiga, iogurte, creme de leite, requeijão)
                   - "PADARIA" (Pães, bolos, biscoitos, tortas, pão de queijo)
                   - "BEBIDAS" (Sucos, refrigerantes, águas, cervejas, vinhos, destilados)
                   - "MERCEARIA" (Arroz, feijão, óleo, sal, açúcar, café, macarrão, enlatados, molhos, farinhas)
                   - "LIMPEZA" (Sabão em pó, detergente, amaciante, desinfetante, esponjas)
                   - "HIGIENE" (Shampoo, sabonete, creme dental, papel higiênico, desodorante)
                   - "CONGELADOS" (Sorvetes, pizzas congeladas, nuggets, pratos prontos congelados)

                2. Sugerir de 3 a 5 itens úteis/essenciais adicionais que o usuário possa ter esquecido de colocar baseado nos itens que já estão na lista (ex: se comprou pão, sugira manteiga; se tem macarrão, sugira molho de tomate; se tem carnes para churrasco, sugira carvão ou sal grosso).
                Classifique a importância da sugestão como: "HIGH" (Alta - item crucial associado), "MEDIUM" (Média - básico/dia-a-dia), ou "LOW" (Baixa - complementar).

                Retorne OBRIGATORIAMENTE uma resposta no formato JSON estrito, sem textos adicionais antes ou depois. Use exatamente esta estrutura:
                {
                  "categorized": [
                    {
                      "name": "nome exato do item original",
                      "category": "NOME_DA_CATEGORIA_EM_MAIUSCULAS"
                    }
                  ],
                  "suggestions": [
                    {
                      "name": "Nome da sugestão em português",
                      "category": "NOME_DA_CATEGORIA_EM_MAIUSCULAS",
                      "importance": "HIGH"
                    }
                  ]
                }
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(responseMimeType = "application/json")
            )

            try {
                val response = withContext(Dispatchers.IO) {
                    GeminiRetrofitClient.service.generateContent(resolvedApiKey, request)
                }
                
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                val cleanedJson = extractJson(rawText)

                if (cleanedJson.isNotEmpty()) {
                    val adapter = GeminiRetrofitClient.responseMoshiParser.adapter(AislesAIServiceResponse::class.java)
                    val result = withContext(Dispatchers.Default) {
                        adapter.fromJson(cleanedJson)
                    }
                    
                    if (result != null) {
                        dbUpdateCategorized(currentItems, result)

                        val cleanSuggestions = result.suggestions.filter { sug ->
                            currentItems.none { item -> item.name.lowercase().trim() == sug.name.lowercase().trim() }
                        }
                        _aiSuggestions.value = cleanSuggestions
                    } else {
                        _aiError.value = "Não foi possível analisar as sugestões da Inteligência Artificial."
                    }
                } else {
                    _aiError.value = "Resposta da IA vazia ou com formato inválido."
                }
            } catch (e: Exception) {
                _aiError.value = "Falha de conexão com a IA: ${e.localizedMessage ?: "Erro desconhecido"}. Verifique sua internet ou sua chave de API."
            } finally {
                _aiLoading.value = false
            }
        }
    }

    private suspend fun dbUpdateCategorized(currentItems: List<ShoppingItem>, result: AislesAIServiceResponse) {
        val dbItemsToUpdate = currentItems.filter { it.category == "OUTROS" }
        dbItemsToUpdate.forEach { item ->
            val match = result.categorized.firstOrNull { it.name.lowercase().trim() == item.name.lowercase().trim() }
            if (match != null) {
                val parsedEnumName = try {
                    AisleCategory.valueOf(match.category.uppercase().trim()).name
                } catch (e: Exception) {
                    "OUTROS"
                }
                if (parsedEnumName != "OUTROS") {
                    repository.updateItem(item.copy(category = parsedEnumName))
                }
            }
        }
    }

    private fun extractJson(text: String): String {
        val trimmed = text.trim()
        if (trimmed.startsWith("```json") && trimmed.endsWith("```")) {
            return trimmed.substring(7, trimmed.length - 3).trim()
        }
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            return trimmed.substring(3, trimmed.length - 3).trim()
        }
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return trimmed.substring(start, end + 1)
        }
        return trimmed
    }
}

class ShoppingViewModelFactory(private val repository: ShoppingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
