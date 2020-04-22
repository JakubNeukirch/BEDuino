package pl.infotower.bluetoothservice.connecting

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import io.reactivex.rxjava3.core.Single
import java.util.*

internal class ConnectingServiceImpl(private val _bluetoothAdapter: BluetoothAdapter) :
    ConnectingService {

    companion object {
        private const val DEFAULT_UUID = "00001101-0000-1000-8000-00805f9b34fb"
    }

    override fun connect(device: android.bluetooth.BluetoothDevice): Single<Boolean> {
        return createConnectingSingle(device)
    }

    private fun createConnectingSingle(device: android.bluetooth.BluetoothDevice): Single<Boolean> {
        return Single.create { emitter ->
            _bluetoothAdapter.bondedDevices
            val socket: BluetoothSocket? =
                device.createRfcommSocketToServiceRecord(UUID.fromString(DEFAULT_UUID))
            try {
                socket?.connect()
                emitter.onSuccess(true)
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }
}