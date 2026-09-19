package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.Pocket
import com.example.data.model.PocketType
import com.example.util.CategoryIcons
import com.example.util.CurrencyUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPocketDialog(
    initialType: PocketType = PocketType.SAVINGS,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Long?, colorHex: Long, iconName: String, type: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(CategoryIcons.AVAILABLE_COLORS[0]) }
    var selectedIcon by remember { mutableStateOf(if (initialType == PocketType.APARTADO) "shopping_bag" else "savings") }
    var nameError by remember { mutableStateOf(false) }

    val isApartado = selectedType == PocketType.APARTADO

    val pocketIcons = remember {
        listOf(
            "savings",
            "shopping_bag",
            "restaurant",
            "directions_car",
            "home",
            "flight",
            "school",
            "fitness_center",
            "sports_esports"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(selectedColor))
                        .border(1.5.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIcons.getIcon(selectedIcon),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (isApartado) "Nuevo Apartado" else "Nuevo Ahorro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selector de Tipo (Ahorro vs Apartado)
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isSav = selectedType == PocketType.SAVINGS
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = if (isSav) Color.Black else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedType = PocketType.SAVINGS
                                    if (selectedIcon == "shopping_bag") selectedIcon = "savings"
                                }
                        ) {
                            Text(
                                text = "Ahorro",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSav) Color.White else Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        val isAp = selectedType == PocketType.APARTADO
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = if (isAp) Color.Black else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedType = PocketType.APARTADO
                                    if (selectedIcon == "savings") selectedIcon = "shopping_bag"
                                }
                        ) {
                            Text(
                                text = "Apartado (Mes)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAp) Color.White else Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isApartado) {
                        "Subdivide tu dinero disponible del mes para gastos específicos (ocio, súper, etc.). No bloquea tu saldo libre general."
                    } else {
                        "Meta a mediano/largo plazo. El monto asignado se descuenta de tu Saldo Libre para protegerlo."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(if (isApartado) "Nombre del apartado" else "Nombre del ahorro") },
                    placeholder = {
                        Text(if (isApartado) "Ej. Supermercado, Ocio, Salidas" else "Ej. Vacaciones, Fondo de emergencia")
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Ingresa un nombre válido") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pocket_name_input")
                )

                // Meta / Presupuesto opcional
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        val digits = CurrencyUtils.parseClpDigits(input)
                        targetText = if (digits > 0L) CurrencyUtils.formatClp(digits) else ""
                    },
                    label = { Text(if (isApartado) "Presupuesto sugerido (opcional)" else "Meta de ahorro (CLP, opcional)") },
                    placeholder = { Text(if (isApartado) "Ej. $80.000" else "Ej. $500.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pocket_target_input")
                )

                // Color de acento
                Text(
                    text = "Color de acento",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIcons.AVAILABLE_COLORS.forEach { colorHex ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = Color.Black,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Color seleccionado",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Ícono tipo sticker
                Text(
                    text = "Ícono representativo",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pocketIcons.forEach { iconKey ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(selectedColor) else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = Color.Black,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIcons.getIcon(iconKey),
                                contentDescription = iconKey,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val target = CurrencyUtils.parseClpDigits(targetText).takeIf { it > 0L }
                        onConfirm(name.trim(), target, selectedColor, selectedIcon, selectedType.name)
                    }
                },
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.testTag("confirm_create_pocket_btn")
            ) {
                Text(if (isApartado) "Crear apartado" else "Crear ahorro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(20.dp))
    )
}

@Composable
fun DepositPocketDialog(
    pocket: Pocket,
    freeAvailableBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val amountLong = CurrencyUtils.parseClpDigits(amountText)
    val exceedsFree = amountLong > freeAvailableBalance
    val isApartado = pocket.isApartado

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(pocket.colorHex))
                        .border(1.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIcons.getIcon(pocket.iconName),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isApartado) "Asignar Dinero" else "Cargar Dinero",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pocket.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info de Saldo Libre
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isApartado) "Saldo Libre Sin Asignar:" else "Saldo Libre Disponible:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatClp(freeAvailableBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (freeAvailableBalance > 0L) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "${if (isApartado) "Asignado" else "Guardado"}: ${CurrencyUtils.formatClp(pocket.currentAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val digits = CurrencyUtils.parseClpDigits(input)
                        amountText = if (digits > 0L) CurrencyUtils.formatClp(digits) else ""
                        errorText = null
                    },
                    label = { Text(if (isApartado) "Monto a asignar (CLP)" else "Monto a cargar (CLP)") },
                    placeholder = { Text("$10.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorText != null || exceedsFree,
                    supportingText = {
                        if (exceedsFree) {
                            Text(
                                text = if (isApartado) {
                                    "Supera tu saldo sin asignar (${CurrencyUtils.formatClp(freeAvailableBalance)})"
                                } else {
                                    "Supera tu saldo libre disponible (${CurrencyUtils.formatClp(freeAvailableBalance)})"
                                },
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (errorText != null) {
                            Text(text = errorText!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                if (isApartado) {
                                    "Se asignará de tu saldo disponible para presupuestar este apartado."
                                } else {
                                    "Se descontará de tu saldo libre para guardarlo en este ahorro."
                                }
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountLong <= 0L) {
                        errorText = "Ingresa un monto mayor a 0"
                    } else if (amountLong > freeAvailableBalance) {
                        errorText = "Saldo insuficiente"
                    } else {
                        onConfirm(amountLong)
                    }
                },
                enabled = amountLong > 0L && !exceedsFree,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.testTag("confirm_deposit_btn")
            ) {
                Text(if (isApartado) "Asignar dinero" else "Cargar dinero", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(20.dp))
    )
}

@Composable
fun WithdrawPocketDialog(
    pocket: Pocket,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val amountLong = CurrencyUtils.parseClpDigits(amountText)
    val exceedsPocket = amountLong > pocket.currentAmount
    val isApartado = pocket.isApartado

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(pocket.colorHex))
                        .border(1.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIcons.getIcon(pocket.iconName),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isApartado) "Liberar Dinero" else "Retirar Dinero",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pocket.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info de Dinero en el Bolsillo
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isApartado) "Monto asignado en este apartado:" else "Monto disponible en este bolsillo:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = CurrencyUtils.formatClp(pocket.currentAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val digits = CurrencyUtils.parseClpDigits(input)
                        amountText = if (digits > 0L) CurrencyUtils.formatClp(digits) else ""
                        errorText = null
                    },
                    label = { Text(if (isApartado) "Monto a liberar (CLP)" else "Monto a retirar (CLP)") },
                    placeholder = { Text("$10.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorText != null || exceedsPocket,
                    supportingText = {
                        if (exceedsPocket) {
                            Text(
                                text = "Supera lo guardado en el bolsillo (${CurrencyUtils.formatClp(pocket.currentAmount)})",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (errorText != null) {
                            Text(text = errorText!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                if (isApartado) {
                                    "El dinero volverá a quedar como 'Sin asignar' dentro de tu saldo libre."
                                } else {
                                    "El dinero regresará de inmediato a tu saldo libre disponible."
                                }
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountLong <= 0L) {
                        errorText = "Ingresa un monto mayor a 0"
                    } else if (amountLong > pocket.currentAmount) {
                        errorText = "No puedes retirar más de lo guardado"
                    } else {
                        onConfirm(amountLong)
                    }
                },
                enabled = amountLong > 0L && !exceedsPocket,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.testTag("confirm_withdraw_btn")
            ) {
                Text(if (isApartado) "Liberar dinero" else "Retirar dinero", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(20.dp))
    )
}

@Composable
fun DeletePocketConfirmDialog(
    pocket: Pocket,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar bolsillo \"${pocket.name}\"?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pocket.currentAmount > 0L) {
                    Text(
                        text = "Este bolsillo tiene ${CurrencyUtils.formatClp(pocket.currentAmount)} guardados. Al eliminarlo, este dinero se reintegrará a tu saldo libre disponible.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Esta acción eliminará el bolsillo y su historial de movimientos. Esta operación no se puede deshacer.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.testTag("confirm_delete_pocket_btn")
            ) {
                Text("Eliminar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(20.dp))
    )
}
