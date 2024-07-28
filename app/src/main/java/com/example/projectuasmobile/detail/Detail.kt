package com.example.projectuasmobile.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.projectuasmobile.Utils
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.projectuasmobile.MainActivity
import com.example.projectuasmobile.location.LocationUtils
import com.example.projectuasmobile.location.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DetailScreen(
    locationUtils: LocationUtils,
    detailViewModel: DetailViewModel?,
    locationViewModel: LocationViewModel,
    context: Context,
    noteId: String,
    navController: NavController,
    onNavigate: () -> Unit
){
    val detailUiState = detailViewModel?.detailUiState ?: DetailUiState()

    val isFormNotBlank = detailUiState.note.isNotBlank() &&
            detailUiState.title.isNotBlank()

    val selectedColor by animateColorAsState(targetValue = Utils.colors[detailUiState.colorIndex])

    val isNoteIdNotBlank = noteId.isNotBlank()
    val icon = if (isNoteIdNotBlank) Icons.Default.Refresh
        else Icons.Default.Check

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){

                locationUtils.requestLocationUpdates(locationViewModel = locationViewModel)
            } else {
                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if(rationalRequired) {
                    Toast.makeText(context,
                        "Location Permission is required for this feature to work", Toast.LENGTH_LONG)
                        .show()
                } else{
                    Toast.makeText(context,
                        "Location Permission is required. Please enable it in the Android Settings",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    )

    LaunchedEffect(key1 = Unit) {
        if (isNoteIdNotBlank){
            detailViewModel?.getNote(noteId)
        }else{
            detailViewModel?.resetState()
        }
    }

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(visible = isFormNotBlank) {
                FloatingActionButton(
                    onClick = {
                        if (isNoteIdNotBlank){
                            detailViewModel?.updateNote(noteId)
                        }else{
                            detailViewModel?.addNote()
                        }
                    }
                ) {
                    Icon(imageVector = icon , contentDescription = null)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = selectedColor)
                .padding(padding)
        ) {
            if (detailUiState.noteAddedStatus){
                scope.launch {
                    snackbarHostState
                        .showSnackbar("Note telah ditambahkan")
                    detailViewModel?.resetNoteAddesStatus()
                    onNavigate.invoke()
                }
            }

            if (detailUiState.updateNoteStatus){
                scope.launch {
                    snackbarHostState
                        .showSnackbar("Note telah diupdate")
                    detailViewModel?.resetNoteAddesStatus()
                    onNavigate.invoke()
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp)
            ) {
                itemsIndexed(Utils.colors){ colorIndex, color ->
                    ColorItem(color = color) {
                        detailViewModel?.onColorChange(colorIndex)
                    }

                }
            }
            OutlinedTextField(
                value = detailUiState.title,
                onValueChange = {detailViewModel?.onTitleChange(it) },
                label = { Text(text = "Title")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = detailUiState.note,
                onValueChange = {detailViewModel?.onNoteChange(it)},
                label = { Text(text = "Notes")},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = detailUiState.address,
                onValueChange = {detailViewModel?.onAddressChange(it)},
                label = { Text(text = "Address")},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            )
            Button(onClick = {
                if(locationUtils.hasLocationPermission(context)){
                    locationUtils.requestLocationUpdates(locationViewModel)
                    navController.navigate("locationscreen"){
                        this.launchSingleTop
                    }
                } else {
                    requestPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
                }
            },
                modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                Text(text = "Set Address")
            }
        }
    }
}

@Composable
fun ColorItem(
    color: Color,
    onClick:() -> Unit
){
    Surface(
        color = color,
        shape = CircleShape,
        modifier = Modifier
            .padding(8.dp)
            .size(36.dp)
            .clickable {
                onClick.invoke()
            },
        border = BorderStroke(2.dp, Color.Black)
    ) {

    }
}