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
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.util.Base64
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
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
import com.omarea.krscript.model.*
import com.omarea.krscript.ui.ActionListFragment
import com.omarea.krscript.ui.ParamsFileChooserRender
import com.projectkr.shell.permissions.CheckRootStatus
import com.projectkr.shell.ui.TabIconHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Calendar
import kotlin.random.Random
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import com.omarea.common.shell.KeepShellPublic
import com.omarea.common.ui.DialogHelper
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val progressBarDialog = ProgressBarDialog(this)
    private var handler = Handler()
    private lateinit var usbManager: UsbManager

    // 一言提示列表
    private val oneWordsList = listOf(
        "努力不一定成功，但放弃一定失败。",
        "生活不止眼前的苟且，还有诗和远方。",
        "机会是给有准备的人的。",
        "成功源于不懈的努力。",
        "原神，启动",
        "困难犹如弹簧，你弱它就强，你强它就弱。",
        "天空没有翅膀的痕迹，但鸟儿已经飞过。",
        "人生最重要的不是所站的位置，而是所朝的方向。",
        "世界会向那些有目标和远见的人让路。",
        "别人的看法只是参考，而非定论。",
        "梦想是步履的动力，努力是梦想的实现。",
        "成功不是将来才有的，而是从决定去做的那一刻起，持续累积而成。",
        "每一次选择都是一种放弃，每一次放弃都是一种选择。",
        "坚持下去，才能看到最美的风景。",
        "相信自己，你就已走了成功的第一步。",
        "努力奋斗的人生，比轻松颓废的人生更有价值。",
        "成功就是把复杂的问题简单化，然后狠狠去做。",
        "没有人值得你流泪，值得让你这么做的人不会让你哭。",
        "世界不会给予你什么，一切都靠自己努力。",
        "刷机要范围，售后一笑，生死难料。",
        "命运掌握在自己手中，而不是掌握在他人口中。",
        "不要等待机会，而要创造机会。",
        "梦想是心灵的引导者，勇气是梦想的同伴。",
        "只有敢于冒险，才能发现自己的潜能。",
        "坚持下去，你会发现比想象中更强大的自己。",
        "每一次挑战都是成长的机会，迎接它们吧。",
        "行动胜于空谈，成果来自于付出。",
        "坚持信念，即使孤独，也能走得更远。",
        "勇敢面对不确定性，那里蕴藏着无限可能。",
        "别让过去的错误影响你未来的成功。",
        "只要心中有梦想，就能找到通往目标的路。",
        "做最好的自己，而不是和别人比较。",
        "无论遇到什么，都要保持微笑，它是最好的武器。",
        "创造力源于对新事物的勇敢尝试。",
        "相信自己的直觉，它通常是正确的。",
        "在每一次选择中都展现你的真实价值。",
        "努力和智慧是达成目标的双翼。",
        "帮助他人成功，也是自己成功的一部分。",
        "生活的意义在于不断探索和前进，而非停滞和畏缩。",
        "困难是通向成功的必经之路，每一步都让你更加坚强。",
        "成功并非终点，而是一段不断努力的旅程。",
    )
    private fun checkPermission(permission: String): Boolean = PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED
    private var isDialogShown = false
    private fun readFile(filePath: String): String {
        val file = File(filePath)
        return if (file.exists()) {
            try {
                val inputStream = FileInputStream(file)
                inputStream.bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                e.printStackTrace()
                "Error reading file"
            }
        } else {
            "File not found"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
      
        Update().checkUpdate(this)


        val calendar = Calendar.getInstance()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
    
    val greetingMessage = when {
    hourOfDay in 6..11 -> "早上好，早上起来第一时间开始玩机！"
    hourOfDay in 12..17 -> "中午好，放松放松一下，在玩玩机~"
    hourOfDay in 18..20 -> "下午好，喝一杯奶茶，继续玩机~"
    else -> "晚上好，晚上刷机有危险！"
}

// 使用 this@MainActivity 作为上下文
Toast.makeText(this@MainActivity, greetingMessage, Toast.LENGTH_SHORT).show()
        // 随机选择一言提示
        val randomIndex = Random.nextInt(oneWordsList.size)
        val randomOneWord = oneWordsList[randomIndex]
        // 显示 Toast 提示
        Toast.makeText(this, randomOneWord, Toast.LENGTH_SHORT).show()
        
        super.onCreate(savedInstanceState)
        ThemeModeState.switchTheme(this)
        setContentView(R.layout.activity_main)

       usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        //  val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        //supportActionBar!!.elevation = 0f
        setTitle(R.string.app_name2)


        main_tabhost.setup()
        val tabIconHelper = TabIconHelper(main_tabhost, this)
        main_tabhost.setOnTabChangedListener {
            tabIconHelper.updateHighlight()
        }


            val home = FragmentHome()
         val donate = FragmentPages()
       // val home2 = FragmentPages()
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.main_tabhost_cpu, home)
            transaction.replace(R.id..id.main_tabhost_3, donate)
      //  transaction.replace(R.id.main_tabhost_3, home2)


        tabIconHelper.newTabSpec(getString(R.string.tab_home), getDrawable(R.drawable.tab_home)!!, R.id.main_tabhost_cpu)
        tabIconHelper.newTabSpec(getString(R.string.tab_pages), getDrawable(R.drawable.tab_pages)!!, R.id.main_tabhost_3)
            tabIconHelper.newTabSpec(getString(R.string.tab_favorites), getDrawable(R.drawable.tab_favorites)!!, R.id.main_tabhost_donate)
            transaction.commitAllowingStateLoss()


        if (!(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 111);
        }
    }

    private fun getItems(pageNode: PageNode): ArrayList<NodeInfoBase>? {
        var items: ArrayList<NodeInfoBase>? = null

        if (!pageNode.pageConfigSh.isNullOrEmpty()) {
    items = PageConfigSh(this, pageNode.pageConfigSh, null).execute()
}

        if (items == null && pageNode.pageConfigPath.isNotEmpty()) {
            items = PageConfigReader(this.applicationContext, pageNode.pageConfigPath, null).readConfigXml()
        }

        return items
    }

    private fun requestUSBPermission() {
        val usbDeviceList = usbManager.deviceList
        val deviceIterator = usbDeviceList.values.iterator()

        while (deviceIterator.hasNext()) {
            val usbDevice = deviceIterator.next()

            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            usbManager.requestPermission(usbDevice, permissionIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(usbReceiver)
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        usbDevice?.let {
                            // 权限已授予，可以进行USB设备的操作
                        }
                    } else {
                        // 权限未授予，无法进行USB设备的操作
                    }
                }
            }
        }
    }

