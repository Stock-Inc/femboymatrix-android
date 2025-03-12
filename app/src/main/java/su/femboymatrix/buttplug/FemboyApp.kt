package su.femboymatrix.buttplug

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import su.femboymatrix.buttplug.ui.composables.FemboyBottomNavigationBar
import su.femboymatrix.buttplug.ui.composables.navigationItemContentList
import su.femboymatrix.buttplug.ui.screen.chat.ChatScreen
import su.femboymatrix.buttplug.ui.screen.chat.ChatViewModel
import su.femboymatrix.buttplug.ui.screen.home.HomeScreen
import su.femboymatrix.buttplug.ui.screen.home.HomeViewModel
import su.femboymatrix.buttplug.ui.screen.profile.LoginScreen
import su.femboymatrix.buttplug.ui.screen.profile.ProfileScreen
import su.femboymatrix.buttplug.ui.screen.profile.ProfileViewModel
import su.femboymatrix.buttplug.ui.theme.FemboyMatrixTheme

enum class FemboyApp {
    Home, Login, Chat
}

@Composable
fun FemboyApp(
    chatViewModel: ChatViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = FemboyApp.valueOf(
        backStackEntry?.destination?.route ?: FemboyApp.Home.name
    )

    val chatUiState = chatViewModel.chatUiState.collectAsState().value
    val chatHistory = chatViewModel.chatHistory.collectAsState(emptyList()).value

    val loginUiState = profileViewModel.uiState.collectAsState().value
    val currentName = profileViewModel.currentName.collectAsState().value

    val homeUiState = homeViewModel.uiState.collectAsState().value

    Scaffold(
        bottomBar = {
            FemboyBottomNavigationBar(
                currentScreen = currentScreen,
                onClick = { navController.navigate(it.name) },
                navigationItemContentList = navigationItemContentList,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = FemboyApp.Home.name,
        ) {
            composable(route = FemboyApp.Home.name) {
                HomeScreen(
                    uiState = homeUiState,
                    modifier = Modifier.padding(contentPadding)
                )
            }

            composable(route = FemboyApp.Chat.name) {
                ChatScreen(
                    uiState = chatUiState,
                    chatHistory = chatHistory,
                    onTextChange = chatViewModel::changeChatText,
                    onSendCommand = chatViewModel::sendMessage,
                    modifier = Modifier.padding(contentPadding)
                )
            }

            composable(route = FemboyApp.Login.name) {
                if (currentName == "") {
                    LoginScreen(
                        uiState = loginUiState,
                        onNameChange = profileViewModel::changeName,
                        onPasswordChange = profileViewModel::changePassword,
                        onLoginClick = profileViewModel::login,
                        onRegisterClick = profileViewModel::login,
                        modifier = Modifier.padding(contentPadding)
                    )
                } else {
                    ProfileScreen(
                        name = currentName,
                        onLogoutClick = profileViewModel::logout,
                        modifier = Modifier.padding(contentPadding)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun FemboyAppPreview() {
    FemboyMatrixTheme(darkTheme = true) {
        FemboyApp()
    }
}
