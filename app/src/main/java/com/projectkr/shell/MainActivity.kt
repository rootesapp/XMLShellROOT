package com.projectkr.shell

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.content.SharedPreferences
import android.util.DisplayMetrics
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.omarea.common.shared.FilePathResolver
import com.omarea.common.ui.ProgressBarDialog
import com.omarea.krscript.config.PageConfigReader
import com.omarea.krscript.config.PageConfigSh
import com.projectkr.shell.FloatMonitor
import com.omarea.krscript.model.*
import com.omarea.krscript.ui.ActionListFragment
import com.omarea.krscript.ui.ParamsFileChooserRender
import com.projectkr.shell.ui.TabIconHelper
import kotlinx.android.synthetic.main.activity_main.*
import com.projectkr.shell.permissions.CheckRootStatus
import com.omarea.common.ui.DialogHelper
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.provider.Settings
import net.khirr.android.privacypolicy.PrivacyPolicyDialog
import com.projectkr.shell.Update

class MainActivity : AppCompatActivity() {
    private val progressBarDialog = ProgressBarDialog(this)
    private var handler = Handler()
    private lateinit var globalSPF: SharedPreferences
    private var krScriptConfig = KrScriptConfig()

    private var fileSelectedInterface: ParamsFileChooserRender.FileSelectedInterface? = null

    private fun checkPermission(permission: String): Boolean = PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)  // 确保布局文件加载在Fragment替换之前
        Update().checkUpdate(this)

        // Network status check
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager?.activeNetworkInfo?.isConnected == true) {
            Toast.makeText(this, "欢迎(无话可说)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "无网络连接，自己写吧", Toast.LENGTH_SHORT).show()
        }

        krScriptConfig = KrScriptConfig()

        progressBarDialog.showDialog(getString(R.string.please_wait))

        Thread(Runnable {
            // Removed XML file reading and config parsing

            handler.post {
                progressBarDialog.hideDialog()
            }
        }).start()

        // 只进行一次 Fragment 替换操作
        val home = FragmentHome()
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.main_tabhost_cpu, home)
        transaction.commitAllowingStateLoss()

        // 检查权限并请求
        if (!(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 111)
        }
    }

    private fun chooseFilePath(fileSelectedInterface: ParamsFileChooserRender.FileSelectedInterface): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.kr_write_external_storage), Toast.LENGTH_LONG).show()
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
            return false
        } else {
            val suffix = fileSelectedInterface.suffix()
            if (!suffix.isNullOrEmpty()) {
                // 使用文件扩展名
                chooseFilePath(suffix)
            } else {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                val mimeType = fileSelectedInterface.mimeType()
                intent.type = mimeType ?: "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, ACTION_FILE_PATH_CHOOSER)
            }
            this.fileSelectedInterface = fileSelectedInterface
            return true
        }
    }

    private fun chooseFilePath(extension: String) {
        try {
            val intent = Intent(this, ActivityFileSelector::class.java)
            intent.putExtra("extension", extension)
            startActivityForResult(intent, 65400)
        } catch (ex: Exception) {
            Toast.makeText(this, "启动内置文件选择器失败！", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle activity result for file chooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ACTION_FILE_PATH_CHOOSER) {
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            fileSelectedInterface?.onFileSelected(result?.let { getPath(it) })
            fileSelectedInterface = null
        } else if (requestCode == ACTION_FILE_PATH_CHOOSER_INNER) {
            val absPath = data?.getStringExtra("file")
            fileSelectedInterface?.onFileSelected(absPath)
            fileSelectedInterface = null
        }
    }

    private fun getPath(uri: Uri): String? {
        return try {
            FilePathResolver().getPath(this, uri)
        } catch (ex: Exception) {
            null
        }
    }

    // Handle page opening
    fun _openPage(pageNode: PageNode) {
        OpenPageHelper(this).openPage(pageNode)
    }

    private fun getDensity(): Int {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.densityDpi
    }

    // Permissions handling
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fileSelectedInterface?.let { chooseFilePath(it) }
        } else {
            Toast.makeText(this, "权限被拒绝，无法选择文件", Toast.LENGTH_SHORT).show()
        }
    }

    // Menu creation and item selection
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_graph).isVisible = main_tabhost_cpu.visibility == View.VISIBLE
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_menu_info -> {
                val intent = Intent()
                intent.setClass(this, AboutActivity::class.java)
                startActivity(intent)
            }
            R.id.option_menu_reboot -> {
                DialogPower(this).showPowerMenu()
            }
            R.id.action_graph -> {
                if (FloatMonitor.isShown == true) {
                    FloatMonitor(this).hidePopupWindow()
                    return false
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        FloatMonitor(this).showPopupWindow()
                        Toast.makeText(this, getString(R.string.float_monitor_tips), Toast.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        startActivity(intent)
                    }
                } else {
                    FloatMonitor(this).showPopupWindow()
                    Toast.makeText(this, getString(R.string.float_monitor_tips), Toast.LENGTH_LONG).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Constants for request codes
    private val ACTION_FILE_PATH_CHOOSER = 1234
    private val ACTION_FILE_PATH_CHOOSER_INNER = 1235
}
