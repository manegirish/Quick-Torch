/**
 * @author Girish Mane(girishmane8692@gmail.com)
 *         Created on 01-02-2018
 *         Last Modified on 01-02-2018
 */

package gstech.com.quicktorch

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.SparseIntArray
import android.view.View
import android.provider.Settings

abstract class RuntimePermissionActivity : AppCompatActivity() {

    private var mErrorString: SparseIntArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mErrorString = SparseIntArray()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionCheck = PackageManager.PERMISSION_GRANTED + grantResults.sum()
        if (grantResults.isNotEmpty() && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(requestCode)
        } else {
            Snackbar.make(findViewById<View>(android.R.id.content), mErrorString!!.get(requestCode),
                    Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                    {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:" + packageName)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        startActivity(intent)
                    }).show()
        }
    }

    fun requestAppPermissions(requestedPermissions: Array<String>,
                              stringId: Int, requestCode: Int) {
        mErrorString!!.put(requestCode, stringId)
        var permissionCheck = PackageManager.PERMISSION_GRANTED
        var shouldShowRequestPermissionRationale = false
        for (permission in requestedPermissions) {
            permissionCheck += ContextCompat.checkSelfPermission(this, permission)
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
                Snackbar.make(findViewById<View>(android.R.id.content), stringId,
                        Snackbar.LENGTH_INDEFINITE).setAction("GRANT",
                        { ActivityCompat.requestPermissions(this, requestedPermissions, requestCode) }).show()
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode)
            }
        } else {
            onPermissionsGranted(requestCode)
        }
    }

    abstract fun onPermissionsGranted(requestCode: Int)
}