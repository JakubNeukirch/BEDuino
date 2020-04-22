package pl.infotower.bluetoothservice.state

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import io.reactivex.rxjava3.core.Observable

interface BluetoothStateService {
    var isOn: Boolean
    fun observeBluetoothState(): Observable<Boolean>

    companion object {
        fun create(context: Context): BluetoothStateService = BluetoothStateServiceImpl(context)
    }
}

private class BluetoothStateServiceImpl(private val _context: Context) : BluetoothStateService {
    private val _bluetoothAdapter: BluetoothAdapter? by lazy { (_context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter }
    override var isOn: Boolean
        get() = _bluetoothAdapter?.isEnabled ?: false
        set(value) {
            if (value)
                _bluetoothAdapter?.enable()
            else
                _bluetoothAdapter?.disable()
        }

    override fun observeBluetoothState(): Observable<Boolean> {
        var receiver: BluetoothReceiver? = null
        val observable = Observable.create<Boolean> { emitter ->
            receiver = BluetoothReceiver {
                emitter.onNext(it)
            }
            val filters = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            _context.registerReceiver(receiver, filters)
        }
        observable.doFinally {
            receiver?.let { _context.unregisterReceiver(it) }
        }
        return observable
    }

    private class BluetoothReceiver(private val onStateChanged: (Boolean) -> Unit) :
        BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                Log.i("Bluetooth", "State $state")
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        onStateChanged(false)
                    }
                    BluetoothAdapter.STATE_ON -> {
                        onStateChanged(true)
                    }
                }
            }
        }
    }
}