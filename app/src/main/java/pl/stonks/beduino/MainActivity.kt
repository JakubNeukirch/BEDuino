package pl.stonks.beduino

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val _viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeDeviceState()
        observeSendingState()
        connectButton.setOnClickListener {
            _viewModel.findAndConnect()
        }
        ledSwitch.setOnClickListener {
            _viewModel.turnLed()
        }
    }

    private fun observeSendingState() {
        _viewModel.sendingState.observe(this, Observer {
            showMessage("$it")
        })
    }

    private fun observeDeviceState() {
        _viewModel.deviceState.observe(this, Observer {
            showMessage("$it")
        })
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }
}
