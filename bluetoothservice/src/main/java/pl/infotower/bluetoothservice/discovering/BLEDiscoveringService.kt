package pl.infotower.bluetoothservice.discovering

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

internal class BLEDiscoveringService(
    private val _context: Context,
    private val _bluetoothAdapter: BluetoothAdapter
) : DiscoveringService {
    private var _bluetoothDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    private val _bluetoothLeScanner: BluetoothLeScanner = _bluetoothAdapter.bluetoothLeScanner
    override val discoveredDevices: Set<BluetoothDevice>
        get() = _bluetoothDevices
    private var _leScanCallback: LeScanCallback? = null

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
            _leScanCallback = LeScanCallback(it)
            _bluetoothLeScanner.startScan(_leScanCallback)
        }
        observable.doOnDispose { _bluetoothLeScanner.stopScan(_leScanCallback) }
        return observable
    }

    override fun getBondedDevices(): List<BluetoothDevice> {
        return _bluetoothAdapter.bondedDevices.toList()
    }

    private inner class LeScanCallback(private val _emitter: ObservableEmitter<BluetoothDevice>) :
        ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _emitter.onError(Exception("LE Scan failed"))
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let { btDevice ->
                _bluetoothDevices.add(btDevice)
                _emitter.onNext(btDevice)
            }
        }
    }
}