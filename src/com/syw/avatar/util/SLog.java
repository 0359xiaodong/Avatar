package com.syw.avatar.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Environment;

public class SLog {
    // 日志类型标识符(优先级：由低到高排列，取值越小优先级越高)
    public static final char SHOW_VERBOSE_LOG = 0x01;
    public static final char SHOW_DEBUG_LOG = 0x02;
    public static final char SHOW_INFO_LOG = 0x04;
    public static final char SHOW_WARN_LOG = 0x08;
    public static final char SHOW_ERROR_LOG = 0x10;

    // 默认为五种日志类型均在 LogCat 中输出显示
    public static char m_cLogCatShowLogType = SHOW_VERBOSE_LOG | SHOW_DEBUG_LOG
            | SHOW_INFO_LOG | SHOW_WARN_LOG | SHOW_ERROR_LOG;
    // 默认为五种日志类型均不在 日志文件 中输出保存
    public static char m_cFileSaveLogType = 0x00;
    // 以下注释不要删除，以便日后开启指定日志类型输出到日志文件中
    // public static char m_cFileSaveLogType = SHOW_VERBOSE_LOG |
    // SHOW_DEBUG_LOG |
    // SHOW_INFO_LOG |
    // SHOW_WARN_LOG |
    // SHOW_ERROR_LOG;
    // 存放日志文件的目录全路径
    public static String m_strLogFolderPath = "";

