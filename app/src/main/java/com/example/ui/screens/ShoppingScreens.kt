package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.AISuggestionItem
import com.example.data.entities.AisleCategory
import com.example.data.entities.ShoppingItem
import com.example.data.entities.ShoppingTrip
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.ShoppingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Simple extensions to format local values
fun Double.toCurrency(): String = String.format(Locale("pt", "BR"), "R$ %.2f", this)

fun Long.toDateString(): String = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(this))

@Composable
fun ShoppingAssistantApp(viewModel: ShoppingViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.TripList -> {
                TripListScreen(
                    viewModel = viewModel,
                    onTripClick = { viewModel.navigateToDetail(it) }
                )
            }
            is Screen.TripDetail -> {
                TripDetailScreen(
                    viewModel = viewModel,
                    tripId = screen.tripId,
                    onBackClick = { viewModel.navigateToDashboard() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    viewModel: ShoppingViewModel,
    onTripClick: (Long) -> Unit
) {
    val trips by viewModel.allTrips.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Let's compute some dashboard stats
    val totalTrips = trips.size
    val totalSpent = trips.sumOf { trip ->
         if (trip.isCompleted) trip.budgetLimit else 0.0 // representation of historical spent if completed, actually let's retrieve total spent in items or budget
         // but let's just make a simple total representation of budgets
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛒 Assistente de Compras", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    var showKeyConfig by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showKeyConfig = true },
                        modifier = Modifier.testTag("config_key_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Configurar Chave")
                    }
                    if (showKeyConfig) {
                        ApiKeyConfigDialog(viewModel = viewModel, onDismiss = { showKeyConfig = false })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("create_trip_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Lista")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats Dashboard Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Economia & Controle de Gastos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Planeje suas listas, organize por corredores de supermercado e monitore seu saldo em tempo real para não estourar seu orçamento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Listas", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "$totalTrips",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Orçamento Médio", style = MaterialTheme.typography.bodySmall)
                            val avgBudget = if (trips.isNotEmpty()) trips.map { it.budgetLimit }.average() else 0.0
                            Text(
                                avgBudget.toCurrency(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Text(
                text = "Minhas Viagens & Listas de Compras",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (trips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📝 Nenhuma lista criada",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque no botão + abaixo para começar o planejamento da sua próxima ida ao supermercado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trips) { trip ->
                        TripItemCard(
                            trip = trip,
                            onItemClick = { onTripClick(trip.id) },
                            onDeleteClick = { viewModel.deleteTrip(trip.id) }
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateTripDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, budget, notes ->
                    viewModel.createNewTrip(title, budget, notes)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun TripItemCard(
    trip: ShoppingTrip,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("trip_card_${trip.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (trip.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (!trip.isCompleted) CardDefaults.outlinedCardBorder() else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (trip.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Finalizado",
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = trip.date.toDateString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Orçamento: ${trip.budgetLimit.toCurrency()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (trip.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .testTag("delete_trip_${trip.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir Lista",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CreateTripDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var budgetStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Criar Lista de Supermercado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome da Lista (ex: Feira Semanal)") },
                    placeholder = { Text("Carrinho de Compras") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Seu Limite de Orçamento (R$)") },
                    placeholder = { Text("Ex: 150.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_budget_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)") },
                    placeholder = { Text("Ex: Comprar itens da marca X") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_notes_input")
                )

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val budgetValue = budgetStr.toDoubleOrNull()
                            if (budgetValue == null || budgetValue <= 0.0) {
                                error = "Por favor, insira um orçamento válido maior que zero."
                            } else {
                                onCreate(title.trim(), budgetValue, notes.trim())
                            }
                        },
                        modifier = Modifier.testTag("trip_confirm_button")
                    ) {
                        Text("Criar & Abrir")
                    }
                }
            }
        }
    }
}

@Composable
fun ApiKeyConfigDialog(
    viewModel: ShoppingViewModel,
    onDismiss: () -> Unit
) {
    val currentKey by viewModel.userApiKey.collectAsState()
    var tempKey by remember { mutableStateOf(currentKey) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Configurar Gemini AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Adicione sua Chave de API do Gemini para categorizar itens automaticamente por corredores e sugerir itens importantes de forma inteligente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = tempKey,
                    onValueChange = { tempKey = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("Disponível em ai.google.dev") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.setApiKey(tempKey)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("api_key_save_button")
                    ) {
                        Text("Salvar Chave")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: ShoppingViewModel,
    tripId: Long,
    onBackClick: () -> Unit
) {
    val trip by viewModel.selectedTrip.collectAsState()
    val rawItems by viewModel.activeTripItems.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }

    if (trip == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregando lista...")
        }
        return
    }

    val currentTrip = trip!!

    // Live calculations of totals
    val currentCartTotal = rawItems.filter { it.isChecked }.sumOf { it.actualPrice * it.actualQuantity }
    val plannedTotal = rawItems.sumOf { it.estimatedPrice * it.targetQuantity }
    val progress = if (currentTrip.budgetLimit > 0.0) currentCartTotal / currentTrip.budgetLimit else 0.0
    val budgetExceeded = currentCartTotal > currentTrip.budgetLimit
    val excessAmount = currentCartTotal - currentTrip.budgetLimit

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            currentTrip.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Planejado: ${plannedTotal.toCurrency()} | Orçamento: ${currentTrip.budgetLimit.toCurrency()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (currentTrip.isCompleted) {
                        IconButton(
                            onClick = { viewModel.reopenTrip(currentTrip.id) },
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reabrir Lista")
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.finalizeTrip(currentTrip.id) },
                            modifier = Modifier.testTag("finalize_trip_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Finalizar Compras")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Persistent Budget Limit Banner (Calculadora de Orçamento)
            BudgetCalculatorBanner(
                currentCartTotal = currentCartTotal,
                budgetLimit = currentTrip.budgetLimit,
                progress = progress,
                budgetExceeded = budgetExceeded,
                excessAmount = excessAmount
            )

            // Tabs to separate planners and active checkout
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    text = { Text("1. Planejar Lista") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    text = { Text("2. No Supermercado") }
                )
            }

            AnimatedContent(
                targetState = activeTab,
                modifier = Modifier.weight(1f),
                label = "TabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> PlannerTab(
                        viewModel = viewModel,
                        currentTrip = currentTrip,
                        items = rawItems,
                        aiLoading = aiLoading,
                        aiError = aiError,
                        aiSuggestions = aiSuggestions
                    )
                    1 -> CheckoutTab(
                        viewModel = viewModel,
                        currentTrip = currentTrip,
                        items = rawItems
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetCalculatorBanner(
    currentCartTotal: Double,
    budgetLimit: Double,
    progress: Double,
    budgetExceeded: Boolean,
    excessAmount: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                budgetExceeded -> Color(0xFFFCE8E6) // Light Red
                progress >= 0.8 -> Color(0xFFFEF7E0) // Light Gold/Yellow
                else -> Color(0xFFE6F4EA) // Light Green
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "CARRINHO ATUAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            budgetExceeded -> Color(0xFFC5221F)
                            progress >= 0.8 -> Color(0xFFB06000)
                            else -> Color(0xFF137333)
                        }
                    )
                    Text(
                        currentCartTotal.toCurrency(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            budgetExceeded -> Color(0xFFC5221F)
                            progress >= 0.8 -> Color(0xFFB06000)
                            else -> Color(0xFF137333)
                        }
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "LIMITE ORÇADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        budgetLimit.toCurrency(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Beautiful status indicator bar
            val colorIndicator = when {
                budgetExceeded -> Color(0xFFC5221F)
                progress >= 0.8 -> Color(0xFFF9AB00)
                else -> Color(0xFF1E8E3E)
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = colorIndicator,
                trackColor = colorIndicator.copy(alpha = 0.2f)
            )

            if (budgetExceeded) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "⚠️ Orçamento estourado em ${excessAmount.toCurrency()}!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC5221F),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                val percentage = (progress * 100).toInt()
                Text(
                    "Você utilizou $percentage% do seu orçamento limite.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PlannerTab(
    viewModel: ShoppingViewModel,
    currentTrip: ShoppingTrip,
    items: List<ShoppingItem>,
    aiLoading: Boolean,
    aiError: String?,
    aiSuggestions: List<AISuggestionItem>
) {
    var itemName by remember { mutableStateOf("") }
    var itemPriceStr by remember { mutableStateOf("") }
    var itemQty by remember { mutableIntStateOf(1) }
    var priority by remember { mutableStateOf("MEDIUM") } // LOW, MEDIUM, HIGH
    var showSuggestionsSection by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        if (currentTrip.isCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Esta lista está fechada/finalizada. Reabra para modificar itens.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Planner Add Item Widget
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Adicionar Item Planejado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Nome do item") },
                            placeholder = { Text("Ex: Arroz Tio João") },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("item_name_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = itemPriceStr,
                            onValueChange = { itemPriceStr = it },
                            label = { Text("Preço Est. (R$)") },
                            placeholder = { Text("Ex: 5.50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("item_price_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Priority Selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Importância:", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.width(6.dp))
                            listOf("LOW" to "Baixa", "MEDIUM" to "Média", "HIGH" to "Alta").forEach { (level, text) ->
                                val isSelected = priority == level
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .background(
                                            color = if (isSelected) {
                                                when (level) {
                                                    "HIGH" -> Color(0xFFFCE8E6)
                                                    "MEDIUM" -> Color(0xFFFEF7E0)
                                                    else -> Color(0xFFF1F3F4)
                                                }
                                            } else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { priority = level }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) {
                                            when (level) {
                                                "HIGH" -> Color(0xFFC5221F)
                                                "MEDIUM" -> Color(0xFFB06000)
                                                else -> Color(0xFF5F6368)
                                            }
                                        } else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Quantity selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (itemQty > 1) itemQty-- }
                            ) {
                                Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Text("$itemQty", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { itemQty++ }
                            ) {
                                Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (itemName.isNotBlank()) {
                                val estPrice = itemPriceStr.toDoubleOrNull() ?: 0.0
                                viewModel.addItem(itemName, itemQty, priority, estPrice)
                                itemName = ""
                                itemPriceStr = ""
                                itemQty = 1
                                priority = "MEDIUM"
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_add_button")
                    ) {
                        Text("Adicionar Item à Lista")
                    }
                }
            }
        }

        // AI Assistant Call-to-Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🤖 Assistente de Compra Inteligente",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Deixe a Inteligência Artificial classificar seus itens em corredores do mercado e sugerir marcas ou produtos importantes que você esqueceu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.runAISmartAssistant() },
                        enabled = !aiLoading && !currentTrip.isCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("run_ai_button")
                    ) {
                        if (aiLoading) {
                            Text("IA..." )
                        } else {
                            Text("Organizar IA")
                        }
                    }
                }

                if (aiError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        aiError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Handle AI Suggestions UI
        if (aiSuggestions.isNotEmpty() && showSuggestionsSection) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✨ Sugestões Inteligentes (Você esqueceu?)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        IconButton(
                            onClick = { showSuggestionsSection = false }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        items(aiSuggestions) { sug ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        sug.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    val catEmoji = try {
                                        AisleCategory.valueOf(sug.category.uppercase()).icon
                                    } catch (e: Exception) {
                                        "📦"
                                    }
                                    val catName = try {
                                        AisleCategory.valueOf(sug.category.uppercase()).displayName
                                    } catch (e: Exception) {
                                        "Gerais/Outros"
                                    }
                                    Text(
                                        "$catEmoji $catName",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Row {
                                    Button(
                                        onClick = {
                                            viewModel.acceptSuggestion(
                                                sug.name,
                                                sug.category.uppercase(),
                                                sug.importance.uppercase()
                                            )
                                        },
                                        modifier = Modifier.height(28.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.dismissSuggestion(sug.name) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Itens no Planejamento (${items.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Planner List Items
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sua lista está vazia. Adicione itens acima!",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    PlannerItemCard(
                        item = item,
                        viewModel = viewModel,
                        currentTrip = currentTrip
                    )
                }
            }
        }
    }
}

@Composable
fun PlannerItemCard(
    item: ShoppingItem,
    viewModel: ShoppingViewModel,
    currentTrip: ShoppingTrip
) {
    val categoryEnum = try {
        AisleCategory.valueOf(item.category)
    } catch (e: Exception) {
        AisleCategory.OUTROS
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("planner_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(categoryEnum.colorHex)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Small local tags
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${categoryEnum.icon} ${categoryEnum.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Qtd Planejada: ${item.targetQuantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Est. Unitário: ${item.estimatedPrice.toCurrency()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    val importanceLabel = when (item.priority) {
                        "HIGH" -> "🚨 Alta"
                        "MEDIUM" -> "⚡ Média"
                        else -> "🌱 Baixa"
                    }
                    Text(
                        text = importanceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = when (item.priority) {
                            "HIGH" -> Color(0xFFC5221F)
                            "MEDIUM" -> Color(0xFFB06000)
                            else -> Color(0xFF5F6368)
                        }
                    )
                }
            }

            if (!currentTrip.isCompleted) {
                IconButton(
                    onClick = { viewModel.deleteItem(item.id) },
                    modifier = Modifier.testTag("delete_item_${item.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutTab(
    viewModel: ShoppingViewModel,
    currentTrip: ShoppingTrip,
    items: List<ShoppingItem>
) {
    // For supermarket navigation, we will group items specifically by Aisle/Category!
    // This solves the problem of "getting lost in the aisles" completely.
    val itemsByAisle = remember(items) {
        items.groupBy {
            try {
                AisleCategory.valueOf(it.category)
            } catch (e: Exception) {
                AisleCategory.OUTROS
            }
        }.toSortedMap(compareBy { it.ordinal })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sua lista de planejamento está vazia. Adicione itens antes de ir às compras!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // For each aisle, show an Aisle Group Card with its checklist inside
                itemsByAisle.forEach { (aisle, aisleItems) ->
                    item {
                        AisleSectionCard(
                            aisle = aisle,
                            aisleItems = aisleItems,
                            viewModel = viewModel,
                            currentTrip = currentTrip
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AisleSectionCard(
    aisle: AisleCategory,
    aisleItems: List<ShoppingItem>,
    viewModel: ShoppingViewModel,
    currentTrip: ShoppingTrip
) {
    val completedCount = aisleItems.count { it.isChecked }
    val totalCount = aisleItems.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(aisle.colorHex).copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Aisle Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(aisle.icon, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        aisle.displayName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "$completedCount / $totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items in this specific aisle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                aisleItems.forEach { item ->
                    CheckoutItemRow(
                        item = item,
                        viewModel = viewModel,
                        currentTrip = currentTrip
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutItemRow(
    item: ShoppingItem,
    viewModel: ShoppingViewModel,
    currentTrip: ShoppingTrip
) {
    var showEditPriceDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("checkout_item_row_${item.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { checked ->
                    if (!currentTrip.isCompleted) {
                        viewModel.toggleItemCheck(item, checked)
                    }
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF1E8E3E)
                ),
                enabled = !currentTrip.isCompleted,
                modifier = Modifier
                    .testTag("item_check_${item.id}")
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.clickable {
                if (!currentTrip.isCompleted) {
                    showEditPriceDialog = true
                }
            }) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isChecked) Color.Gray else Color.Black,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Unitário: ${item.actualPrice.toCurrency()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isChecked) Color.Gray else Color.DarkGray
                    )
                    Text(
                        text = "Qtd: ${item.actualQuantity}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isChecked) Color.Gray else Color.DarkGray
                    )
                    Text(
                        text = "Subtotal: ${(item.actualPrice * item.actualQuantity).toCurrency()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isChecked) Color(0xFF1E8E3E) else Color.DarkGray
                    )
                }
            }
        }

        // Live quantity adjustment inside cart
        if (!currentTrip.isCompleted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        if (item.actualQuantity > 1) {
                            viewModel.updateItem(item.copy(actualQuantity = item.actualQuantity - 1))
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("-", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Text(
                    "${item.actualQuantity}",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = {
                        viewModel.updateItem(item.copy(actualQuantity = item.actualQuantity + 1))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("+", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { showEditPriceDialog = true },
                    modifier = Modifier.size(32.dp).testTag("edit_price_btn_${item.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar Preço",
                        modifier = Modifier.size(16.dp),
                        tint = Color.DarkGray
                    )
                }
            }
        }
    }

    if (showEditPriceDialog) {
        EditActualPriceDialog(
            item = item,
            onDismiss = { showEditPriceDialog = false },
            onSave = { updatedPrice, updatedQty ->
                viewModel.updateItem(item.copy(actualPrice = updatedPrice, actualQuantity = updatedQty))
                showEditPriceDialog = false
            }
        )
    }
}

@Composable
fun EditActualPriceDialog(
    item: ShoppingItem,
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit
) {
    var priceStr by remember { mutableStateOf(item.actualPrice.toString()) }
    var qty by remember { mutableIntStateOf(item.actualQuantity) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Ajustar Valor no Carrinho",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Insira o preço real exibido na prateleira do supermercado para atualizar seu cálculo de orçamento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Preço Real no Supermercado (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("actual_price_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quantidade comprada:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (qty > 1) qty-- }) { Row { Text("-", fontWeight = FontWeight.Bold) } }
                        Text("$qty", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { qty++ }) { Row { Text("+", fontWeight = FontWeight.Bold) } }
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Voltar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val prcValue = priceStr.toDoubleOrNull()
                            if (prcValue == null || prcValue < 0.0) {
                                error = "Insira um preço válido maior ou igual a zero."
                            } else {
                                onSave(prcValue, qty)
                            }
                        },
                        modifier = Modifier.testTag("save_actual_price_btn")
                    ) {
                        Text("Atualizar")
                    }
                }
            }
        }
    }
}
