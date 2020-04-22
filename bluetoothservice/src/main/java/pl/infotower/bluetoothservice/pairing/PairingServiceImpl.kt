package pl.infotower.bluetoothservice.pairing

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.rxjava3.core.Completable

@Suppress("ThrowableNotThrown")
internal class PairingServiceImpl(private val _context: Context) :
    PairingService {

    override fun pair(device: BluetoothDevice): Completable {
        var pairingReceiver: PairingReceiver? = null
        val completable = Completable.create { emitter ->
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            }
            pairingReceiver = PairingReceiver {
                if (it) {
                    emitter.onComplete()
                } else {
                    emitter.onError(DeviceNotBondException(device.name))
                }
            }
            _context.registerReceiver(pairingReceiver, filter)
            try {
                device.createBond()
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
        completable.doOnDispose {
            if (pairingReceiver != null) {
                _context.unregisterReceiver(pairingReceiver)
            }
        }
        return completable
    }

    private inner class PairingReceiver(private val onBond: (isBond: Boolean) -> Unit) :
        BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    onBond(true)
                } else if (device.bondState == BluetoothDevice.BOND_NONE) {
                    onBond(false)
                }
            }
        }
    }
}

class DeviceNotBondException(device: String) : Exception() {
    override val message: String? = "$device not connected"
}