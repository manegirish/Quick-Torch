/**
 * @author Girish Mane(girishmane8692@gmail.com)
 *         Created on 27-01-2018
 *         Last Modified on 01-02-2018
 */

package gstech.com.quicktorch

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.Settings
import android.content.Intent
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import java.util.*
import android.annotation.SuppressLint
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper

class MainActivity : RuntimePermissionActivity(), View.OnClickListener {

    //private val TAG: String = "MainActivity"
    private var flashOn: Boolean = false
    private var hasFlash: Boolean = false

    private var running = false

    private var userBrightness: Int = 100
    private lateinit var camera: Camera

    private lateinit var timer: TimerTask
    lateinit var time: Timer

    private val sosString: String = "0010100101"
    private val flashString: String = "010101010"

    private val sosBlinkDelay: Long = 1700
    private val flashBlinkDelay: Long = 500

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
            super@MainActivity.requestAppPermissions(arrayOf(android.Manifest.permission.CAMERA),
                    R.string.runtime_permissions_txt, 1)
        }
    }

    override fun onPause() {
        super.onPause()
        resetFlash()
    }

    override fun onPermissionsGranted(requestCode: Int) {
    }

    override fun onClick(p0: View?) {
        when (p0) {
            main_activity_bulb_icon -> {
                if (running) {
                    time.cancel()
                    timer.cancel()
                    running = false
                    resetFlash()
                }
                torchOperation()
            }

            main_activity_menu_button -> showPopMenu(main_activity_menu_button)

            main_activity_flash_button -> {
                if (running) {
                    time.cancel()
                    timer.cancel()
                    running = false
                    resetFlash()
                } else {
                    resetFlash()
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
                    resetFlash()
                    time = object : Timer(true) {}
                    showSosTorch(sosString, sosBlinkDelay)
                }
            }
        }
    }

    private fun resetFlash() {
        if (flashOn) {
            turnOffFlash()
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
                                        turnOnFlash()
                                    }
                                } else {
                                    runOnUiThread {
                                        turnOffFlash()
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
            resetFlash()
            Toast.makeText(applicationContext, "Application shared!", Toast.LENGTH_SHORT).show()
            true
        }
        menuBuilder.add(applicationContext.getString(R.string.rate_us)).setOnMenuItemClickListener {
            resetFlash()
            Toast.makeText(applicationContext, "Thank you for rating us!", Toast.LENGTH_SHORT).show()
            true
        }
        val mPopup = MenuPopupHelper(this, menuBuilder, view)
        mPopup.show()
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
            try {
                camera = Camera.open()
                val parameters = camera.parameters
                parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                camera.parameters = parameters
                camera.startPreview()
            } catch (e: RuntimeException) {
                Toast.makeText(applicationContext, "Error Accessing Flash Check Permissions", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            setBrightness(255)
            activity_main_layout.setBackgroundColor(applicationContext.resources.getColor(R.color.white))
        }
        main_activity_bulb_icon.setImageResource(R.drawable.vi_bulb_on)
        flashOn = true
    }

    private fun turnOffFlash() {
        if (hasFlash) {
            try {
                val parameters = camera.parameters
                parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera.stopPreview()
                camera.release()
            } catch (e: RuntimeException) {
                Toast.makeText(applicationContext, "Error Accessing Flash Check Permissions", Toast.LENGTH_SHORT).show()
                return
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

    @TargetApi(Build.VERSION_CODES.M)
    private fun getBrightness(): Int {
        var systemStatus = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemStatus = Settings.System.canWrite(applicationContext)
        }
        if (!systemStatus) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + applicationContext.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            return Settings.System.getInt(
                    this.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    0)
        }
        return 0
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun setBrightness(level: Int) {
        var systemStatus = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemStatus = Settings.System.canWrite(applicationContext)
        }
        if (!systemStatus) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + applicationContext.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            if (level in 0..255) {
                Settings.System.putInt(
                        this.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        level
                )
            }
        }
    }
}
