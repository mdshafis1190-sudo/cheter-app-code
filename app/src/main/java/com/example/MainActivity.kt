package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppMode
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MenuViewModel

class MainActivity : ComponentActivity() {

  private var currentViewModel: MenuViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val context = LocalContext.current
          val viewModel: MenuViewModel = viewModel()
          currentViewModel = viewModel
          val uiState by viewModel.uiState.collectAsState()

          LaunchedEffect(Unit) {
            viewModel.initSession(context)
            handleDeepLink(intent, viewModel)
          }

          if (uiState.appMode == AppMode.SELLER_DASHBOARD) {
            SellerDashboardScreen(viewModel = viewModel)
          } else {
            MenuScreen(viewModel = viewModel)
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    currentViewModel?.let { handleDeepLink(intent, it) }
  }

  private fun handleDeepLink(intent: Intent?, viewModel: MenuViewModel) {
    val data = intent?.data ?: return
    val hotelParam = data.getQueryParameter("hotel")
      ?: data.getQueryParameter("hotel_id")
      ?: data.getQueryParameter("hotelId")
      ?: data.getQueryParameter("shopId")
      ?: data.getQueryParameter("shop_id")

    val tableParam = data.getQueryParameter("table")
      ?: data.getQueryParameter("table_number")
      ?: data.getQueryParameter("tableNumber")
      ?: data.getQueryParameter("t")

    viewModel.setHotelAndTableFromUrl(
      hotelParam = hotelParam,
      tableParam = tableParam?.toIntOrNull()
    )
  }
}
