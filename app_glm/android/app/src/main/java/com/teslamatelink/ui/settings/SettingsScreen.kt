package com.teslamatelink.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.data.local.SecureSettingsDataStore
import com.teslamatelink.data.local.SettingsDataStore
import com.teslamatelink.data.model.Instance
import com.teslamatelink.ui.components.ChipStatus
import com.teslamatelink.ui.components.StitchStatusChip
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors
import kotlinx.coroutines.launch

/**
 * Shared app-level settings backed by a simple companion object.
 *
 * TODO: Replace with a proper SettingsViewModel backed by DataStore/SharedPreferences
 * once Hilt + DataStore integration is complete.
 */
object AppSettings {
    var isDarkTheme by mutableStateOf(false)
    var isMockMode by mutableStateOf(true)
    var serverUrl by mutableStateOf("http://192.168.1.100:4000")
    var apiToken by mutableStateOf("")
    var instances by mutableStateOf(listOf<Instance>())
    var activeInstanceId by mutableStateOf<String?>(null)

    fun switchInstance(instance: Instance) {
        activeInstanceId = instance.id
        serverUrl = instance.serverUrl
        apiToken = instance.apiToken
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = StitchColors.OnSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun StitchInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = StitchColors.Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StitchColors.Primary,
                unfocusedBorderColor = StitchColors.Border,
                focusedContainerColor = StitchColors.White,
                unfocusedContainerColor = StitchColors.White,
                focusedTextColor = StitchColors.OnSurface,
                unfocusedTextColor = StitchColors.OnSurface,
                cursorColor = StitchColors.Primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = StitchColors.OnSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Box {
            Row(
                modifier = Modifier
                    .border(1.dp, StitchColors.Border, RoundedCornerShape(4.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selected,
                    color = StitchColors.OnSurface,
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = StitchColors.OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { onSelect(opt); expanded = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsDataStore = remember { SettingsDataStore(context, SecureSettingsDataStore(context)) }

    var serverUrl by remember { mutableStateOf(AppSettings.serverUrl) }
    var apiToken by remember { mutableStateOf(AppSettings.apiToken) }
    var isDarkTheme by remember { mutableStateOf(AppSettings.isDarkTheme) }
    var isMockMode by remember { mutableStateOf(AppSettings.isMockMode) }
    var useRealDataSource by remember { mutableStateOf(false) }
    var showAddInstanceDialog by remember { mutableStateOf(false) }
    var newInstanceName by remember { mutableStateOf("") }
    var newInstanceUrl by remember { mutableStateOf("") }
    var newInstanceToken by remember { mutableStateOf("") }
    var testingConnection by remember { mutableStateOf(false) }
    var connectionResult by remember { mutableStateOf<String?>(null) }
    var connectionSuccess by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("中文 (Chinese)") }
    var themeMode by remember { mutableStateOf("Light Mode") }

    LaunchedEffect(Unit) {
        settingsDataStore.instances.collect { loaded ->
            if (loaded.isNotEmpty() && AppSettings.instances.isEmpty()) {
                AppSettings.instances = loaded
            }
        }
    }
    LaunchedEffect(Unit) {
        settingsDataStore.activeInstanceId.collect { id ->
            if (id != null && AppSettings.activeInstanceId == null) {
                AppSettings.activeInstanceId = id
                AppSettings.instances.find { it.id == id }?.let { inst ->
                    AppSettings.serverUrl = inst.serverUrl
                    AppSettings.apiToken = inst.apiToken
                    serverUrl = inst.serverUrl
                    apiToken = inst.apiToken
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        settingsDataStore.useRealDataSource.collect { value ->
            useRealDataSource = value
        }
    }

    Scaffold(
        containerColor = StitchColors.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchColors.White),
                title = {
                    Text(
                        text = "设置",
                        color = StitchColors.OnSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.24).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = StitchColors.OnSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // ── Section 1: 实例 ─────────────────────────────────────
            Column {
                SectionLabel("实例")
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StitchColors.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.Border),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        val activeInstance = AppSettings.instances.find { it.id == AppSettings.activeInstanceId }
                        if (activeInstance != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeInstance.name,
                                        color = StitchColors.OnSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeInstance.serverUrl,
                                        color = StitchColors.OnSurfaceVariant,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = JetBrainsMonoFamily
                                    )
                                }
                                StitchStatusChip(text = "已连接", status = ChipStatus.ONLINE)
                            }
                        } else {
                            Text(
                                text = "未配置实例",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }

                        // Other switchable instances
                        AppSettings.instances.filter { it.id != AppSettings.activeInstanceId }.forEach { inst ->
                            Divider(
                                color = StitchColors.Border,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        AppSettings.switchInstance(inst)
                                        serverUrl = inst.serverUrl
                                        apiToken = inst.apiToken
                                        scope.launch {
                                            settingsDataStore.saveActiveInstanceId(inst.id)
                                            settingsDataStore.saveServerUrl(inst.serverUrl)
                                        }
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(inst.name, color = StitchColors.OnSurface, fontSize = 16.sp)
                                    Text(
                                        inst.serverUrl,
                                        color = StitchColors.OnSurfaceVariant,
                                        fontSize = 14.sp,
                                        fontFamily = JetBrainsMonoFamily
                                    )
                                }
                                Text(
                                    "切换",
                                    color = StitchColors.Primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .clickable { showAddInstanceDialog = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "添加实例",
                                tint = StitchColors.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "添加实例",
                                color = StitchColors.Primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ── Section 2: 连接 ────────────────────────────────────
            Column {
                SectionLabel("连接")
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StitchColors.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.Border),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StitchInputField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it; AppSettings.serverUrl = it },
                            label = "服务器地址"
                        )
                        StitchInputField(
                            value = apiToken,
                            onValueChange = { apiToken = it; AppSettings.apiToken = it },
                            label = "API 令牌",
                            keyboardType = KeyboardType.Password,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    testingConnection = true
                                    connectionResult = null
                                    scope.launch {
                                        kotlinx.coroutines.delay(800)
                                        connectionSuccess = serverUrl.isNotBlank()
                                        connectionResult = if (connectionSuccess) "连接成功" else "地址无效"
                                        testingConnection = false
                                    }
                                },
                                enabled = !testingConnection && serverUrl.isNotBlank(),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = StitchColors.White,
                                    contentColor = StitchColors.OnSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.Border)
                            ) {
                                Text(if (testingConnection) "测试中..." else "测试连接", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    AppSettings.serverUrl = serverUrl
                                    AppSettings.apiToken = apiToken
                                    scope.launch {
                                        settingsDataStore.saveServerUrl(serverUrl)
                                        settingsDataStore.saveApiToken(apiToken)
                                    }
                                    onNavigateToDashboard()
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StitchColors.Primary,
                                    contentColor = StitchColors.OnPrimary
                                )
                            ) {
                                Text("保存", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        connectionResult?.let { result ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (connectionSuccess) StitchColors.StatusOnline else StitchColors.Error,
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                                Text(
                                    result,
                                    color = if (connectionSuccess) StitchColors.StatusOnline else StitchColors.Error,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Section 3: 显示 ────────────────────────────────────
            Column {
                SectionLabel("显示")
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StitchColors.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.Border),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DropdownSelector(
                            label = "语言",
                            selected = language,
                            options = listOf("English", "中文 (Chinese)", "Deutsch"),
                            onSelect = { language = it }
                        )
                        Divider(color = StitchColors.Border)
                        DropdownSelector(
                            label = "主题",
                            selected = themeMode,
                            options = listOf("System", "Light Mode", "Dark Mode"),
                            onSelect = {
                                themeMode = it
                                isDarkTheme = it == "Dark Mode"
                                AppSettings.isDarkTheme = isDarkTheme
                            }
                        )
                        Divider(color = StitchColors.Border)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "模拟模式",
                                    color = StitchColors.OnSurface,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "使用模拟数据进行测试",
                                    color = StitchColors.OnSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                            Switch(
                                checked = isMockMode,
                                onCheckedChange = {
                                    isMockMode = it
                                    AppSettings.isMockMode = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StitchColors.White,
                                    checkedTrackColor = StitchColors.Primary,
                                    uncheckedThumbColor = StitchColors.White,
                                    uncheckedTrackColor = StitchColors.SurfaceContainerHighest
                                )
                            )
                        }
                    }
                }
            }

            // ── Section 4: 关于 ────────────────────────────────────
            Column {
                SectionLabel("关于")
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StitchColors.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.Border),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAbout() }
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "MateLink 应用",
                            color = StitchColors.OnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "v1.2.4 (构建 420)",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = JetBrainsMonoFamily
                            )
                            Text(
                                "October 24, 2023",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddInstanceDialog) {
        AlertDialog(
            onDismissRequest = { showAddInstanceDialog = false },
            title = { Text("添加实例") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newInstanceName,
                        onValueChange = { newInstanceName = it },
                        label = { Text("实例名称") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newInstanceUrl,
                        onValueChange = { newInstanceUrl = it },
                        label = { Text("服务器地址") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newInstanceToken,
                        onValueChange = { newInstanceToken = it },
                        label = { Text("API 令牌") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val instance = Instance(
                            id = java.util.UUID.randomUUID().toString(),
                            name = newInstanceName,
                            serverUrl = newInstanceUrl,
                            apiToken = newInstanceToken
                        )
                        val updated = AppSettings.instances + instance
                        AppSettings.instances = updated
                        AppSettings.switchInstance(instance)
                        serverUrl = instance.serverUrl
                        apiToken = instance.apiToken
                        scope.launch {
                            settingsDataStore.saveInstances(updated)
                            settingsDataStore.saveActiveInstanceId(instance.id)
                            settingsDataStore.saveServerUrl(instance.serverUrl)
                        }
                        newInstanceName = ""
                        newInstanceUrl = ""
                        newInstanceToken = ""
                        showAddInstanceDialog = false
                    },
                    enabled = newInstanceName.isNotBlank() && newInstanceUrl.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showAddInstanceDialog = false }) { Text("取消") }
            }
        )
    }
}
