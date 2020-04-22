package pl.infotower.bluetoothservice

import android.content.Context
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import pl.infotower.bluetoothservice.communication.BluetoothMessenger
import java.util.*

/**
 * Service responsible for discovery of new bluetooth devices and managing them
 */
interface BluetoothService {
    /**
     * @param filters - listen devices which names contains any of filters
     */
    fun scanDevices(filters: List<String>): Observable<BluetoothDevice>

    fun pair(device: BluetoothDevice): Completable

    fun connect(device: BluetoothDevice): Single<Boolean>

    fun createMessenger(device: BluetoothDevice): BluetoothMessenger

    companion object {
        fun getInstance(context: Context): BluetoothService = BluetoothServiceImpl(context)
    }
}

data class BluetoothDevice(
    val name: String,
    val mac: String,
    val uuid: UUID?,
    val isBonded: Boolean
)