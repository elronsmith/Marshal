package ru.elron.examplemarshal.ui.medium

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_medium.*
import ru.elron.examplemarshal.R
import ru.elron.examplemarshal.utils.Marshal

/**
 * Проверяет разрешение во время открытия экрана
 * - если разрешение есть, то отображаем список файлов
 * - если разрешения нет:
 *   - если 1 раз, то сразу отображаем системный диалог
 *   - если уже отображали и пользователь не предоставил разрешение, то показываем
 *     свой диалог с объяснением зачем нам нужно это разрешение
 */
class MediumActivity : AppCompatActivity(R.layout.activity_medium), Marshal.OnPermissionListener {
    private lateinit var marshal: Marshal

    companion object {
        fun start(activity: FragmentActivity) {
            activity.startActivity((Intent(activity, MediumActivity::class.java)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        marshal = Marshal.Builder(this, this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .build()
    }

    override fun onStart() {
        super.onStart()
        marshal.checkAndRequestPermissions()
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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        marshal.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionGranted(marshal: Marshal) {
        updateFileList()
    }

    override fun onPermissionDenied(marshal: Marshal, needRationale: Boolean) {
        updateFileList()
        onShowDialogRationale(marshal, marshal.permissions[0])
    }

    override fun onShowDialogRationale(marshal: Marshal, permission: String) {
        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_read_external_storage)
            .setPositiveButton(R.string.permission_button_allow) { _, _ ->
                marshal.requestPermissions(true)
            }
            .setNegativeButton(R.string.permission_button_close) { _, _ ->
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton(R.string.permission_button_settings) { _, _ ->
                marshal.openApplicationSettings()
            }
            .setCancelable(false)

        builder.create().show()
    }
}
