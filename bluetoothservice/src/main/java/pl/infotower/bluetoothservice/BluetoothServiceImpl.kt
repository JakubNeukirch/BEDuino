package pl.infotower.bluetoothservice

import android.bluetooth.BluetoothAdapter
import android.content.Context
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import pl.infotower.bluetoothservice.communication.BluetoothMessenger
import pl.infotower.bluetoothservice.communication.BluetoothMessengerImpl
import pl.infotower.bluetoothservice.connecting.ConnectingService
import pl.infotower.bluetoothservice.connecting.ConnectingServiceImpl
import pl.infotower.bluetoothservice.discovering.DiscoveringService
import pl.infotower.bluetoothservice.discovering.BLEDiscoveringService
import pl.infotower.bluetoothservice.discovering.StandardDiscoveringService
import pl.infotower.bluetoothservice.pairing.PairingService
import pl.infotower.bluetoothservice.pairing.PairingServiceImpl

class BluetoothServiceImpl(private val _context: Context) : BluetoothService {
    private val _bluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val _discoveringService: DiscoveringService by lazy {
        StandardDiscoveringService(
            _context,
            _bluetoothAdapter
        )
    }
    private val _connectingService: ConnectingService by lazy {
        ConnectingServiceImpl(
            _bluetoothAdapter
        )
    }
    private val _pairingService: PairingService by lazy {
        PairingServiceImpl(
            _context
        )
    }

    override fun scanDevices(filters: List<String>): Observable<BluetoothDevice> {
        return _discoveringService.listenDevices(filters)
            .map { it.toModel() }
    }

    override fun connect(device: BluetoothDevice): Single<Boolean> {
        val androidDevice = getAndroidBluetoothDevice(device)!!
        return _connectingService.connect(androidDevice)
    }

    override fun pair(device: BluetoothDevice): Completable {
        return _pairingService.pair(getAndroidBluetoothDevice(device)!!)
    }

    override fun createMessenger(device: BluetoothDevice): BluetoothMessenger {
        return BluetoothMessengerImpl(_context, getAndroidBluetoothDevice(device)!!)
    }

    private fun getAndroidBluetoothDevice(device: BluetoothDevice): android.bluetooth.BluetoothDevice? {
        return _discoveringService.discoveredDevices.find { it.address == device.mac }
            ?: _bluetoothAdapter.bondedDevices.find { it.address == device.mac }
    }

    private fun android.bluetooth.BluetoothDevice.toModel(): BluetoothDevice {
        return BluetoothDevice(
            this.name ?: "NONAME",
            this.address,
            this.uuids?.first()?.uuid,
            _bluetoothAdapter.bondedDevices.contains(this)
        )
    }

}