package com.optirout.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.optirout.data.model.TransportModal
import com.optirout.viewmodel.RouteViewModel

@Composable
fun ModalSelectionScreen(
    viewModel: RouteViewModel = viewModel(),
    onRouteStarted: (TransportModal) -> Unit = {}
) {
    val selectedModal by viewModel.selectedModal
    val modals by viewModel.modals
    val isLoading by viewModel.isLoading
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1419))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "OptiRout",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Escolha o modal e inicie a consulta de rotas.",
            fontSize = 13.sp,
            color = Color(0xFFB0B0B0),
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Grid de Modais (2 colunas)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Linha 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModalButton(
                    modal = modals[0],
                    isSelected = selectedModal?.id == modals[0].id,
                    onClick = { viewModel.selectModal(modals[0]) },
                    modifier = Modifier.weight(1f)
                )
                ModalButton(
                    modal = modals[1],
                    isSelected = selectedModal?.id == modals[1].id,
                    onClick = { viewModel.selectModal(modals[1]) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Linha 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModalButton(
                    modal = modals[2],
                    isSelected = selectedModal?.id == modals[2].id,
                    onClick = { viewModel.selectModal(modals[2]) },
                    modifier = Modifier.weight(1f)
                )
                ModalButton(
                    modal = modals[3],
                    isSelected = selectedModal?.id == modals[3].id,
                    onClick = { viewModel.selectModal(modals[3]) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Linha 3 (apenas 1 item)
            ModalButton(
                modal = modals[4],
                isSelected = selectedModal?.id == modals[4].id,
                onClick = { viewModel.selectModal(modals[4]) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botão
        Button(
            onClick = {
                selectedModal?.let {
                    viewModel.startRoute()
                    onRouteStarted(it)
                }
            },
            enabled = selectedModal != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C7FFF),
                disabledContainerColor = Color(0xFF4B5563)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Abrir sessão de rotas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ModalButton(
    modal: TransportModal,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = animateColorAsState(
        targetValue = if (isSelected) Color(0xFF6366F1) else Color(0xFF2A3341),
        label = "bgColor"
    )
    
    val borderColor = animateColorAsState(
        targetValue = if (isSelected) Color(0xFF818CF8) else Color.Transparent,
        label = "borderColor"
    )
    
    Surface(
        modifier = modifier
            .height(90.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor.value,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.value,
        shadowElevation = if (isSelected) 4.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone pequeno (sem emoji grande)
            Text(
                text = modal.emoji,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = modal.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}