package pl.infotower.bluetoothservice.connecting

import io.reactivex.rxjava3.core.Single

internal interface ConnectingService {
    fun connect(device: android.bluetooth.BluetoothDevice): Single<Boolean>
}