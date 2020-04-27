package pl.stonks.beduino

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

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
        staticButton.setOnClickListener {
            val color = Color.valueOf(colorPicker.selectedColor)
            _viewModel.turnLed(
                (color.red() * 255).roundToInt(),
                (color.green() * 255).roundToInt(),
                (color.blue() * 255).roundToInt()
            )
        }
        pulseButton.setOnClickListener {
            val color = Color.valueOf(colorPicker.selectedColor)
            _viewModel.pulseLed(
                (color.red() * 255).roundToInt(),
                (color.green() * 255).roundToInt(),
                (color.blue() * 255).roundToInt()
            )
        }
        lightnessSlider.setColorPicker(colorPicker)
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
