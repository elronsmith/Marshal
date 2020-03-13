package ru.elron.examplemarshal.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Проверяет есть ли разрешения у приложения
 */
class Marshal private constructor(val activity: FragmentActivity,
                                  val listener: OnPermissionListener) : PreferenceManager.OnActivityResultListener {
    var permissionReqiestCode: Int = REQUEST_PERMISSION
    var rationalePermissionReqiestCode: Int = REQUEST_PERMISSION_RATIONALE
    var changePermissionReqiestCode: Int = REQUEST_PERMISSION_CHANGED

    lateinit var permissions: Array<out String>
    lateinit var results: Array<Boolean>

    companion object {
        /** запрос без объяснения зачем нужен доступ */
        val REQUEST_PERMISSION              = 1111
        /** запрос с объяснением зачем нужен доступ */
        val REQUEST_PERMISSION_RATIONALE    = 1112
        /** отправляет пользователя изменить настройки доступа этого приложения */
        val REQUEST_PERMISSION_CHANGED      = 1113

        fun hasPermissions(context: Context, permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        fun hasPermissions(context: Context, permissionsArray: Array<out String>): Boolean {
            val result = true
            for (p in permissionsArray)
                if (!hasPermissions(context, p))
                    return false
            return result
        }
    }

    /**
     * Вызывается когда пользователь вручную поменял настройки
     */
    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?): Boolean {
        if (requestCode == changePermissionReqiestCode) {
            checkPermissions()
            return true
        }

        return false
    }

    /**
     * Вызывается после того когда пользователь предоставил разрешения(или нет)
     */
    fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<out String>,
                                   grantResults: IntArray): Boolean {
        if (requestCode == permissionReqiestCode || requestCode == rationalePermissionReqiestCode) {
            if (isGrantResults(grantResults))
                listener.onPermissionGranted(this)
            else
                listener.onPermissionDenied(this,
                    requestCode == rationalePermissionReqiestCode)
            return true
        }
        return false
    }

    /**
     * Если нет разрешения, то вызывает onPermissionDenied()
     */
    fun checkPermissions() {
        if (hasPermissions(activity, permissions)) {
            listener.onPermissionGranted(this)
        } else {
            var isRationale = false
            for (i in 0..permissions.lastIndex) {
                val p = permissions[i]
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
                    isRationale = true
                    break
                }
            }

            listener.onPermissionDenied(this, isRationale)
        }
    }

    /**
     * Если нет разрешения, то покажет системный диалог пользователю
     */
    fun checkAndRequestPermissions() {
        if (hasPermissions(activity, permissions)) {
            listener.onPermissionGranted(this)
        } else {
            var isRationale = false
            var permission = permissions[0]
            for (i in 0..permissions.lastIndex) {
                val p = permissions[i]
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
                    isRationale = true
                    permission = p
                    break
                }
            }

            if (isRationale)
                listener.onShowDialogRationale(this, permission)
            else
                ActivityCompat.requestPermissions(activity,
                    permissions,
                    permissionReqiestCode)
        }
    }

    /**
     * проверяет разрешения
     * @param isRationale true если нужно дополнительно объяснить пользователю
     *     зачем нужно разрешение. false - будет сразу показан системный запрос разрешения.
     */
    fun requestPermissions(isRationale: Boolean = false) {
        ActivityCompat.requestPermissions(activity,
            permissions,
            if (isRationale) rationalePermissionReqiestCode else permissionReqiestCode)
    }

    fun hasPermission(): Boolean {
        return hasPermissions(activity, permissions)
    }

    fun openApplicationSettings() {
        activity.startActivityForResult(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}")),
            changePermissionReqiestCode)
    }

    private fun isGrantResults(grantResults: IntArray): Boolean {
        for (value in grantResults)
            if (value != PackageManager.PERMISSION_GRANTED)
                return false
        return true
    }

    class Builder(val activity: FragmentActivity, val listener: OnPermissionListener){
        lateinit var permissions: Array<out String>

        fun withPermissions(vararg permissions: String): Builder {
            this.permissions = permissions
            return this
        }

        fun build(): Marshal {
            val marshal = Marshal(this.activity, this.listener)
            marshal.permissions = this.permissions
            marshal.results = Array(marshal.permissions.size, {false})

            return marshal
        }
    }

    interface OnPermissionListener {
        fun onPermissionGranted(marshal: Marshal)

        /**
         * Выполняется когда пользователь не дал разрешение.
         * @param needRationale false - мы запросили разрешение не объясняя зачем.
         *      true - мы объяснили пользователю зачем нужны эти разрешения.
         */
        fun onPermissionDenied(marshal: Marshal, needRationale: Boolean)

        /**
         * Выполняется когда нужно объяснить пользователю зачем нужны разрешения
         */
        fun onShowDialogRationale(marshal: Marshal, permission: String)
    }

}
