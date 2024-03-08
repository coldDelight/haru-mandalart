package com.coldblue.history

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coldblue.history.content.HistoryContent
import java.time.LocalDate

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(),
    navigateToSetting: () -> Unit,
) {

    val historyUiState by historyViewModel.historyUiState.collectAsStateWithLifecycle()
    Log.e("TAG", "HistoryScreen: $historyUiState", )

    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        HistoryContentWithState(
            historyUiState,
            historyViewModel::selectDate
        )
    }
//    Column {
//        Text(text = "HistoryScreen")
//        Button(onClick = { navigateToSetting() }) {
//            Text(text = "Navigate To Setting")
//        }
//    }
}

@Composable
fun HistoryContentWithState(
    uiState: HistoryUiState,
    selectDate: (LocalDate) -> Unit
) {
    when (uiState) {
        is HistoryUiState.Loading -> Text(text = "로딩")
        is HistoryUiState.Error -> Text(text = uiState.msg)
        is HistoryUiState.Success ->
            HistoryContent(
                uiState,
                selectDate
            )
    }

}