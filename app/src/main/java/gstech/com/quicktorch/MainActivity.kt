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
import android.graphics.Color
import android.net.Uri
import android.os.Build


class MainActivity : AppCompatActivity(),View.OnClickListener {

    private val TAG : String = "MainActivity"
    private var flashOn : Boolean = false
    private var hasFlash : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hasFlash = this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (!hasFlash){
            //showAlert("Device Error","Sorry, your device doesn't support flash light!")
        }

        main_activity_bulb_icon.setOnClickListener(this)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onClick(p0: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(applicationContext)) {
               toggleLight()
            } else {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + applicationContext.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun toggleLight(){
        if (flashOn) {
            flashOn = false
            setBrightness(100)
            main_activity_bulb_icon.setImageResource(R.drawable.vi_bulb_off)
            activity_main_layout.setBackgroundColor(applicationContext.resources.getColor(R.color.colorAccent))
            Toast.makeText(applicationContext, "You clicked Off "+getBrightness(), Toast.LENGTH_SHORT).show()
        }else{
            flashOn = true
            setBrightness(255)
            main_activity_bulb_icon.setImageResource(R.drawable.vi_bulb_on)
            activity_main_layout.setBackgroundColor(applicationContext.resources.getColor(R.color.white))
            Toast.makeText(applicationContext, "You clicked On : "+getBrightness(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBrightness():Int{
        return Settings.System.getInt(
                this.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0)
    }

    private fun setBrightness(level : Int){
        if(level in 0..255){
                Settings.System.putInt(
                        this.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        level
            )
        }
    }

}
