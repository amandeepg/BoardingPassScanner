package ca.amandeep.bcbpscanner

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ncredinburgh.iata.model.IataCode
import kotlinx.coroutines.flow.MutableStateFlow
import splitties.experimental.ExperimentalSplittiesApi
import splitties.permissions.hasPermission

class ScanningViewModel(application: Application) : AndroidViewModel(application) {

    val flashOnFlow = MutableStateFlow(false)

    var flashOn: Boolean
        get() = flashOnFlow.value
        set(value) {
            flashOnFlow.value = value
        }

    val stateFlow = MutableStateFlow<State>(State.Stopped)

    @OptIn(ExperimentalSplittiesApi::class)
    var state: State
        get() = stateFlow.value
        set(value) {
            stateFlow.value =
                if (hasPermission(Manifest.permission.CAMERA)) {
                    value
                } else {
                    value.asStopped()
                }
        }

    sealed interface State {
        object Stopped : State
        object Detecting : State
        open class Detected(val code: IataCode) : State
        class DetectedAndStopped(code: IataCode) : Detected(code), State
        object Searching : State

        val isStopped: Boolean
            get() = this is Stopped || this is DetectedAndStopped

        fun asStopped() = when (this) {
            is Detected -> DetectedAndStopped(code)
            else -> Stopped
        }

        fun asDetecting() = when (this) {
            is DetectedAndStopped -> Detected(code)
            else -> Detecting
        }
    }

    fun setStateStopped() {
        state = state.asStopped()
    }

    fun setStateDetecting() {
        state = state.asDetecting()
    }

    fun setDetected(barcode: IataCode) {
        state = when (state) {
            is State.DetectedAndStopped, State.Stopped -> State.DetectedAndStopped(barcode)
            else -> State.Detected(barcode)
        }
    }
}
