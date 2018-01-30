package gstech.com.quicktorch

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.Settings
import android.content.Intent
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.util.Log
import java.util.*
import android.R.id.button1
import android.annotation.SuppressLint
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.MenuPopupWindow
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupWindow


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG: String = "MainActivity"
    private var flashOn: Boolean = false
    private var hasFlash: Boolean = false

    private var running = false

    private var userBrightness: Int = 100
    private var camera: Camera? = null
    var params: Camera.Parameters? = null
    lateinit var timer: TimerTask
    lateinit var time: Timer
    //lateinit var thread: Thread
    val sosString: String = "0010100101"
    val flashString: String = "10101010"

    val sosBlinkDelay: Long = 1700
    val flashBlinkDelay: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hasFlash = this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        main_activity_bulb_icon.setOnClickListener(this)
        main_activity_menu_button.setOnClickListener(this)
        main_activity_flash_button.setOnClickListener(this)
        main_activity_sos_button.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (!hasFlash) {
            userBrightness = getBrightness()
        } else {
            getCamera()
            turnOnFlash()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onClick(p0: View?) {
        when (p0) {
            main_activity_bulb_icon -> torchOperation()
            main_activity_menu_button -> {showPopMenu(main_activity_menu_button)}
            main_activity_flash_button -> {
                if (running) {
                    time.cancel()
                    timer.cancel()
                    running = false
                    resetFlash()
                } else {
                    time = object : Timer(true) {}
                    showSosTorch(flashString, flashBlinkDelay)
                }
            }
            main_activity_sos_button -> {
                if (running) {
                    time.cancel()
                    timer.cancel()
                    running = false
                    resetFlash()
                } else {
                    time = object : Timer(true) {}
                    showSosTorch(sosString, sosBlinkDelay)
                }
            }
        }
    }

    // Get the camera
    private fun getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open()
                params = camera?.parameters
            } catch (e: RuntimeException) {
                Toast.makeText(applicationContext, "Camera Error. Failed to Open. Error: " + e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Camera Error. Failed to Open. Error: " + e.message)
            }
        }
    }

    private fun resetFlash() {
        if (flashOn) {
            if (hasFlash) {
                releaseCameraAndPreview()
            } else {
                turnOffFlash()
            }
            flashOn = false
        }
    }

    private fun showSosTorch(myString: String, blinkDelay: Long) {
        running = true
        timer = object : TimerTask() {
            override fun run() {
                while (running) {
                    try {
                        for (number: Char in myString.toCharArray()) {
                            if (running) {
                                if (number == '0') {
                                    runOnUiThread {
                                        if (hasFlash) {
                                            params?.flashMode = Camera.Parameters.FLASH_MODE_ON
                                        } else {
                                            turnOnFlash()
                                        }
                                    }
                                } else {
                                    runOnUiThread {
                                        if (hasFlash) {
                                            params?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                                        } else {
                                            turnOffFlash()
                                        }
                                    }
                                }
                                Thread.sleep(blinkDelay)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        time.schedule(timer, 1700)
    }

    @SuppressLint("RestrictedApi")
    private fun showPopMenu(view: View) {
        val menuBuilder = MenuBuilder(this)
        menuBuilder.add(applicationContext.getString(R.string.share)).setOnMenuItemClickListener {
            Toast.makeText(applicationContext, "Application shared!", Toast.LENGTH_SHORT).show()
            true
        }
        menuBuilder.add(applicationContext.getString(R.string.rate_us)).setOnMenuItemClickListener {
            Toast.makeText(applicationContext, "Thank you for rating us!", Toast.LENGTH_SHORT).show()
            true
        }
        val mPopup = MenuPopupHelper(this, menuBuilder, view)
        mPopup.show()
    }

    private fun releaseCameraAndPreview() {
        if (camera != null) {
            camera?.release()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun torchOperation() {
        var systemStatus = true
        if (hasFlash) {
            toggleLight()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                systemStatus = Settings.System.canWrite(applicationContext)
            }
            if (systemStatus) {
                toggleLight()
            } else {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + applicationContext.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun turnOnFlash() {
        if (hasFlash) {
            releaseCameraAndPreview()
            if (camera == null) {
                try {
                    camera = Camera.open()
                    params?.flashMode = Camera.Parameters.FLASH_MODE_ON
                    camera?.parameters = params
                    camera?.startPreview()
                } catch (e: RuntimeException) {
                    Log.e(TAG, "Camera Error. Failed to Open. Error: " + e.message)
                    return
                }
            }
        } else {
            setBrightness(255)
            activity_main_layout.setBackgroundColor(applicationContext.resources.getColor(R.color.white))
            /*Toast.makeText(applicationContext, "You clicked On : " + getBrightness(), Toast.LENGTH_SHORT).show()*/
        }
        main_activity_bulb_icon.setImageResource(R.drawable.vi_bulb_on)
        flashOn = true
    }

    private fun turnOffFlash() {
        if (hasFlash) {
            if (camera == null) {
                try {
                    camera = Camera.open()
                    params?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                    camera?.parameters = params
                    camera?.stopPreview()
                    camera?.release()
                } catch (e: RuntimeException) {
                    Log.e(TAG, "Camera Error. Failed to Open. Error: " + e.message)
                    return
                }
            }
        } else {
            setBrightness(userBrightness)
            activity_main_layout.setBackgroundColor(applicationContext.resources.getColor(R.color.colorAccent))
        }
        main_activity_bulb_icon.setImageResource(R.drawable.vi_bulb_off)
        flashOn = false
    }

    private fun toggleLight() {
        if (flashOn) {
            turnOffFlash()
        } else {
            turnOnFlash()
        }
    }

    private fun getBrightness(): Int {
        return Settings.System.getInt(
                this.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0)
    }

    private fun setBrightness(level: Int) {
        if (level in 0..255) {
            Settings.System.putInt(
                    this.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    level
            )
        }
    }

}
