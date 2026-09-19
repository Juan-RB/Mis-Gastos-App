package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.RecurringExpenseStatus
import com.example.data.model.RecurringExpenseWithCategory

@Composable
fun ExpensesUnifiedScreen(
    uiState: ExpensesUiState,
    recurringStatuses: List<RecurringExpenseStatus>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    onToggleMonthFilter: (Boolean) -> Unit,
    onSelectCategoryFilter: (Long?) -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onEditExpenseClick: (ExpenseWithCategory) -> Unit,
    onDeleteExpenseClick: (ExpenseWithCategory) -> Unit,
    onAddRecurringClick: () -> Unit,
    onEditRecurringClick: (RecurringExpenseWithCategory) -> Unit,
    onDeleteRecurringClick: (RecurringExpenseWithCategory) -> Unit,
    onRegisterRecurringClick: (RecurringExpenseWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) } // 0: Variables, 1: Fijos

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // TabRow tipo píldora con borde de 1dp
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("expenses_unified_tab_row")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Variables
                val isVariables = selectedSubTab == 0
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = if (isVariables) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedSubTab = 0 }
                        .testTag("subtab_variables")
                ) {
                    Text(
                        text = "Variables",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isVariables) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Tab 2: Fijos
                val isFijos = selectedSubTab == 1
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = if (isFijos) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedSubTab = 1 }
                        .testTag("subtab_fijos")
                ) {
                    Text(
                        text = "Fijos",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isFijos) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedSubTab == 0) {
                ExpensesListScreen(
                    uiState = uiState,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onResetMonth = onResetMonth,
                    onToggleMonthFilter = onToggleMonthFilter,
                    onSelectCategoryFilter = onSelectCategoryFilter,
                    onAddExpenseClick = onAddExpenseClick,
                    onAddIncomeClick = onAddIncomeClick,
                    onEditExpenseClick = onEditExpenseClick,
                    onDeleteExpenseClick = onDeleteExpenseClick
                )
            } else {
                RecurringExpensesScreen(
                    recurringStatuses = recurringStatuses,
                    selectedYear = uiState.selectedYear,
                    selectedMonth = uiState.selectedMonth,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onResetMonth = onResetMonth,
                    onAddRecurringClick = onAddRecurringClick,
                    onAddIncomeClick = onAddIncomeClick,
                    onEditRecurringClick = onEditRecurringClick,
                    onDeleteRecurringClick = onDeleteRecurringClick,
                    onRegisterRecurringClick = onRegisterRecurringClick
                )
            }
        }
    }
}