companion object {
    const val ACTION_USB_PERMISSION = "com.projectkr.shell.USB_PERMISSION"
    const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
}

    private var fileSelectedInterface: ParamsFileChooserRender.FileSelectedInterface? = null
    private val ACTION_FILE_PATH_CHOOSER = 65400
    private val ACTION_FILE_PATH_CHOOSER_INNER = 65300

    private fun chooseFilePath(extension: String) {
        try {
            val intent = Intent(this, ActivityFileSelector::class.java)
            intent.putExtra("extension", extension)
            startActivityForResult(intent, ACTION_FILE_PATH_CHOOSER_INNER)
        } catch (ex: java.lang.Exception) {
            Toast.makeText(this, "启动内置文件选择器失败！", Toast.LENGTH_SHORT).show()
        }
    }


private fun requestOverlayPermission() {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
}


    private fun chooseFilePath(fileSelectedInterface: ParamsFileChooserRender.FileSelectedInterface): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.kr_write_external_storage), Toast.LENGTH_LONG).show()
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2);
            return false
        } else {
            return try {
                val suffix = fileSelectedInterface.suffix()
                if (suffix != null && suffix.isNotEmpty()) {
                    chooseFilePath(suffix)
                } else {
                    val intent = Intent(Intent.ACTION_GET_CONTENT);
                    val mimeType = fileSelectedInterface.mimeType()
                    if (mimeType != null) {
                        intent.type = mimeType
                    } else {
                        intent.type = "*/*"
                    }
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, ACTION_FILE_PATH_CHOOSER);
                }
                this.fileSelectedInterface = fileSelectedInterface
                true;
            } catch (ex: java.lang.Exception) {
                false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTION_FILE_PATH_CHOOSER) {
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (fileSelectedInterface != null) {
                if (result != null) {
                    val absPath = getPath(result)
                    fileSelectedInterface?.onFileSelected(absPath)
                } else {
                    fileSelectedInterface?.onFileSelected(null)
                }
            }
            this.fileSelectedInterface = null
        } else if (requestCode == ACTION_FILE_PATH_CHOOSER_INNER) {
            val absPath = if (data == null || resultCode != Activity.RESULT_OK) null else data.getStringExtra("file")
            fileSelectedInterface?.onFileSelected(absPath)
            this.fileSelectedInterface = null
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getPath(uri: Uri): String? {
        try {
            return FilePathResolver().getPath(this, uri)
        } catch (ex: Exception) {
            return null
        }
    }

    fun _openPage(pageNode: PageNode) {
        OpenPageHelper(this).openPage(pageNode)
    }

    private fun getDensity(): Int {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.densityDpi
    }

private fun hasOverlayPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        // 在低于 Android M 的版本中，默认认为应用有悬浮窗权限
        true
    }
}


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        // 检查标题栏是否为空
        supportActionBar?.let { actionBar ->
            actionBar.elevation = 0f
            actionBar.setTitle(R.string.app_name2)
        }

        // 检查 main_tabhost_cpu 是否为空并且可见
        if (main_tabhost_cpu != null && main_tabhost_cpu.visibility == View.VISIBLE) {
            menu.findItem(R.id.action_graph).isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {


            R.id.option_menu_settings -> {
                
            Toast.makeText(this, "正在开发", Toast.LENGTH_SHORT).show()
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
