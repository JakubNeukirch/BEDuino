package pl.infotower.bluetoothservice.pairing

import android.bluetooth.BluetoothDevice
import io.reactivex.rxjava3.core.Completable

internal interface PairingService {
    fun pair(device: BluetoothDevice): Completable
}