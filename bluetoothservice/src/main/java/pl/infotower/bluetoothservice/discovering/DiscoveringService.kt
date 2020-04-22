package pl.infotower.bluetoothservice.discovering

import android.bluetooth.BluetoothDevice
import io.reactivex.rxjava3.core.Observable

internal interface DiscoveringService {
    val discoveredDevices: Set<BluetoothDevice>
    fun listenDevices(filters: List<String>): Observable<BluetoothDevice>
    fun getBondedDevices(): List<BluetoothDevice>
}