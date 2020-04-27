package pl.infotower.bluetoothservice.communication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

interface BluetoothMessenger {
    fun connect() : Completable
    fun send(message: ByteArray): Completable
    fun disconnected(): Completable
}

class BluetoothMessengerImpl(private val _context: Context, private val _device: BluetoothDevice) : BluetoothMessenger {

    private var _socket: BluetoothSocket? = null


    override fun connect(): Completable {
        return Completable.create {
            val uuid = _device.uuids?.firstOrNull()?.uuid ?: UUID.fromString("00001106-0000-1000-8000-00805F9B34FB")
            _socket = _device.createRfcommSocketToServiceRecord(uuid)
            _socket!!.connect()
            it.onComplete()
        }.doFinally {
            listenInput()
        }
    }
    private fun listenInput() {
        val reader = _socket?.inputStream?.bufferedReader()
        Observable.create<String> {
            reader?.let { reader ->
                while (true) {
                    it.onNext(reader.readLine())
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { Log.i("Messenger", it) }
            .doOnError { Log.i("Messenger", "$it") }
            .doFinally { reader?.close() }
            .subscribe(
                {},{}
            )

    }

    override fun send(message: ByteArray): Completable {
        return Completable.create {
            if(_socket == null) {
                it.onError(IllegalStateException("Socket not connected"))
            }
            _socket?.outputStream?.write(message)
            it.onComplete()
        }
    }

    override fun disconnected(): Completable {
        return Completable.fromAction {
            _socket?.close()
        }
    }
}