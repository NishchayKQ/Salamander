package nish.wry.salamander.data

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**a [MutableStateFlow] with added benefit of saving and restoring state to [SavedStateHandle]
 * @param savedStateHandle the [SavedStateHandle] to save the updates in
 * @param key a unique String to save and retrieve from [savedStateHandle]
 * @param defaultValue value to use if [key] is not found in the [savedStateHandle]
 * **/
class MutableSaveStateFlow<T>(
    val savedStateHandle: SavedStateHandle,
    val key: String,
    defaultValue: T,
) {
    init {
        require(key.isNotBlank()) { "key=$key; must not be blank" }
    }

    /**should not be directly accessed, use [update] to change its value and [asStateFlow] to access it**/
    @Suppress("PropertyName")
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val _state: MutableStateFlow<T> = MutableStateFlow(
        savedStateHandle[key] ?: defaultValue
    )

    /** uses [MutableStateFlow]'s [MutableStateFlow.update] method along with saving updated state to [SavedStateHandle] **/
    inline fun update(function: (T) -> T) {
        _state.update(function)
        savedStateHandle[key] = _state.value
    }

    /**same result as [MutableStateFlow.asStateFlow]**/
    fun asStateFlow(): StateFlow<T> = _state.asStateFlow()

}