    private static void SaveLog2File(String TAG, String strMsg) {
        FileWriter objFilerWriter = null;
        BufferedWriter objBufferedWriter = null;

        do // 非循环，只是为了减少分支缩进深度
        {
            String state = Environment.getExternalStorageState();
            // 未安装 SD 卡
            if (true != Environment.MEDIA_MOUNTED.equals(state)) {
                android.util.Log.d(TAG, "Not mount SD card!");
                break;
            }

            // SD 卡不可写
            if (true == Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                android.util.Log.d(TAG, "Not allow write SD card!");
                break;
            }

            // 只有存在外部 SD 卡且可写入的情况下才允许保存日志文件到指定目录路径下
            // 没有指定日志文件存放位置的话，就写到默认位置，即 SD 卡根目录下的 custom_android_log 目录中
            if (true == m_strLogFolderPath.trim().equals("")) {
                String strSaveLogPath = Environment
                        .getExternalStorageDirectory() + "/imuse/log";

                File fileSaveLogFolderPath = new File(strSaveLogPath);
                // 保存日志文件的路径不存在的话，就创建它
                if (true != fileSaveLogFolderPath.exists()) {
                    fileSaveLogFolderPath.mkdirs();
                }

                // 如果这里保存日志文件的路径还不存在的话，则要提醒用户了
                if (true != fileSaveLogFolderPath.exists()) {
                    android.util.Log.d(TAG, "Create log folder failed!");
                    break;
                }

                // 指定日志文件保存的路径，文件名由内部按日期时间形式
                m_strLogFolderPath = strSaveLogPath;
            }

            // 得到当前日期时间的指定格式字符串
            String strDateTimeFileName = new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date());

            File fileLogFilePath = new File(m_strLogFolderPath,
                    strDateTimeFileName + ".log");
            // 如果日志文件不存在，则创建它
            if (true != fileLogFilePath.exists()) {
                try {
                    fileLogFilePath.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            // 如果执行到这步日志文件还不存在，就不写日志到文件了
            if (true != fileLogFilePath.exists()) {
                android.util.Log.d(TAG, "Create log file failed!");
                break;
            }

            try {
                objFilerWriter = new FileWriter(fileLogFilePath, //
                        true); // 续写不覆盖
            } catch (IOException e1) {
                android.util.Log.d(TAG, "New FileWriter Instance failed");
                e1.printStackTrace();
                break;
            }

            objBufferedWriter = new BufferedWriter(objFilerWriter);

            // 得到当前日期时间的指定格式字符串
            String strDateTimeLogHead = new SimpleDateFormat(
                    "yyyy-MM-dd_HH:mm:ss").format(new Date());

            // 将日期时间头与日志信息体结合起来
            strMsg = TAG + " " + strDateTimeLogHead + " " + strMsg + "\n\n";

            try {
                objBufferedWriter.write(strMsg);
                objBufferedWriter.flush();
            } catch (IOException e) {
                android.util.Log
                        .d(TAG,
                                "objBufferedWriter.write or objBufferedWriter.flush failed");
                e.printStackTrace();
            }

        } while (false);

        if (null != objBufferedWriter) {
            try {
                objBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (null != objFilerWriter) {
            try {
                objFilerWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void v(String TAG, String msg) {
        // 空字符串或全是空格的字符串做为日志输出没有意义！
        if ((null == msg) || (true == msg.trim().equals(""))) {
            return;
        }

        String strMsg = "\nfile: "
                + new Throwable().getStackTrace()[1].getFileName()
                + " \nclass: "
                + new Throwable().getStackTrace()[1].getClassName()
                + " \nmethod: "
                + new Throwable().getStackTrace()[1].getMethodName()
                + " \nline: "
                + new Throwable().getStackTrace()[1].getLineNumber() + " \n"
                + msg + "\n\n";

        if (0 != (SHOW_VERBOSE_LOG & m_cLogCatShowLogType)) {
            android.util.Log.v(TAG, msg);
        }

        if (0 != (SHOW_VERBOSE_LOG & m_cFileSaveLogType)) {
            SaveLog2File(TAG, strMsg);
        }
    }

    public static void d(String TAG, String msg) {
        // 空字符串或全是空格的字符串做为日志输出没有意义！
        if ((null == msg) || (true == msg.trim().equals(""))) {
            return;
        }

//        String strMsg = "\nfile: "
//                + new Throwable().getStackTrace()[1].getFileName()
//                + " \nclass: "
//                + new Throwable().getStackTrace()[1].getClassName()
//                + " \nmethod: "
//                + new Throwable().getStackTrace()[1].getMethodName()
//                + " \nline: "
//                + new Throwable().getStackTrace()[1].getLineNumber() + " \n"
//                + msg;

      String strMsg = new Throwable().getStackTrace()[1].getFileName()
              + " [M:" + new Throwable().getStackTrace()[1].getMethodName() + "]"
              + " [L:" + new Throwable().getStackTrace()[1].getLineNumber() + "]\n"
              + msg;
        if (0 != (SHOW_DEBUG_LOG & m_cLogCatShowLogType)) {
            android.util.Log.d(TAG, strMsg);
        }

        if (0 != (SHOW_DEBUG_LOG & m_cFileSaveLogType)) {
            SaveLog2File(TAG, strMsg);
        }
    }

    public static void i(String TAG, String msg) {
        // 空字符串或全是空格的字符串做为日志输出没有意义！
        if ((null == msg) || (true == msg.trim().equals(""))) {
            return;
        }

        String strMsg = "\nfile: "
                + new Throwable().getStackTrace()[1].getFileName()
                + " \nclass: "
                + new Throwable().getStackTrace()[1].getClassName()
                + " \nmethod: "
                + new Throwable().getStackTrace()[1].getMethodName()
                + " \nline: "
                + new Throwable().getStackTrace()[1].getLineNumber() + " \n"
                + msg;

        if (0 != (SHOW_INFO_LOG & m_cLogCatShowLogType)) {
            android.util.Log.i(TAG, msg);
        }

        if (0 != (SHOW_INFO_LOG & m_cFileSaveLogType)) {
            SaveLog2File(TAG, strMsg);
        }
    }

    public static void w(String TAG, String msg) {
        // 空字符串或全是空格的字符串做为日志输出没有意义！
        if ((null == msg) || (true == msg.trim().equals(""))) {
            return;
        }

        String strMsg = "\nfile: "
                + new Throwable().getStackTrace()[1].getFileName()
                + " \nclass: "
                + new Throwable().getStackTrace()[1].getClassName()
                + " \nmethod: "
                + new Throwable().getStackTrace()[1].getMethodName()
                + " \nline: "
                + new Throwable().getStackTrace()[1].getLineNumber() + " \n"
                + msg;

        if (0 != (SHOW_WARN_LOG & m_cLogCatShowLogType)) {
            android.util.Log.w(TAG, msg);
        }

        if (0 != (SHOW_WARN_LOG & m_cFileSaveLogType)) {
            SaveLog2File(TAG, strMsg);
        }
    }

    public static void e(String TAG, String msg) {
        // 空字符串或全是空格的字符串做为日志输出没有意义！
        if ((null == msg) || (true == msg.trim().equals(""))) {
            return;
        }

        String strMsg = "\nfile: "
                + new Throwable().getStackTrace()[1].getFileName()
                + " \nclass: "
                + new Throwable().getStackTrace()[1].getClassName()
                + " \nmethod: "
                + new Throwable().getStackTrace()[1].getMethodName()
                + " \nline: "
                + new Throwable().getStackTrace()[1].getLineNumber() + " \n"
                + msg;

        if (0 != (SHOW_ERROR_LOG & m_cLogCatShowLogType)) {
            android.util.Log.e(TAG, msg);
        }

        if (0 != (SHOW_ERROR_LOG & m_cFileSaveLogType)) {
            SaveLog2File(TAG, strMsg);
        }
    }
}