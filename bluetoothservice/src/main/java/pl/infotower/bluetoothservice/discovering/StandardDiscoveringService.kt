package pl.infotower.bluetoothservice.discovering

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

internal class StandardDiscoveringService(
    private val _context: Context,
    private val _bluetoothAdapter: BluetoothAdapter
) : DiscoveringService {
    private var _bluetoothDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    override val discoveredDevices: Set<BluetoothDevice>
        get() = _bluetoothDevices
    private var _receiver: BroadcastReceiver? = null

    override fun listenDevices(filters: List<String>): Observable<BluetoothDevice> {
        return createBluetoothDeviceListener()
            .filter { btDevice ->
                Log.i("ViewModel", "Discovered ${btDevice.name} ${btDevice.address}")
                filters.any { (btDevice.name ?: "").contains(it) || btDevice.address ==it } }
            .doOnNext {
                Log.i("ViewModel", "Found $it")
            }
    }

    private fun createBluetoothDeviceListener(): Observable<BluetoothDevice> {
        val observable = Observable.create<BluetoothDevice> {
            _bluetoothDevices = mutableSetOf()
            _receiver = ScanCallback(it)
            val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            _context.registerReceiver(_receiver, intentFilter)
            _bluetoothAdapter.startDiscovery()
        }
        observable.doOnDispose {
            _bluetoothAdapter.cancelDiscovery()
        }
        return observable
    }

    override fun getBondedDevices(): List<BluetoothDevice> {
        return _bluetoothAdapter.bondedDevices.toList()
    }

    private inner class ScanCallback(private val _emitter: ObservableEmitter<BluetoothDevice>) :
        BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == BluetoothDevice.ACTION_FOUND) {
                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    ?.let { btDevice ->
                    _bluetoothDevices.add(btDevice)
                    _emitter.onNext(btDevice)
                }
            }
        }
    }
}