package su.femboymatrix.buttplug.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import su.femboymatrix.buttplug.data.ChatHistoryEntity
import su.femboymatrix.buttplug.data.FemboyNetworkRepository
import su.femboymatrix.buttplug.data.FemboyOfflineRepository
import su.femboymatrix.buttplug.utils.TIMEOUT_MILLIS
import javax.inject.Inject

data class ChatUiState(
    val text: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val femboyNetworkRepository: FemboyNetworkRepository,
    private val femboyOfflineRepository: FemboyOfflineRepository
) : ViewModel() {
    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState = _chatUiState.asStateFlow()

    val chatHistory: Flow<List<ChatHistoryEntity>> =
        femboyOfflineRepository.getAllHistory().stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
        )

    fun sendMessage(
        currentName: String,
        message: String
    ) {
        if (message.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    femboyOfflineRepository.addToHistory(
                        if (message.startsWith('/')) {
                            ChatHistoryEntity(
                                name = message.drop(1),
                                message = femboyNetworkRepository.sendMessage(
                                    message.drop(1)
                                ),
                                isCommand = true
                            )
                        } else {
                            ChatHistoryEntity(
                                name = currentName,
                                message = message
                            )
                        }
                    )
                } catch (e: IOException) {
                    femboyOfflineRepository.addToHistory(
                        ChatHistoryEntity(
                            message = "Error connecting to the server:\n$e",
                            isCommand = true
                        )
                    )
                } catch (e: HttpException) {
                    femboyOfflineRepository.addToHistory(
                        ChatHistoryEntity(
                            message = "Error connecting to the server:\n$e",
                            isCommand = true
                        )
                    )
                }
                _chatUiState.update {
                    it.copy(
                        text = ""
                    )
                }
            }
        }
    }

    fun changeChatText(text: String) {
        _chatUiState.update {
            it.copy(
                text = text
            )
        }
    }
}
