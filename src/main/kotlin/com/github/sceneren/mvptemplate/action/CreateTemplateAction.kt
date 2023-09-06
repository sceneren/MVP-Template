package com.github.sceneren.mvptemplate.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import javax.swing.*

class CreateTemplateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // 创建对话框窗口
        val dialog = JFrame("创建MVP模板")
        dialog.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        // 创建输入框
        val textField = JTextField()
        textField.preferredSize = Dimension(200, 30)

        val jPanel = JPanel()

        // 创建确定按钮
        val okButton = JButton("确定")
        okButton.addActionListener { _ ->
            val input = textField.text.trim() // 获取输入框的内容
            if (input.isNotEmpty()) {
                if (input.endsWith("Act") || input.endsWith("Activity") || input.endsWith("Frag") || input.endsWith("Fragment")) {
                    try {
                        val selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
                        if (selectedFile?.isDirectory == true) {
                            createFile(selectedFile.path, input)
                            dialog.dispose()
                        } else {
                            if (selectedFile?.parent != null) {
                                createFile(selectedFile.parent!!.path, input)
                                dialog.dispose()
                            } else {
                                JOptionPane.showMessageDialog(jPanel, "创建文件的位置异常")
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        JOptionPane.showMessageDialog(jPanel, e.message)
                    }
                } else {
                    JOptionPane.showMessageDialog(jPanel, "文件名请以Act\\Activity\\Frag\\Fragment结尾")
                }
            } else {
                JOptionPane.showMessageDialog(jPanel, "请输入要创建的文件名")
            }
        }

        // 创建容器用于添加内边距
        val container = JPanel()
        container.layout = BorderLayout()
        container.border = BorderFactory.createEmptyBorder(10, 10, 10, 10) // 设置内边距


        jPanel.layout = GridLayout(2, 1)
        // 将组件添加到容器中
        jPanel.add(textField)
        jPanel.add(okButton)

        container.add(jPanel)

        // 将容器添加到对话框窗口中
        dialog.contentPane.add(container)

        // 设置对话框窗口大小和位置，并显示
        dialog.pack()
        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true
    }

    private fun createFile(dir: String, input: String) {
        val regex = Regex("(Act|Activity|Fragment|Frag)$")

        val filePreName = input.replace(regex, "")

        val isActivity = input.endsWith("Act") || input.endsWith("Activity")

        val parentDir = File(dir).parentFile
        val pageFile = File(dir, "$input.kt") // 构建新文件对象
        val mvpDir = File(parentDir, "mvp")
        val cDir = File(mvpDir, "contract")
        val mDir = File(mvpDir, "model")
        val pDir = File(mvpDir, "presenter")

        val mvpCFile = File(cDir, "${filePreName}Contract.kt")
        val mvpMFile = File(mDir, "${filePreName}Model.kt")
        val mvpPFile = File(pDir, "${filePreName}Presenter.kt")

        println(pageFile.absolutePath)
        println(mvpCFile.absolutePath)
        println(mvpMFile.absolutePath)
        println(mvpPFile.absolutePath)
        try {
            if (pageFile.createNewFile()) {
                if (!cDir.exists()) {
                    cDir.mkdirs()
                }
                if (!mDir.exists()) {
                    mDir.mkdirs()
                }
                if (!pDir.exists()) {
                    pDir.mkdirs()
                }

                if (mvpCFile.createNewFile()) {
                    if (mvpMFile.createNewFile()) {
                        if (mvpPFile.createNewFile()) {
                            //往文件里面写入模板代码
                            writeContractCode(mvpCFile, filePreName)
                            writeModelCode(mvpMFile, filePreName)
                            writePresenterCode(mvpPFile, filePreName)
                        } else {
                            throw Exception("Presenter 文件创建失败")
                        }
                    } else {
                        throw Exception("Model 文件创建失败")
                    }
                } else {
                    throw Exception("Contract 文件创建失败")
                }

            } else {
                throw Exception("页面文件已经存在")
            }
        } catch (e: Exception) {
            throw Exception("创建文件时出现异常：${e.message}")
        } finally {
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(pageFile)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(mvpCFile)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(mvpMFile)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(mvpPFile)
            VirtualFileManager.getInstance().syncRefresh()
        }
    }

    private fun writeContractCode(file: File, filePreName: String) {
        // 写入模板代码
        val writer = getPrintWriter(file)
        try {
            val rootPackage = getRootPackage(file)
            writer.println("package $rootPackage")
            writer.println("import com.lanshu.base.base_mvp.i.IBaseModel")
            writer.println("import com.lanshu.base.base_mvp.i.IBaseView")
            writer.println()
            writer.println("class ${file.nameWithoutExtension} {")
            writer.println("\tinterface IView : IBaseView {")
            writer.println("\t}")
            writer.println()
            writer.println("\tinterface IModel : IBaseModel {")
            writer.println("\t}")
            writer.println("}")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            writer.flush()
            writer.close()
        }
    }

    private fun writeModelCode(file: File, filePreName: String) {
        // 写入模板代码
        val writer = getPrintWriter(file)
        try {
            val rootPackage = getRootPackage(file)
            val mvpPackage = getMvpPackage(file)

            writer.println("package $rootPackage")
            writer.println()
            writer.println("import ${mvpPackage}.contract.${filePreName}Contract")
            writer.println()
            writer.println("class ${file.nameWithoutExtension} : ${filePreName}Contract.IModel {")

            writer.println("}")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            writer.flush()
            writer.close()
        }
    }

    private fun writePresenterCode(file: File, filePreName: String) {
        // 写入模板代码
        val writer = getPrintWriter(file)
        try {
            val rootPackage = getRootPackage(file)
            val mvpPackage = getMvpPackage(file)

            writer.println("package $rootPackage")
            writer.println()
            writer.println("import com.lanshu.base.base_mvp.impl.BasePresenter")
            writer.println("import ${mvpPackage}.contract.${filePreName}Contract")
            writer.println("import ${mvpPackage}.model.${filePreName}Model")
            writer.println()
            writer.println("class ${file.nameWithoutExtension}(mView: ${filePreName}Contract.IView) : BasePresenter<${filePreName}Model, ${filePreName}Contract.IView>(${filePreName}Model(), mView){")

            writer.println("}")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            writer.flush()
            writer.close()
        }
    }

    private fun getPrintWriter(file: File): PrintWriter {
        return PrintWriter(FileWriter(file))
    }

    private fun getPackageName(file: File): String {
        val str = file.parentFile.absolutePath.replace("\\", ".")

        var result = str.split("src.main.java.")
        if (result.size != 2) {
            result = str.split("src.main.kotlin.")
        }
        if (result.size != 2) {
            result = str.split("src.")
        }
        return if (result.size != 2) {
            result[0]
        } else {
            result[1]
        }
    }

    private fun getRootPackage(file: File): String {
        return getPackageName(file)
    }

    private fun getMvpPackage(file: File): String {
        return getPackageName(file.parentFile)
    }

}