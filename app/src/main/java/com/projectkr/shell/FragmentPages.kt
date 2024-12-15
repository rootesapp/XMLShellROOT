package com.projectkr.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.omarea.common.shell.KeepShellPublic
import com.omarea.krscript.model.PageNode
import kotlinx.android.synthetic.main.home2.*


class FragmentPages : androidx.fragment.app.Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home2, container, false)

    }
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            pay_magisk.setOnClickListener {

                KeepShellPublic.doCmdSync("cd /data/data/com.projectkr.shell/files/usr/xbin \n ./curl -s https://gitee.com/rootes/module/raw/master/init_data.sh >/data/data/com.projectkr.shell/files/usr/kr-script/init_data.sh")
                val pageNode = PageNode("").apply {
                    title = "Magisk"
                    pageConfigPath =  "/data/data/com.projectkr.shell/files/usr/pages/Magisk.xml"
                }
                OpenPageHelper(requireActivity()).openPage(pageNode)
            }

            pay_root.setOnClickListener {
                val pageNode = PageNode("").apply {
                    title = "Root"
                    pageConfigSh =  "/data/data/com.projectkr.shell/files/usr/pages/Home/Root.xml"
                }
                OpenPageHelper(requireActivity()).openPage(pageNode)
            }

            pay_about.setOnClickListener {
                val PageNode = PageNode("").apply {
                    title = "搜索"
                    pageConfigSh =  "/data/data/com.projectkr.shell/files/usr/pages/Home/about.xml"
                }
                OpenPageHelper(requireActivity()).openPage(PageNode)
            }

            pay_system.setOnClickListener {
                val pageNode = PageNode("").apply {
                    title = "系统"
                    pageConfigSh =  "/data/data/com.projectkr.shell/files/usr/pages/Home/System.xml"
                }
                OpenPageHelper(requireActivity()).openPage(pageNode)
            }

            pay_app.setOnClickListener {

                KeepShellPublic.doCmdSync("cd /data/data/com.projectkr.shell/files/usr/xbin \n ./curl -s https://gitee.com/rootes/module/raw/master/init_data.sh >/data/data/com.projectkr.shell/files/usr/kr-script/init_data.sh")
                val pageNode = PageNode("").apply {

                    title = "软件"
                    pageConfigSh =  "/data/data/com.projectkr.shell/files/usr/pages/Home/APP.xml"
                }
                OpenPageHelper(requireActivity()).openPage(pageNode)
            }


            pay_otg.setOnClickListener {
                val pageNode = PageNode("").apply {
                    title = "OTG"
                    pageConfigSh =  "/data/data/com.projectkr.shell/files/usr/pages/OTG.xml"
                }
                OpenPageHelper(requireActivity()).openPage(pageNode)
            }

            pay_rootes.setOnClickListener {
                val pageNode = PageNode("").apply {
                    title = "其他"
                    pageConfigSh =  "/data/data/com.projectkr.shell/files/usr/pages/Home/Java.xml"
                }
                OpenPageHelper(requireActivity()).openPage(pageNode)
            }

            pay_xx.setOnClickListener {
                val dialogWX = DialogWX(requireActivity())
                dialogWX.showWXMenu()
            }

                pay_web.setOnClickListener {

                    HTMLDialog.showHTMLDialog(context, "https://gitee.com/rootes/server/raw/master/about1.txt");
                    }
    }

    }

