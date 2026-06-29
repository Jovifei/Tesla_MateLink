package com.teslamatelink.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import com.teslamatelink.data.model.Instance
import com.teslamatelink.domain.model.CarImageOverride
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "teslamatelink_settings")

data class AppSettings(
    val serverUrl: String = "",
    val secondaryServerUrl: String = "",
    val apiToken: String = "",
    val httpBasicAuthUsername: String = "",
    val httpBasicAuthPassword: String = "",
    val acceptInvalidCerts: Boolean = false,
    val currencyCode: String = "EUR",
    val showShortDrivesCharges: Boolean = false,
    val teslamateBaseUrl: String = "",
    val lastSelectedCarId: Int? = null,
    val useRealDataSource: Boolean = false
) {
    val isConfigured: Boolean
        get() = serverUrl.isNotBlank()

    val hasSecondaryServer: Boolean
        get() = secondaryServerUrl.isNotBlank()
}

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStore: SecureSettingsDataStore
) {
    private val serverUrlKey = stringPreferencesKey("server_url")
    private val secondaryServerUrlKey = stringPreferencesKey("secondary_server_url")
    private val acceptInvalidCertsKey = booleanPreferencesKey("accept_invalid_certs")
    private val currencyCodeKey = stringPreferencesKey("currency_code")
    private val showShortDrivesChargesKey = booleanPreferencesKey("show_short_drives_charges")
    private val teslamateBaseUrlKey = stringPreferencesKey("teslamate_base_url")
    private val lastSelectedCarIdKey = intPreferencesKey("last_selected_car_id")
    private val carImageOverridesKey = stringPreferencesKey("car_image_overrides")
    private val instancesKey = stringPreferencesKey("instances")
    private val activeInstanceIdKey = stringPreferencesKey("active_instance_id")
    private val notificationPermissionAskedKey = booleanPreferencesKey("notification_permission_asked")
    private val useRealDataSourceKey = booleanPreferencesKey("use_real_data_source")

    /**
     * Snapshot of the data-source flag for synchronous reads from non-coroutine
     * contexts (e.g. DelegatingCarRepository.delegate getter).
     * Initialized to false; updated by [initUseRealDataSourceSnapshot].
     */
    private val useRealDataSourceSnapshotFlow = MutableStateFlow(false)

    val useRealDataSource: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[useRealDataSourceKey] ?: false
    }

    val notificationPermissionAsked: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[notificationPermissionAskedKey] ?: false
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            serverUrl = preferences[serverUrlKey] ?: "",
            secondaryServerUrl = preferences[secondaryServerUrlKey] ?: "",
            apiToken = secureStore.getApiToken(),
            httpBasicAuthUsername = secureStore.getHttpBasicUsername(),
            httpBasicAuthPassword = secureStore.getHttpBasicPassword(),
            acceptInvalidCerts = preferences[acceptInvalidCertsKey] ?: false,
            currencyCode = preferences[currencyCodeKey] ?: "EUR",
            showShortDrivesCharges = preferences[showShortDrivesChargesKey] ?: false,
            teslamateBaseUrl = preferences[teslamateBaseUrlKey] ?: "",
            lastSelectedCarId = preferences[lastSelectedCarIdKey],
            useRealDataSource = preferences[useRealDataSourceKey] ?: false
        )
    }

    val showShortDrivesCharges: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[showShortDrivesChargesKey] ?: false
    }

    /**
     * Flow of car image overrides, keyed by car ID.
     */
    val carImageOverrides: Flow<Map<Int, CarImageOverride>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[carImageOverridesKey] ?: "{}"
        parseOverridesJson(jsonString)
    }

    private fun parseOverridesJson(jsonString: String): Map<Int, CarImageOverride> {
        return try {
            val result = mutableMapOf<Int, CarImageOverride>()
            val obj = JSONObject(jsonString)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val carId = key.toIntOrNull() ?: continue
                val overrideJson = obj.getJSONObject(key)
                val override = CarImageOverride(
                    variant = overrideJson.getString("variant"),
                    wheelCode = overrideJson.getString("wheelCode")
                )
                result[carId] = override
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    val instances: Flow<List<Instance>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[instancesKey] ?: "[]"
        parseInstancesJson(jsonString)
    }

    val activeInstanceId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[activeInstanceIdKey]
    }

    private fun parseInstancesJson(jsonString: String): List<Instance> {
        return try {
            val arr = JSONArray(jsonString)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Instance(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    serverUrl = obj.getString("serverUrl"),
                    apiToken = obj.getString("apiToken"),
                    carId = obj.optInt("carId", 1)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun instancesToJson(instances: List<Instance>): String {
        val arr = JSONArray()
        for (instance in instances) {
            val obj = JSONObject()
            obj.put("id", instance.id)
            obj.put("name", instance.name)
            obj.put("serverUrl", instance.serverUrl)
            obj.put("apiToken", instance.apiToken)
            obj.put("carId", instance.carId)
            arr.put(obj)
        }
        return arr.toString()
    }

    suspend fun saveInstances(instances: List<Instance>) {
        context.dataStore.edit { preferences ->
            preferences[instancesKey] = instancesToJson(instances)
        }
    }

    suspend fun saveActiveInstanceId(id: String?) {
        context.dataStore.edit { preferences ->
            if (id != null) {
                preferences[activeInstanceIdKey] = id
            } else {
                preferences.remove(activeInstanceIdKey)
            }
        }
    }

    private fun overridesToJson(overrides: Map<Int, CarImageOverride>): String {
        val obj = JSONObject()
        for ((carId, override) in overrides) {
            val overrideObj = JSONObject()
            overrideObj.put("variant", override.variant)
            overrideObj.put("wheelCode", override.wheelCode)
            obj.put(carId.toString(), overrideObj)
        }
        return obj.toString()
    }

    suspend fun saveSettings(
        serverUrl: String,
        secondaryServerUrl: String,
        apiToken: String,
        httpBasicAuthUsername: String,
        httpBasicAuthPassword: String,
        acceptInvalidCerts: Boolean,
        currencyCode: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[serverUrlKey] = serverUrl
            preferences[secondaryServerUrlKey] = secondaryServerUrl
            preferences[acceptInvalidCertsKey] = acceptInvalidCerts
            preferences[currencyCodeKey] = currencyCode
        }
        secureStore.setApiToken(apiToken)
        secureStore.setHttpBasicUsername(httpBasicAuthUsername)
        secureStore.setHttpBasicPassword(httpBasicAuthPassword)
    }

    suspend fun saveHttpBasicAuth(username: String, password: String) {
        secureStore.setHttpBasicUsername(username)
        secureStore.setHttpBasicPassword(password)
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[serverUrlKey] = url
        }
    }

    suspend fun saveCurrency(currencyCode: String) {
        context.dataStore.edit { preferences ->
            preferences[currencyCodeKey] = currencyCode
        }
    }

    suspend fun saveShowShortDrivesCharges(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[showShortDrivesChargesKey] = show
        }
    }

    suspend fun saveTeslamateBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[teslamateBaseUrlKey] = url
        }
    }

    suspend fun saveLastSelectedCarId(carId: Int) {
        context.dataStore.edit { preferences ->
            preferences[lastSelectedCarIdKey] = carId
        }
    }

    /**
     * Save or clear a car image override.
     *
     * @param carId The car ID to save the override for
     * @param override The override to save, or null to clear
     */
    suspend fun saveCarImageOverride(carId: Int, override: CarImageOverride?) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[carImageOverridesKey] ?: "{}"
            val currentMap = parseOverridesJson(currentJson).toMutableMap()

            if (override != null) {
                currentMap[carId] = override
            } else {
                currentMap.remove(carId)
            }

            preferences[carImageOverridesKey] = overridesToJson(currentMap)
        }
    }

    suspend fun saveNotificationPermissionAsked() {
        context.dataStore.edit { preferences ->
            preferences[notificationPermissionAskedKey] = true
        }
    }

    /**
     * Synchronous read of the data-source flag.
     * Call [initUseRealDataSourceSnapshot] once at app startup to populate.
     */
    fun useRealDataSourceSnapshot(): Boolean = useRealDataSourceSnapshotFlow.value

    /**
     * Populate the snapshot from DataStore. Call once at app startup
     * (e.g. in Application.onCreate or via Hilt initializer).
     */
    suspend fun initUseRealDataSourceSnapshot() {
        useRealDataSourceSnapshotFlow.value = useRealDataSource.first()
    }

    suspend fun setUseRealDataSource(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[useRealDataSourceKey] = value
        }
        useRealDataSourceSnapshotFlow.value = value
    }

    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        secureStore.clearAll()
    }
}
