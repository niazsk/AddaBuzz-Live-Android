package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddaBuzzRepository
import com.example.data.LineKind
import com.example.data.TerminalEngine
import com.example.data.TerminalLine
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

private val TerminalBackground = Color(0xFF05040C)
private val TerminalSurface = Color(0xFF0E0B1C)
private val TerminalGreen = Color(0xFF4ADE80)
private val TerminalAmber = Color(0xFFFBBF24)

private val quickCommands = listOf("help", "status", "whoami", "rooms", "users", "wallet", "gifts", "seats", "clear")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val lines = remember {
        mutableStateListOf(
            TerminalLine("AddaBuzz Live Shell v1.0.0", LineKind.ACCENT),
            TerminalLine("Type 'help' to list available commands.", LineKind.MUTED)
        )
    }
    val history = remember { mutableStateListOf<String>() }
    var input by remember { mutableStateOf("") }
    var historyIndex by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()

    fun runCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return
        lines += TerminalLine("addabuzz@live:~$ $trimmed", LineKind.INPUT)
        history += trimmed
        historyIndex = -1
        val output = TerminalEngine.execute(trimmed, repository)
        output.forEach { line ->
            if (line.text == TerminalEngine.CLEAR) {
                lines.clear()
            } else {
                lines += line
            }
        }
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
    }

    Scaffold(
        containerColor = TerminalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = TerminalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AddaBuzz Terminal",
                            style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { lines.clear() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear output", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TerminalSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(lines) { line ->
                    TerminalLineView(line)
                }
            }

            QuickCommandBar(
                commands = quickCommands,
                onSelect = { runCommand(it) }
            )

            CommandInput(
                value = input,
                onValueChange = { input = it },
                onSubmit = {
                    runCommand(input)
                    input = ""
                },
                onHistory = { direction ->
                    if (history.isEmpty()) return@CommandInput
                    historyIndex = when (direction) {
                        -1 -> -1
                        else -> (historyIndex + direction).coerceIn(0, history.lastIndex)
                    }
                    input = if (historyIndex == -1) "" else history[historyIndex]
                }
            )
        }
    }
}

@Composable
private fun TerminalLineView(line: TerminalLine) {
    val color = when (line.kind) {
        LineKind.INPUT -> ElectricCyan
        LineKind.OUTPUT -> TextPrimary
        LineKind.ERROR -> LiveRed
        LineKind.MUTED -> TextMuted
        LineKind.ACCENT -> TerminalGreen
    }
    SelectionContainer {
        Text(
            text = line.text,
            style = Typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = color,
                fontWeight = if (line.kind == LineKind.INPUT) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun QuickCommandBar(
    commands: List<String>,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        commands.forEach { cmd ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceCard,
                modifier = Modifier.clickable { onSelect(cmd) }
            ) {
                Text(
                    text = cmd,
                    style = Typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalAmber
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CommandInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onHistory: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$ ",
            style = Typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = TerminalGreen,
                fontWeight = FontWeight.Bold
            )
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = Typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = TextPrimary
            ),
            cursorBrush = SolidColor(TerminalGreen),
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = { onSubmit() },
                onPrevious = { onHistory(-1) },
                onNext = { onHistory(1) }
            ),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSubmit) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Run command",
                tint = PurplePrimary
            )
        }
    }
}
