package com.example.projectuasmobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectuasmobile.detail.DetailScreen
import com.example.projectuasmobile.detail.DetailViewModel
import com.example.projectuasmobile.home.Home
import com.example.projectuasmobile.home.HomeViewModel
import com.example.projectuasmobile.location.LocationUtils
import com.example.projectuasmobile.location.LocationViewModel
import com.example.projectuasmobile.location.MapScreen
import com.example.projectuasmobile.login.LoginScreen
import com.example.projectuasmobile.login.LoginViewModel
import com.example.projectuasmobile.login.SignUpScreen

enum class LoginRoutes {
    Signup,
    SignIn
}

enum class HomeRoutes {
    Home,
    Detail
}

enum class NestedRoutes {
    Main,
    Login
}

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel,
    detailViewModel: DetailViewModel,
    homeViewModel: HomeViewModel,
    locationViewModel: LocationViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NestedRoutes.Main.name
    ) {
        authGraph(navController, loginViewModel)
        homeGraph(navController = navController, detailViewModel, homeViewModel, locationViewModel)
    }
}

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel
){
    navigation(
        startDestination = LoginRoutes.SignIn.name,
        route = NestedRoutes.Login.name
    ){
        composable(route = LoginRoutes.SignIn.name) {
            LoginScreen(onNavToHomePage = {
                navController.navigate(NestedRoutes.Main.name) {
                    launchSingleTop = true
                    popUpTo(route = LoginRoutes.SignIn.name) {
                        inclusive = true
                    }
                }
            },
                loginViewModel = loginViewModel
            ) {
                navController.navigate(LoginRoutes.Signup.name) {
                    launchSingleTop = true
                    popUpTo(LoginRoutes.SignIn.name) {
                        inclusive = true
                    }
                }
            }
        }
        composable(route = LoginRoutes.Signup.name) {
            SignUpScreen(onNavToHomePage = {
                navController.navigate(NestedRoutes.Main.name) {
                    popUpTo(LoginRoutes.Signup.name) {
                        inclusive = true
                    }
                }
            },
                loginViewModel = loginViewModel
            ) {
                navController.navigate(LoginRoutes.SignIn.name)
            }
        }
    }
}

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    detailViewModel: DetailViewModel,
    homeViewModel: HomeViewModel,
    locationViewModel: LocationViewModel,
){
    navigation(
        startDestination = HomeRoutes.Home.name,
        route = NestedRoutes.Main.name
    ){
        composable(HomeRoutes.Home.name){
            Home(
                homeViewModel = homeViewModel,
                onNoteClick = { noteId ->
                    navController.navigate(
                        HomeRoutes.Detail.name + "?id=$noteId"
                    ){
                        launchSingleTop = true
                    }
                },
                navToDetailPage = {
                    navController.navigate(HomeRoutes.Detail.name)
                }
            ) {
                navController.navigate(NestedRoutes.Login.name){
                    launchSingleTop = true
                    popUpTo(0){
                        inclusive = true
                    }
                }
            }
        }
        composable(
            route = HomeRoutes.Detail.name + "?id={id}",
            arguments = listOf(navArgument("id"){
                type = NavType.StringType
                defaultValue = ""
            })
        ){entry ->
            DetailScreen(
                detailViewModel = detailViewModel,
                noteId = entry.arguments?.getString("id") as String,
                locationViewModel = locationViewModel,
                context = LocalContext.current,
                locationUtils = LocationUtils(LocalContext.current),
                navController = navController
            ) {
                navController.navigateUp()
            }
        }

        dialog("locationscreen") { backstack ->
            locationViewModel.location.value?.let { it1 ->
                MapScreen(location = it1, onLocationSelected = { locationData, address ->
                    locationViewModel.fetchAddress("${locationData.latitude}, ${locationData.longitude}")
                    detailViewModel.onAddressChange(address)
                    navController.popBackStack()
                })
            }
        }
    }
}