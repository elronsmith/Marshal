package ru.elron.examplemarshal.ui.hard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_hard.*
import ru.elron.examplemarshal.R
import ru.elron.examplemarshal.utils.Marshal

/**
 * Проверяет разрешение во время нажатия на кнопку
 * - если разрешение есть:
 *   - если включено GPS, то будет зеленая иконка
 *   - если выключено GPS, то будет красная иконка
 * - если разрешения нет:
 *   - если выключено GPS, то будет красная иконка
 *   - если включено GPS, то запрашиваем разрешение у пользователя
 */
class HardActivity : AppCompatActivity(R.layout.activity_hard), Marshal.OnPermissionListener {
    val storageMarshal: Marshal by lazy {
        Marshal.Builder(this, this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .build()
    }
    val gpsMarshal: Marshal by lazy {
        val marshal = Marshal.Builder(this, this)
            .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
            .build()
        marshal.permissionReqiestCode += 10
        marshal.rationalePermissionReqiestCode += 10
        marshal.changePermissionReqiestCode += 10
        marshal
    }

    var hasGpsPermission = false
    var gpsEnabled: Boolean = false
        get() = getPreferences(Context.MODE_PRIVATE).getBoolean(ARG_GPS_EMABLED, false)
        set(value) {
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(ARG_GPS_EMABLED, value).apply()
            field = value
        }

    companion object {
        const val ARG_GPS_EMABLED   = "GPS_EMABLED"

        fun start(activity: FragmentActivity) {
            activity.startActivity((Intent(activity, HardActivity::class.java)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateGps()
    }

    override fun onStart() {
        super.onStart()
        storageMarshal.checkAndRequestPermissions()
        if (gpsEnabled) {
            invalidateOptionsMenu()
            if (!hasGpsPermission)
                gpsMarshal.checkAndRequestPermissions()
        }


    }

    private fun updateFileList() {
        val root = Environment.getExternalStorageDirectory()
        val array = root.list()

        when {
            array == null -> textView.text = "Ошибка"
            array.isEmpty() -> textView.text = "Пусто"
            else -> {
                val stringBuilder = StringBuilder()

                for (file in array)
                    stringBuilder.append(file).append("\n")

                textView.text = stringBuilder.toString()
            }
        }
    }

    fun updateGps() {
        hasGpsPermission = gpsMarshal.hasPermission()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.string.permission_gps) {
            gpsEnabled = !gpsEnabled
            invalidateOptionsMenu()
            if (gpsEnabled) {
                if (hasGpsPermission) {
                    Toast.makeText(this, "GPS включен", Toast.LENGTH_SHORT).show()
                } else {
                    gpsMarshal.checkAndRequestPermissions()
                }
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        var gpsItem: MenuItem? = menu!!.findItem(R.string.permission_gps)
        if (gpsItem == null) {
            gpsItem = menu.add(0, R.string.permission_gps, 0, "GPS")
            gpsItem!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val drawableRes = when {
            !gpsEnabled -> R.drawable.vd_my_location_off
            hasGpsPermission -> R.drawable.vd_my_location_on
            else -> R.drawable.vd_my_location_on_off
        }

        gpsItem.icon = ContextCompat.getDrawable(this, drawableRes)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        storageMarshal.onRequestPermissionsResult(requestCode, permissions, grantResults)
        gpsMarshal.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionGranted(marshal: Marshal) {
        when(marshal) {
            storageMarshal -> updateFileList()
            gpsMarshal -> {
                hasGpsPermission = true
                invalidateOptionsMenu()
                Toast.makeText(this, "GPS включен!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPermissionDenied(marshal: Marshal, needRationale: Boolean) {
        when(marshal) {
            storageMarshal -> {
                onShowDialogRationale(marshal, marshal.permissions[0])
            }
            gpsMarshal -> {
                onShowDialogRationale(marshal, marshal.permissions[0])
            }
        }
    }

    override fun onShowDialogRationale(marshal: Marshal, permission: String) {
        val builder = AlertDialog.Builder(this)

        when(marshal) {
            storageMarshal -> {
                builder.setTitle(R.string.permission_title)
                    .setMessage(R.string.permission_read_external_storage)
                    .setPositiveButton(R.string.permission_button_allow) { _, _ ->
                        marshal.requestPermissions(true)
                    }
                    .setNegativeButton(R.string.permission_button_close) { _, _ ->
                        Toast.makeText(this, "Cancelled SDCARD", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton(R.string.permission_button_settings) { _, _ ->
                        marshal.openApplicationSettings()
                    }
                    .setCancelable(false)
            }
            gpsMarshal -> {
                builder.setTitle(R.string.permission_title)
                    .setMessage(R.string.permission_gps)
                    .setPositiveButton(R.string.permission_button_allow) { _, _ ->
                        marshal.requestPermissions(true)
                    }
                    .setNegativeButton(R.string.permission_button_close) { _, _ ->
                        Toast.makeText(this, "Cancelled GPS", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton(R.string.permission_button_settings) { _, _ ->
                        marshal.openApplicationSettings()
                    }
                    .setCancelable(false)
            }
        }

        builder.create().show()
    }
}
