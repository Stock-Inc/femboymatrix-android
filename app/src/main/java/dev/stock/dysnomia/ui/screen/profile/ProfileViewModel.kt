package dev.stock.dysnomia.ui.screen.profile

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stock.dysnomia.data.PreferencesRepository
import dev.stock.dysnomia.utils.TIMEOUT_MILLIS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val name: TextFieldValue = TextFieldValue(),
    val password: TextFieldValue = TextFieldValue()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    val currentName = userPreferencesRepository.name.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ""
    )

    fun changeName(name: TextFieldValue) {
        _uiState.update {
            it.copy(
                name = name
            )
        }
    }

    fun changePassword(password: TextFieldValue) {
        _uiState.update {
            it.copy(
                password = password
            )
        }
    }

    fun login(name: String) {
        if (name.trim() != "") {
            viewModelScope.launch {
                userPreferencesRepository.saveName(name.trim())
                _uiState.update {
                    it.copy(
                        password = TextFieldValue()
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearName()
        }
    }
}
