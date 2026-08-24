package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LanguageMode
import com.example.model.MenuCategory
import com.example.ui.theme.DhabaGold
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaRed
import com.example.viewmodel.SortOption

@Composable
fun CategoryBar(
    selectedCategory: MenuCategory,
    categories: List<MenuCategory>,
    searchQuery: String,
    vegOnly: Boolean,
    sortOption: SortOption,
    languageMode: LanguageMode,
    onCategorySelect: (MenuCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onVegOnlyToggle: () -> Unit,
    onSortOptionChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 12.dp, bottom = 6.dp)
    ) {
        // Search bar and Veg Switch Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("menu_search_input"),
                placeholder = {
                    Text(
                        text = when (languageMode) {
                            LanguageMode.HINDI -> "खाना खोजें (जैसे पनीर, रोटी...)"
                            LanguageMode.ENGLISH -> "Search dishes (e.g. Paneer, Roti)..."
                            LanguageMode.BOTH -> "खाना / Dish खोजें..."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DhabaRed,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DhabaRed,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            // Sort Menu Button
            Box {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("sort_menu_button")
                        .clickable { showSortMenu = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = DhabaRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (languageMode == LanguageMode.ENGLISH) option.titleEn else option.titleHi,
                                    fontSize = 13.sp,
                                    fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sortOption == option) DhabaRed else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSortOptionChange(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Quick Ingredient & Flavor Chips Row
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickIngredients = listOf(
                "पनीर" to "🧀 Paneer",
                "लहसुन" to "🧄 Garlic",
                "मक्खन" to "🧈 Butter",
                "चावल" to "🍚 Rice",
                "केसर" to "✨ Kesar",
                "कॉर्न" to "🌽 Corn",
                "मैंगो" to "🥭 Mango",
                "कॉफ़ी" to "☕ Coffee"
            )

            quickIngredients.forEach { (keywordHi, label) ->
                val isCurrentSearch = searchQuery.equals(keywordHi, ignoreCase = true) ||
                        searchQuery.equals(label.substringAfter(" "), ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCurrentSearch) DhabaGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = if (isCurrentSearch) androidx.compose.foundation.BorderStroke(1.dp, DhabaGold) else null,
                    modifier = Modifier
                        .testTag("ingredient_chip_${keywordHi}")
                        .clickable {
                            if (isCurrentSearch) {
                                onSearchChange("")
                            } else {
                                onSearchChange(keywordHi)
                            }
                        }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrentSearch) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Filters (Veg Switch & Category Pills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Veg Only Chip Toggle
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (vegOnly) DhabaGreen.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (vegOnly) DhabaGreen else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .testTag("veg_only_filter_chip")
                    .clickable { onVegOnlyToggle() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    // Veg Square Dot Icon
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .border(1.5.dp, DhabaGreen, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(DhabaGreen, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.ENGLISH) "Pure Veg" else "शुद्ध शाकाहारी",
                        fontSize = 12.sp,
                        fontWeight = if (vegOnly) FontWeight.Bold else FontWeight.Medium,
                        color = if (vegOnly) DhabaGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Category Tabs
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) DhabaRed else MaterialTheme.colorScheme.surface,
                    label = "category_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    label = "category_text"
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bgColor,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    shadowElevation = if (isSelected) 3.dp else 0.dp,
                    modifier = Modifier
                        .testTag("category_tab_${category.name.lowercase()}")
                        .clickable { onCategorySelect(category) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = category.emoji,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (languageMode) {
                                LanguageMode.HINDI -> category.titleHi
                                LanguageMode.ENGLISH -> category.titleEn
                                LanguageMode.BOTH -> "${category.titleHi} (${category.titleEn})"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
