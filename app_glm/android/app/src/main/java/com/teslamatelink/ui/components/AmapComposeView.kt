package com.teslamatelink.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.teslamatelink.domain.GCJ02Converter

data class MapMarker(
    val lat: Double,
    val lng: Double,
    val title: String = "",
    val snippet: String = ""
)

/**
 * Compose wrapper for AMap (高德地图) MapView.
 * Handles lifecycle management via LifecycleEventObserver.
 * All WGS-84 coordinates are converted to GCJ-02 internally.
 */
@Composable
fun AmapComposeView(
    lat: Double,
    lng: Double,
    zoom: Float = 15f,
    markers: List<MapMarker> = emptyList(),
    onMarkerClick: ((MapMarker) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { TextureMapView(context) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { map ->
        val aMap = map.map ?: return@AndroidView

        aMap.clear()

        val gcjPoint = GCJ02Converter.wgs84ToGcj02(lat, lng)
        aMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(gcjPoint.first, gcjPoint.second), zoom)
        )

        markers.forEach { marker ->
            val gcj = GCJ02Converter.wgs84ToGcj02(marker.lat, marker.lng)
            aMap.addMarker(
                MarkerOptions()
                    .position(LatLng(gcj.first, gcj.second))
                    .title(marker.title)
                    .snippet(marker.snippet)
            )
        }

        if (onMarkerClick != null) {
            aMap.setOnMarkerClickListener { amapMarker ->
                val marker = markers.find {
                    val gcj = GCJ02Converter.wgs84ToGcj02(it.lat, it.lng)
                    amapMarker.position.latitude == gcj.first &&
                        amapMarker.position.longitude == gcj.second
                }
                marker?.let { onMarkerClick(it) }
                true
            }
        }
    }
}
