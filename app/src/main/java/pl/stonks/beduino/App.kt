package pl.stonks.beduino

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import pl.infotower.bluetoothservice.BluetoothService
import pl.infotower.bluetoothservice.state.BluetoothStateService

private val serviceModules = module {
    single<BluetoothService> { BluetoothService.getInstance(get()) }
    single { BluetoothStateService.create(get()) }
}
private val viewModels = module {
    viewModel { MainViewModel(get(), get()) }
}
private val modules = listOf(serviceModules, viewModels)

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(modules)
        }
    }
}
