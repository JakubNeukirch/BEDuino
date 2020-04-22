package pl.infotower.bluetoothservice.communication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import io.reactivex.rxjava3.core.Completable
import java.util.*

interface BluetoothMessenger {
    fun connect() : Completable
    fun send(message: String): Completable
    fun disconnected(): Completable
}

class BluetoothMessengerImpl(private val _context: Context, private val _device: BluetoothDevice) : BluetoothMessenger {

    private var _socket: BluetoothSocket? = null

    override fun connect(): Completable {
        return Completable.create {
            val uuid = _device.uuids?.firstOrNull()?.uuid ?: UUID.fromString("00001106-0000-1000-8000-00805F9B34FB")
            _socket = _device.createRfcommSocketToServiceRecord(uuid)
            _socket!!.connect()
        }
    }

    override fun send(message: String): Completable {
        return Completable.create {
            if(_socket == null) {
                it.onError(IllegalStateException("Socket not connected"))
            }
            _socket?.outputStream?.write(message.toByteArray())
            it.onComplete()
        }
    }

    override fun disconnected(): Completable {
        return Completable.fromAction {
            _socket?.close()
        }
    }
}