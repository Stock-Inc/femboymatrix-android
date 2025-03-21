package dev.stock.dysnomia.ui.screen.chat

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stock.dysnomia.data.ChatHistoryEntity
import dev.stock.dysnomia.data.NetworkRepository
import dev.stock.dysnomia.data.OfflineRepository
import dev.stock.dysnomia.utils.TIMEOUT_MILLIS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

data class ChatUiState(
    val messageText: TextFieldValue = TextFieldValue()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val offlineRepository: OfflineRepository
) : ViewModel() {
    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState = _chatUiState.asStateFlow()

    val chatHistory: Flow<List<ChatHistoryEntity>> =
        offlineRepository.getAllHistory().stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
        )

    fun sendMessage(
        currentName: String,
        message: String
    ) {
        if (message.isNotEmpty() && message != "/") {
            viewModelScope.launch {
                try {
                    offlineRepository.addToHistory(
                        if (message.startsWith('/')) {
                            ChatHistoryEntity(
                                name = message.drop(1),
                                message = networkRepository.sendMessage(
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
                    offlineRepository.addToHistory(
                        ChatHistoryEntity(
                            message = "Error connecting to the server:\n$e",
                            isCommand = true
                        )
                    )
                } catch (e: HttpException) {
                    offlineRepository.addToHistory(
                        ChatHistoryEntity(
                            message = "Error connecting to the server:\n$e",
                            isCommand = true
                        )
                    )
                }
                _chatUiState.update {
                    it.copy(
                        messageText = TextFieldValue()
                    )
                }
            }
        }
    }

    fun changeChatText(messageText: TextFieldValue) {
        _chatUiState.update {
            it.copy(
                messageText = messageText
            )
        }
    }
}
