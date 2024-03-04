package com.coldblue.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coldblue.designsystem.theme.HMColor
import com.coldblue.designsystem.theme.HmStyle

@Composable
fun HMMandaFillButton(
    name: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1F)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
//            .clickable { onClick() }
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = name,
            color = textColor,
            modifier = Modifier.padding(5.dp),
            style = HmStyle.text8
        )
    }
}

@Preview
@Composable
fun HMMandaFillButtonPreview() {
    HMMandaFillButton(
        name = "TEST",
        backgroundColor = HMColor.Dark.Pink,
        textColor = HMColor.Text
    ){}
}