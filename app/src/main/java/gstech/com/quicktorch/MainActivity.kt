package gstech.com.quicktorch

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    var flashOn : Boolean = false
    var hasFlash : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hasFlash = this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (!hasFlash){
            //showAlert("Device Error","Sorry, your device doesn't support flash light!")
        }

        main_activity_bulb_icon.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        if (p0!!.id==main_activity_bulb_icon.id) {
            toggleLight()
        }
    }

    private fun toggleLight(){
        if (flashOn) {
            flashOn = false
            main_activity_bulb_icon.setImageResource(R.drawable.ic_bulb_off)
            Toast.makeText(applicationContext, "You clicked Off", Toast.LENGTH_SHORT).show()
        }else{
            flashOn = true
            main_activity_bulb_icon.setImageResource(R.drawable.ic_light_bulb_on)
            Toast.makeText(applicationContext, "You clicked On", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showAlert(title : String, description : String){
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(description)

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", {
            dialogInterface, i ->

        })

        alertDialog.show()
    }
}
