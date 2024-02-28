package com.coldblue.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coldblue.designsystem.IconPack
import com.coldblue.designsystem.component.HMChip
import com.coldblue.designsystem.component.HMTextField
import com.coldblue.designsystem.iconpack.Plus
import com.coldblue.designsystem.theme.HMColor
import com.coldblue.designsystem.theme.HmStyle
import com.coldblue.model.CurrentGroup
import com.coldblue.model.TodoGroup
import com.coldblue.model.ToggleInfo

@Composable
fun TodoGroupBottomSheetWithState(
    currentGroup: CurrentGroupState,
    todoGroupList: List<TodoGroup>,
    currentGroupList: List<CurrentGroupState>,
    upsertCurrentGroup: (CurrentGroup) -> Unit,
    upsertTodoGroup: (TodoGroup) -> Unit,
) {
    val usingGroupId =
        currentGroupList.map { it.currentGroup?.todoGroupId ?: -1 }.filter { it != -1 }

    when (currentGroup) {
        is CurrentGroupState.Empty -> {
            val todoGroups = todoGroupList.filter { !usingGroupId.contains(it.id) }
            TodoGroupBottomSheet(
                todoGroups,
                upsertCurrentGroup,
                currentGroup.index,
                -1,
                upsertTodoGroup
            )
        }

        is CurrentGroupState.Doing -> {
            val id = currentGroup.currentGroup.todoGroupId
            val todoGroups =
                todoGroupList.filter { !usingGroupId.subtract(listOf(id).toSet()).contains(it.id) }

            TodoGroupBottomSheet(
                todoGroups,
                upsertCurrentGroup,
                currentGroup.index,
                currentGroup.currentGroup.todoGroupId,
                upsertTodoGroup
            )
        }

        is CurrentGroupState.Done -> {
            val id = currentGroup.currentGroup.todoGroupId
            val todoGroups =
                todoGroupList.filter { !usingGroupId.subtract(listOf(id).toSet()).contains(it.id) }
            TodoGroupBottomSheet(
                todoGroups,
                upsertCurrentGroup,
                currentGroup.index,
                currentGroup.currentGroup.todoGroupId,
                upsertTodoGroup
            )
        }

        else -> {}
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodoGroupBottomSheet(
    todoGroupList: List<TodoGroup>,
    upsertCurrentGroup: (CurrentGroup) -> Unit,
    currentId: Int,
    id: Int,
    upsertTodoGroup: (TodoGroup) -> Unit,

    ) {
    var openDialog by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<DiaLogState>(DiaLogState.InsertGroup(onInsertGroup = upsertTodoGroup)) }

    val radioButtons = remember {
        mutableStateListOf<ToggleInfo>().apply {
            addAll(todoGroupList.mapIndexed { index, todoGroup ->
                ToggleInfo(
                    isChecked = if (currentId == -1) index == 0 else (todoGroup.id == currentId),
                    text = todoGroup.name,
                    id = id,
                    todoGroupId = todoGroup.id
                )
            })
        }
    }
    if (openDialog) {
        SimpleDialog(
            dialogState,
            onDismissRequest = {
                openDialog = it
            },
            onConfirmation = {},
        )
    }

    Column(
    ) {
        Text(
            text = "그룹", modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start, style = HmStyle.text16, fontWeight = FontWeight.Bold
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.1f),
            verticalArrangement = Arrangement.Center,
//            horizontalArrangement = Arrangement.spacedBy(.dp),
        ) {

            radioButtons.forEachIndexed { index, toggleInfo ->
                OutlinedButton(
                    contentPadding = PaddingValues(
                        4.dp
                    ),
                    modifier = Modifier
                        .fillMaxHeight(0.7f)
                        .padding(8.dp)
                        .border(
                            width = 1.dp,
                            color = HMColor.Primary,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .background(if (toggleInfo.isChecked) HMColor.Primary else HMColor.Background)
                        .clip(RoundedCornerShape(5.dp)),
                    shape = RoundedCornerShape(5.dp),
                    onClick = {
                        upsertCurrentGroup(
                            CurrentGroup(
                                id = id,
                                name = toggleInfo.text,
                                todoGroupId = toggleInfo.todoGroupId
                            )
                        )
                        radioButtons.replaceAll {
                            it.copy(isChecked = it.text == toggleInfo.text)
                        }
                    }) {
                    Text(
                        text = toggleInfo.text,
                        color = if (toggleInfo.isChecked) HMColor.Background else HMColor.Primary
                    )
                }
            }

            OutlinedButton(
                contentPadding = PaddingValues(
                    0.dp
                ),
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .aspectRatio(1.1f)
                    .padding(8.dp)
                    .border(
                        width = 1.dp,
                        color = HMColor.Primary,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .clip(RoundedCornerShape(5.dp))
                    .background(HMColor.Background),
                onClick = {
                    dialogState = DiaLogState.InsertGroup(onInsertGroup = upsertTodoGroup)
                    openDialog = true
                },
                shape = RoundedCornerShape(5.dp)
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = IconPack.Plus,
                    contentDescription = null,
                    tint = HMColor.Primary
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDialog(
    dialogState: DiaLogState,
    onDismissRequest: (Boolean) -> Unit,
    onConfirmation: (Boolean) -> Unit,
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        shape = RoundedCornerShape(10.dp),

        containerColor = HMColor.Background,
        text = {
            Column {
                when (dialogState) {
                    is DiaLogState.InsertGroup -> {
                        Row {
                            Text(
                                text = "그룹이름",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = inputText,
                            onValueChange = {
                                inputText = it
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = HMColor.Primary,
                                containerColor = Color.Transparent
                            ),
                        )


                    }

                    is DiaLogState.UpdateGroup -> {

                    }

                    is DiaLogState.DeleteGroup -> {

                    }
                }
            }
        }, onDismissRequest = {
            onDismissRequest(false)
        }, confirmButton = {
            TextButton(onClick = {
                when(dialogState){
                    is DiaLogState.InsertGroup->{
                        dialogState.onInsertGroup(TodoGroup(inputText))
                    }else->{

                    }
                }
                onConfirmation(true)
                onDismissRequest(false)
            }) {
                Text(
                    text = dialogState.confirmText,
                    color = dialogState.confirmColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }, dismissButton = {
            TextButton(onClick = {
                onDismissRequest(false)
            }) {
                Text("취소", fontWeight = FontWeight.Bold, color = HMColor.Text)
            }
        })
}


