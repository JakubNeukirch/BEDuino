package pl.stonks.beduino

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import pl.infotower.bluetoothservice.BluetoothDevice
import pl.infotower.bluetoothservice.BluetoothService
import pl.infotower.bluetoothservice.communication.BluetoothMessenger
import pl.infotower.bluetoothservice.state.BluetoothStateService

class MainViewModel(
    private val _bluetoothService: BluetoothService,
    private val _bluetoothStateService: BluetoothStateService
) : ViewModel() {
    companion object {
        private const val MODE_PULSE = 0
        private const val MODE_STATIC = 1
    }

    private val _disposables = CompositeDisposable()
    private var _scanDisposable: Disposable? = null
    private var _foundDevice: BluetoothDevice? = null
    private var _messenger: BluetoothMessenger? = null

    val deviceState = MutableLiveData<DeviceState>()
    val sendingState = MutableLiveData<SendingState>()

    fun findAndConnect() {
        if (_foundDevice == null && _scanDisposable == null) {
            _scanDisposable = _bluetoothService.scanDevices(listOf("XM-15"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        Log.e("ViewModel", "Found $it")
                        connect(it)
                    },
                    onError = {
                        deviceState.value = DeviceState.DISCONNECTED
                        Log.e("ViewModel", "$it")
                    }
                )
        } else {
            connect(_foundDevice!!)
        }
    }

    fun pulseLed(red: Int, green: Int, blue: Int) {
        sendToLED(MODE_PULSE ,red, green, blue)
    }

    fun turnLed(red: Int, green: Int, blue: Int) {
        sendToLED(MODE_STATIC ,red, green, blue)
    }

    private fun sendToLED(mode: Int, red: Int, green: Int, blue: Int) {
        _messenger?.let { messenger ->
            _disposables += messenger.send(
                byteArrayOf(
                    mode.toByte(),
                    red.toByte(),
                    green.toByte(),
                    blue.toByte(),
                    '\n'.toByte()
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        sendingState.value = SendingState.SENT
                    },
                    onError = {
                        sendingState.value = SendingState.ERROR_SENDING
                    }
                )
        }
    }

    private fun connect(device: BluetoothDevice) {
        _foundDevice = device
        _messenger = _bluetoothService.createMessenger(device)
        _scanDisposable?.dispose()
        _disposables += _messenger!!.connect()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    Log.e("ViewModel", "connected")
                    deviceState.value = DeviceState.CONNECTED
                },
                onError = {
                    Log.e("ViewModel", "$it")
                    deviceState.value = DeviceState.DISCONNECTED
                }
            )
    }

    override fun onCleared() {
        _scanDisposable?.dispose()
        super.onCleared()
    }
}

enum class DeviceState {
    CONNECTED,
    DISCONNECTED
}

enum class SendingState {
    SENT, ERROR_SENDING
}