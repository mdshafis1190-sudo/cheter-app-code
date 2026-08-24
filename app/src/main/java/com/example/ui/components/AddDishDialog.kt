package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MenuCategory
import com.example.model.MenuItem
import com.example.model.SpicyLevel
import com.example.ui.theme.DhabaGreen
import com.example.ui.theme.DhabaRed

@Composable
fun AddDishDialog(
    onDismiss: () -> Unit,
    initialItem: MenuItem? = null,
    onSaveDish: (
        id: Int?,
        nameHi: String,
        nameEn: String,
        category: MenuCategory,
        price: Int,
        descHi: String,
        descEn: String,
        isVeg: Boolean,
        spicyLevel: SpicyLevel,
        emoji: String,
        prepTimeMin: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = initialItem != null
    var nameHi by remember { mutableStateOf(initialItem?.nameHi ?: "") }
    var nameEn by remember { mutableStateOf(initialItem?.nameEn ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: MenuCategory.MAIN) }
    var priceText by remember { mutableStateOf(initialItem?.price?.toString() ?: "") }
    var prepTimeText by remember { mutableStateOf((initialItem?.prepTimeMin ?: 15).toString()) }
    var descHi by remember { mutableStateOf(initialItem?.descHi ?: "") }
    var descEn by remember { mutableStateOf(initialItem?.descEn ?: "") }
    var isVeg by remember { mutableStateOf(initialItem?.isVeg ?: true) }
    var spicyLevel by remember { mutableStateOf(initialItem?.spicyLevel ?: SpicyLevel.MEDIUM) }
    var emoji by remember { mutableStateOf(initialItem?.emoji ?: "🍛") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val currentPrepTime = prepTimeText.toIntOrNull() ?: (initialItem?.prepTimeMin ?: 15)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = DhabaRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isEditMode) "व्यंजन संपादित करें (Edit Dish)" else "नया व्यंजन जोड़ें (Add Dish)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_dish_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hindi Name
                OutlinedTextField(
                    value = nameHi,
                    onValueChange = { nameHi = it },
                    label = { Text("हिंदी नाम (उदा: दाल तड़का)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_dish_name_hi"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // English Name
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("English Name (e.g. Yellow Dal Tadka)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_dish_name_en"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category and Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Picker
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(56.dp)
                            .clickable { categoryDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${selectedCategory.emoji} ${selectedCategory.titleHi}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            MenuCategory.entries.filter { it != MenuCategory.ALL }.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.emoji} ${cat.titleHi} (${cat.titleEn})", fontSize = 12.sp) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Price Input
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("कीमत (₹)", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_dish_price"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preparation Time in Minutes (तैयारी का समय मिनटों में) Input Field
                OutlinedTextField(
                    value = prepTimeText,
                    onValueChange = { prepTimeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Preparation Time in Minutes (तैयारी का समय मिनटों में)", fontSize = 12.sp) },
                    placeholder = { Text("15", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Prep Time",
                            tint = DhabaRed,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prep_time_minutes_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick preset buttons for prep time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(3, 5, 10, 15, 20, 30).forEach { mins ->
                        val isSelected = currentPrepTime == mins
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) DhabaRed else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { prepTimeText = mins.toString() }
                        ) {
                            Text(
                                text = "$mins min",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hindi Description
                OutlinedTextField(
                    value = descHi,
                    onValueChange = { descHi = it },
                    label = { Text("हिंदी विवरण (उदा: लहसुन और जीरे के तड़के वाली दाल)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_dish_desc_hi"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // English Description
                OutlinedTextField(
                    value = descEn,
                    onValueChange = { descEn = it },
                    label = { Text("English Description (e.g. Spiced yellow lentils with cumin)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_dish_desc_en"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Emoji and Veg Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Emoji Picker Quick Chips
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("🍲", "🍛", "🫓", "🥟", "🍢", "🍚", "🥤", "🍨").forEach { emo ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (emoji == emo) DhabaRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { emoji = emo }
                            ) {
                                Text(
                                    text = emo,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }

                    // Pure Veg Switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (isVeg) "शाकाहारी 🌱" else "Non-Veg", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isVeg,
                            onCheckedChange = { isVeg = it },
                            modifier = Modifier.testTag("new_dish_veg_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save Button
                Button(
                    onClick = {
                        val parsedPrice = priceText.toIntOrNull() ?: 100
                        val finalPrepTime = prepTimeText.toIntOrNull()?.coerceAtLeast(1) ?: 15
                        if (nameHi.isNotBlank() || nameEn.isNotBlank()) {
                            onSaveDish(
                                initialItem?.id,
                                if (nameHi.isNotBlank()) nameHi else nameEn,
                                if (nameEn.isNotBlank()) nameEn else nameHi,
                                selectedCategory,
                                parsedPrice,
                                if (descHi.isNotBlank()) descHi else "स्वादिष्ट और ताज़ा भोजन",
                                if (descEn.isNotBlank()) descEn else "Fresh and delicious dish",
                                isVeg,
                                spicyLevel,
                                emoji,
                                finalPrepTime
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_new_dish_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DhabaRed)
                ) {
                    Text(
                        text = if (isEditMode) "बदलाव सहेजें (Save Changes to Supabase)" else "मेनू में शामिल करें (Save to Supabase)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Backward-compatible overload for adding dishes without an initial item
@Composable
fun AddDishDialog(
    onDismiss: () -> Unit,
    onAddDish: (
        nameHi: String,
        nameEn: String,
        category: MenuCategory,
        price: Int,
        descHi: String,
        descEn: String,
        isVeg: Boolean,
        spicyLevel: SpicyLevel,
        emoji: String,
        prepTimeMin: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    AddDishDialog(
        onDismiss = onDismiss,
        initialItem = null,
        onSaveDish = { _, nameHi, nameEn, category, price, descHi, descEn, isVeg, spicyLevel, emoji, prepTimeMin ->
            onAddDish(nameHi, nameEn, category, price, descHi, descEn, isVeg, spicyLevel, emoji, prepTimeMin)
        },
        modifier = modifier
    )
}
