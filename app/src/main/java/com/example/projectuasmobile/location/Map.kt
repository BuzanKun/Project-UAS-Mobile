package com.example.projectuasmobile.location

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Query

@Composable
fun MapScreen(
    location: LocationData,
    onLocationSelected: (LocationData, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userLocation = remember {
        mutableStateOf(LatLng(location.latitude, location.longitude))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation.value, 10f)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier
                .weight(1f)
                .padding(top = 16.dp),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                userLocation.value = it
            }
        ) {
            Marker(state = MarkerState(position = userLocation.value))
        }

        var newLocation: LocationData

        Button(onClick = {
            coroutineScope.launch {
                newLocation = LocationData(userLocation.value.latitude, userLocation.value.longitude)
                val address = getAddressFromCoordinates(context, newLocation.latitude, newLocation.longitude)
                onLocationSelected(newLocation, address)
            }
        }) {
            Text(text = "Set Location")
        }
    }
}

suspend fun getAddressFromCoordinates(context: Context, lat: Double, lng: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = "AIzaSyCG4clM-N9yrQlwNClNNCG5InXShcWkRPc" // Make sure you have this in your strings.xml
            val response = RetrofitClient.create().getAddressFromCoordinates("$lat,$lng", apiKey)
            if (response.status == "OK" && response.results.isNotEmpty()) {
                response.results[0].formatted_address
            } else {
                "Address not found"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error retrieving address"
        }
    }
}