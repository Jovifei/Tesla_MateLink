package com.teslamatelink.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.teslamatelink.R
import com.teslamatelink.data.local.SettingsDataStore
import com.teslamatelink.data.model.Instance
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsDataStore = remember { SettingsDataStore(context, com.teslamatelink.data.local.SecureSettingsDataStore(context)) }

    // Use shared AppSettings so values persist across recompositions
    var serverUrl by remember { mutableStateOf(AppSettings.serverUrl) }
    var apiToken by remember { mutableStateOf(AppSettings.apiToken) }
    var isDarkTheme by remember { mutableStateOf(AppSettings.isDarkTheme) }
    var isMockMode by remember { mutableStateOf(AppSettings.isMockMode) }
    var useRealDataSource by remember { mutableStateOf(false) }
    var showAddInstanceDialog by remember { mutableStateOf(false) }
    var newInstanceName by remember { mutableStateOf("") }
    var newInstanceUrl by remember { mutableStateOf("") }
    var newInstanceToken by remember { mutableStateOf("") }

    // Load instances from DataStore on first composition
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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instances
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.instances), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAddInstanceDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_instance))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSettings.instances.forEach { instance ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(instance.name, style = MaterialTheme.typography.bodyMedium)
                                Text(instance.serverUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (instance.id == AppSettings.activeInstanceId) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            } else {
                                TextButton(onClick = {
                                    AppSettings.switchInstance(instance)
                                    serverUrl = instance.serverUrl
                                    apiToken = instance.apiToken
                                    scope.launch {
                                        settingsDataStore.saveActiveInstanceId(instance.id)
                                        settingsDataStore.saveServerUrl(instance.serverUrl)
                                    }
                                }) {
                                    Text(stringResource(R.string.switch_instance))
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                    if (AppSettings.instances.isEmpty()) {
                        Text(
                            stringResource(R.string.no_instances),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Connection
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.server_connection), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = {
                            serverUrl = it
                            AppSettings.serverUrl = it
                        },
                        label = { Text(stringResource(R.string.server_url)) },
                        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiToken,
                        onValueChange = {
                            apiToken = it
                            AppSettings.apiToken = it
                        },
                        label = { Text(stringResource(R.string.api_token)) },
                        leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToDashboard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.save_reconnect))
                    }
                }
            }

            // Appearance
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.padding(start = 12.dp))
                            Text(stringResource(R.string.dark_theme))
                        }
                        Switch(checked = isDarkTheme, onCheckedChange = {
                            isDarkTheme = it
                            AppSettings.isDarkTheme = it
                        })
                    }
                }
            }

            // Debug
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.debug), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.mock_mode))
                        Switch(checked = isMockMode, onCheckedChange = {
                            isMockMode = it
                            AppSettings.isMockMode = it
                        })
                    }
                    Text(
                        stringResource(R.string.mock_mode_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Data Source
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.data_source), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.use_real_data))
                        Switch(checked = useRealDataSource, onCheckedChange = { newValue ->
                            useRealDataSource = newValue
                            scope.launch {
                                settingsDataStore.setUseRealDataSource(newValue)
                            }
                        })
                    }
                    Text(
                        stringResource(R.string.use_real_data_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // About
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                        Spacer(modifier = Modifier.padding(start = 12.dp))
                        Text(stringResource(R.string.about), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Button(onClick = onNavigateToAbout) {
                        Text(stringResource(R.string.open))
                    }
                }
            }
        }
    }

    if (showAddInstanceDialog) {
        AlertDialog(
            onDismissRequest = { showAddInstanceDialog = false },
            title = { Text(stringResource(R.string.add_instance)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newInstanceName,
                        onValueChange = { newInstanceName = it },
                        label = { Text(stringResource(R.string.instance_name)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newInstanceUrl,
                        onValueChange = { newInstanceUrl = it },
                        label = { Text(stringResource(R.string.server_url)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newInstanceToken,
                        onValueChange = { newInstanceToken = it },
                        label = { Text(stringResource(R.string.api_token)) },
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
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddInstanceDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
