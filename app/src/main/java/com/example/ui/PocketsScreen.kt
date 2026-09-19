package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Pocket
import com.example.data.model.PocketType
import com.example.ui.theme.BowlbyOneFontFamily
import com.example.util.CategoryIcons
import com.example.util.CurrencyUtils

@Composable
fun PocketsScreen(
    pockets: List<Pocket>,
    totalPocketsAmount: Long,
    totalSavingsAmount: Long,
    totalAllocatedAmount: Long,
    unallocatedBalance: Long,
    freeAvailableBalance: Long,
    onAddPocketClick: (PocketType) -> Unit,
    onDepositClick: (Pocket) -> Unit,
    onWithdrawClick: (Pocket) -> Unit,
    onQuickAssignClick: (Pocket, Long) -> Unit,
    onQuickWithdrawClick: (Pocket, Long) -> Unit,
    onDeleteClick: (Pocket) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0: Ahorros, 1: Apartados
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val savingsPockets = remember(pockets) { pockets.filter { it.isSavings } }
    val apartadosPockets = remember(pockets) { pockets.filter { it.isApartado } }

    val currentType = if (selectedTab == 0) PocketType.SAVINGS else PocketType.APARTADO

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddPocketClick(currentType) },
                shape = RoundedCornerShape(percent = 50),
                containerColor = Color.Black,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = if (selectedTab == 0) "Nuevo ahorro" else "Nuevo apartado",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier
                    .border(1.dp, Color.Black, RoundedCornerShape(percent = 50))
                    .testTag("add_pocket_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pestañas Superiores tipo píldora (TabRow neobrutalista con borde 1dp)
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("pockets_tab_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Pestaña 1: Ahorros
                    val isAhorros = selectedTab == 0
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = if (isAhorros) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                            .testTag("tab_pockets_ahorros")
                    ) {
                        Text(
                            text = "Ahorros (${savingsPockets.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isAhorros) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Pestaña 2: Apartados
                    val isApartados = selectedTab == 1
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = if (isApartados) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                            .testTag("tab_pockets_apartados")
                    ) {
                        Text(
                            text = "Apartados (${apartadosPockets.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isApartados) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Contenido según la pestaña seleccionada
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    // PESTAÑA AHORROS
                    item {
                        SavingsHeroCard(
                            totalSavingsAmount = totalSavingsAmount,
                            freeAvailableBalance = freeAvailableBalance,
                            savingsCount = savingsPockets.size
                        )
                    }

                    if (savingsPockets.isEmpty()) {
                        item {
                            EmptySavingsState(onAddClick = { onAddPocketClick(PocketType.SAVINGS) })
                        }
                    } else {
                        item {
                            Text(
                                text = "METAS DE AHORRO (${savingsPockets.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        items(savingsPockets, key = { it.id }) { pocket ->
                            SavingsPocketCard(
                                pocket = pocket,
                                onDepositClick = { onDepositClick(pocket) },
                                onWithdrawClick = { onWithdrawClick(pocket) },
                                onDeleteClick = { onDeleteClick(pocket) }
                            )
                        }
                    }
                } else {
                    // PESTAÑA APARTADOS
                    item {
                        ApartadosHeroCard(
                            freeAvailableBalance = freeAvailableBalance,
                            totalAllocatedAmount = totalAllocatedAmount,
                            unallocatedBalance = unallocatedBalance,
                            apartadosCount = apartadosPockets.size
                        )
                    }

                    if (apartadosPockets.isEmpty()) {
                        item {
                            EmptyApartadosState(onAddClick = { onAddPocketClick(PocketType.APARTADO) })
                        }
                    } else {
                        item {
                            Text(
                                text = "SOBRES DEL MES (${apartadosPockets.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        items(apartadosPockets, key = { it.id }) { pocket ->
                            ApartadoItemCard(
                                pocket = pocket,
                                unallocatedBalance = unallocatedBalance,
                                onDepositClick = { onDepositClick(pocket) },
                                onWithdrawClick = { onWithdrawClick(pocket) },
                                onQuickAssign = { amount -> onQuickAssignClick(pocket, amount) },
                                onQuickWithdraw = { amount -> onQuickWithdrawClick(pocket, amount) },
                                onDeleteClick = { onDeleteClick(pocket) }
                            )
                        }
                    }
                }

                // Espacio final para no tapar con el FAB
                item {
                    Spacer(Modifier.height(72.dp))
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPONENTES DE PESTAÑA AHORROS
// ----------------------------------------------------------------------------

@Composable
private fun SavingsHeroCard(
    totalSavingsAmount: Long,
    freeAvailableBalance: Long,
    savingsCount: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9CCFF))
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Metas de Ahorro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.secondary,
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        text = "$savingsCount metas",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "TOTAL AHORRADO (BLOQUEADO)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = CurrencyUtils.formatClp(totalSavingsAmount),
                fontFamily = BowlbyOneFontFamily,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("savings_total_amount_text")
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Saldo Libre Disponible",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Text(
                            text = CurrencyUtils.formatClp(freeAvailableBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (freeAvailableBalance >= 0L) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = Color.Black,
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(
                            text = "Protegido de gastos",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsPocketCard(
    pocket: Pocket,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val hasTarget = pocket.hasTarget
    val progress = pocket.progressFraction
    val isTargetReached = pocket.isTargetReached

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pocket_card_${pocket.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(pocket.colorHex))
                            .border(1.5.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.getIcon(pocket.iconName),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = pocket.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasTarget) {
                            Text(
                                text = "Meta: ${CurrencyUtils.formatClp(pocket.targetAmount!!)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar ahorro",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = CurrencyUtils.formatClp(pocket.currentAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (hasTarget) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTargetReached) "¡Meta cumplida! 🎉" else "${pocket.progressPercent}% completado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isTargetReached) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
                    color = Color(pocket.colorHex),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("deposit_pocket_btn_${pocket.id}")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cargar", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onWithdrawClick,
                    enabled = pocket.currentAmount > 0L,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFFF0F0F0),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("withdraw_pocket_btn_${pocket.id}")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Retirar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptySavingsState(onAddClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9CCFF))
                    .border(1.5.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Aún no tienes metas de ahorro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Crea metas a mediano o largo plazo (vacaciones, fondo de emergencia). El dinero que asignes se restará del saldo libre para protegerlo de tus compras diarias.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.testTag("empty_add_savings_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Crear meta de ahorro", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPONENTES DE PESTAÑA APARTADOS
// ----------------------------------------------------------------------------

@Composable
private fun ApartadosHeroCard(
    freeAvailableBalance: Long,
    totalAllocatedAmount: Long,
    unallocatedBalance: Long,
    apartadosCount: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apartados_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD731))
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Apartados del Mes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sobres virtuales del saldo libre",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.secondary,
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        text = "$apartadosCount sobres",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Cuadrícula / desglose financiero de Apartados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Saldo Libre
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "SALDO LIBRE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = CurrencyUtils.formatClp(freeAvailableBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Asignado en Apartados
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE9CCFF).copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ASIGNADO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = CurrencyUtils.formatClp(totalAllocatedAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Saldo que queda Sin Asignar (Destacado Neobrutalista)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (unallocatedBalance > 0L) Color(0xFF55DB9C).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.5.dp, Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QUEDA SIN ASIGNAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Text(
                            text = CurrencyUtils.formatClp(unallocatedBalance),
                            fontFamily = BowlbyOneFontFamily,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = Color.Black,
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(
                            text = "Disponible",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApartadoItemCard(
    pocket: Pocket,
    unallocatedBalance: Long,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onQuickAssign: (Long) -> Unit,
    onQuickWithdraw: (Long) -> Unit,
    onDeleteClick: () -> Unit
) {
    val hasTarget = pocket.hasTarget

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apartado_card_${pocket.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera: Icono, Nombre y botón de eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(pocket.colorHex))
                            .border(1.5.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.getIcon(pocket.iconName),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = pocket.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasTarget) {
                            Text(
                                text = "Presupuesto sugerido: ${CurrencyUtils.formatClp(pocket.targetAmount!!)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Sobre mensual",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar apartado",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Monto asignado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "MONTO ASIGNADO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = CurrencyUtils.formatClp(pocket.currentAmount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (hasTarget) {
                    val pct = if (pocket.targetAmount!! > 0L) {
                        ((pocket.currentAmount.toDouble() / pocket.targetAmount!!.toDouble()) * 100).toInt()
                    } else 0
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(
                            text = "$pct% del presupuesto",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Botones rápidos para sumar o restar asignación directa (+/-)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restar rápido -$10.000
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = pocket.currentAmount >= 10_000L) {
                            onQuickWithdraw(10_000L)
                        }
                ) {
                    Text(
                        text = "-$10.000",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pocket.currentAmount >= 10_000L) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                // Sumar rápido +$5.000
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = unallocatedBalance >= 5_000L) {
                            onQuickAssign(5_000L)
                        }
                ) {
                    Text(
                        text = "+$5.000",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (unallocatedBalance >= 5_000L) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                // Sumar rápido +$10.000
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = unallocatedBalance >= 10_000L) {
                            onQuickAssign(10_000L)
                        }
                ) {
                    Text(
                        text = "+$10.000",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (unallocatedBalance >= 10_000L) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botones de acción principales: Asignar y Liberar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón Asignar (Negro pill, borde 1dp)
                Button(
                    onClick = onDepositClick,
                    enabled = unallocatedBalance > 0L,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF757575),
                        disabledContentColor = Color.LightGray
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("assign_apartado_btn_${pocket.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Asignar", fontWeight = FontWeight.Bold)
                }

                // Botón Liberar (Blanco pill, borde 1dp)
                OutlinedButton(
                    onClick = onWithdrawClick,
                    enabled = pocket.currentAmount > 0L,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFFF0F0F0),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("release_apartado_btn_${pocket.id}")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Liberar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyApartadosState(onAddClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD731))
                    .border(1.5.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Aún no tienes apartados del mes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Crea apartados virtuales para organizar y presupuestar áreas específicas de tu mes (supermercado, salidas, ocio). No restan tu saldo libre general, sino que subdividen lo que tienes disponible.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.testTag("empty_add_apartados_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Crear primer apartado", fontWeight = FontWeight.Bold)
            }
        }
    }
}
