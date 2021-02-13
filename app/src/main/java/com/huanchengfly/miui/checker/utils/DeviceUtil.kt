package com.huanchengfly.miui.checker.utils

import android.text.TextUtils
import android.util.Log
import java.io.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


object DeviceUtil {
    private const val TAG = "DeviceUtil"

    private val MT_PATTERN: Pattern = Pattern.compile("MT([\\d]{2})([\\d]+)")
    private val SM_PATTERN: Pattern = Pattern.compile("Inc ([A-Z]+)([\\d]+)")

    const val DEVICE_HIGH_END = 2
    const val DEVICE_MIDDLE = 1
    const val DEVICE_PRIMARY = 0
    const val DEVICE_UNKNOWN = -1

    private const val ARM_V8 = 8
    private const val CORE_COUNT = 8
    private const val BIG_CORE_FREQ = 2000000
    private const val MIDDLE_FREQ = 2300000
    private const val HIGH_FREQ = 2700000
    private const val MTK_DIMENSITY = 68
    private const val D800 = 73
    private const val MIDDLE_EIGHT_SERIES = 45

    private const val HEX = "0x"
    private const val CPU_MAX_INFO_FORMAT = "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq"
    private const val IMPLEMENTOR = "CPU implementer"
    private const val ARCHITECTURE = "CPU architecture"
    private const val PART = "CPU part"
    private const val PROCESSOR = "processor"
    private const val QUALCOMM = "Qualcomm"
    private const val SNAPDRAGON = "sm"
    private const val SEPARATOR = ": "

    private var mLevel = DEVICE_UNKNOWN
    private var mTotalRam = Int.MAX_VALUE

    fun getDeviceLevel(): Int {
        if (mLevel != DEVICE_UNKNOWN) {
            return mLevel
        }
        if (isMiuiLite()) {
            mLevel = DEVICE_PRIMARY
        } else {
            mLevel = getCpuLevel()
            if (mLevel == DEVICE_MIDDLE && getTotalRam() <= 4) {
                mLevel = DEVICE_PRIMARY
            }
        }
        return mLevel
    }

    fun getTotalRam(): Int {
        if (mTotalRam == Int.MAX_VALUE) {
            mTotalRam = try {
                ((Class.forName("miui.util.HardwareInfo")
                    .getMethod("getTotalPhysicalMemory")
                    .invoke(null) as Long).toLong() / 1024 / 1024 / 1024).toInt()
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
                0
            }
        }
        return mTotalRam
    }

    fun isMiuiLite(): Boolean {
        return try {
            Class.forName("miui.os.Build").getDeclaredField("IS_MIUI_LITE_VERSION")
                .get(null) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createCpuInfo(str: String): CpuInfo {
        val cpuInfo = CpuInfo()
        cpuInfo.id = str.toInt()
        val contentFromFileInfo = getContentFromFileInfo(
            String.format(
                Locale.ENGLISH,
                CPU_MAX_INFO_FORMAT,
                cpuInfo.id!!
            )
        )
        if (contentFromFileInfo != null) {
            cpuInfo.maxFreq = contentFromFileInfo.toInt()
        }
        return cpuInfo
    }

    private fun getContentFromFileInfo(filePath: String): String? {
        try {
            val fileInputStream = FileInputStream(filePath)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val str = bufferedReader.readLine()
            bufferedReader.close()
            fileInputStream.close()
            inputStreamReader.close()
            return str
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getCpuInfoList(): List<CpuInfo> {
        val arrayList = mutableListOf<CpuInfo>()
        try {
            val scanner = Scanner(File("/proc/cpuinfo"))
            var cpuInfo: CpuInfo? = null
            while (scanner.hasNextLine()) {
                val split: List<String> = scanner.nextLine().split(SEPARATOR)
                if (split.size > 1) {
                    cpuInfo = parseLine(split, arrayList, cpuInfo)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "getChipSetFromCpuInfo failed", e)
        }
        return arrayList
    }

    private fun getCpuStats(): CpuStats {
        val cpuInfoList: List<CpuInfo> = getCpuInfoList()
        val cpuStats = CpuStats()
        if (cpuInfoList.size < CORE_COUNT) {
            cpuStats.level = DEVICE_PRIMARY
        }
        doCpuStats(cpuStats, cpuInfoList)
        return cpuStats
    }

    private fun doCpuStats(cpuStats: CpuStats, list: List<CpuInfo>) {
        for (next in list) {
            if (next.architecture!! < ARM_V8) {
                cpuStats.level = DEVICE_PRIMARY
            }
            if (next.maxFreq > cpuStats.maxFreq) {
                cpuStats.maxFreq = next.maxFreq
            }
            if (next.maxFreq >= BIG_CORE_FREQ) {
                cpuStats.bigCoreCount++
            } else {
                cpuStats.smallCoreCount++
            }
        }
        decideLevel(cpuStats)
    }

    private fun decideLevel(cpuStats: CpuStats) {
        if (cpuStats.level == DEVICE_UNKNOWN) {
            cpuStats.level = when {
                cpuStats.bigCoreCount >= 4 ->
                    when {
                        cpuStats.maxFreq > HIGH_FREQ -> DEVICE_HIGH_END
                        cpuStats.maxFreq > MIDDLE_FREQ -> DEVICE_MIDDLE
                        else -> DEVICE_PRIMARY
                    }
                cpuStats.maxFreq > MIDDLE_FREQ -> DEVICE_MIDDLE
                else -> DEVICE_PRIMARY
            }
        }
    }

    private fun parseLine(
        strArr: List<String>,
        list: MutableList<CpuInfo>,
        cpuInfo: CpuInfo?
    ): CpuInfo? {
        val trim = strArr[1].trim { it <= ' ' }
        return if (strArr[0].contains(PROCESSOR) && TextUtils.isDigitsOnly(trim)) {
            createCpuInfo(trim).also { list.add(it) }
        } else {
            if (cpuInfo != null) {
                getCpuInfo(strArr[0], trim, cpuInfo)
            }
            cpuInfo
        }
    }

    private fun getCpuInfo(str: String, str2: String, cpuInfo: CpuInfo) {
        when {
            str.contains(IMPLEMENTOR) -> {
                cpuInfo.implementor = toInt(str2)
            }
            str.contains(ARCHITECTURE) -> {
                cpuInfo.architecture = toInt(str2)
            }
            str.contains(PART) -> {
                cpuInfo.part = toInt(str2)
            }
        }
    }

    private fun toInt(str: String): Int {
        return if (str.startsWith(HEX)) {
            str.substring(2).toInt(16)
        } else {
            str.toInt()
        }
    }

    fun getHardwareInfo(): String {
        return try {
            val scanner = Scanner(File("/proc/cpuinfo"))
            while (scanner.hasNextLine()) {
                val nextLine = scanner.nextLine()
                if (!scanner.hasNextLine()) {
                    val split = nextLine.split(SEPARATOR).toTypedArray()
                    if (split.size > 1) {
                        return split[1]
                    }
                }
            }
            ""
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "getChipSetFromCpuInfo failed", e)
            ""
        }
    }

    private fun getCpuLevel(): Int {
        val hardwareInfo = getHardwareInfo()
        val level = if (hardwareInfo.isNotEmpty()) {
            if (hardwareInfo.contains(QUALCOMM)) {
                getQualcommCpuLevel(hardwareInfo)
            } else {
                getMtkCpuLevel(hardwareInfo)
            }
        } else {
            DEVICE_UNKNOWN
        }
        return if (level == DEVICE_UNKNOWN) {
            getCpuStats().level
        } else {
            level
        }
    }

    private fun getMtkCpuLevel(str: String): Int {
        val matcher: Matcher = MT_PATTERN.matcher(str)
        if (!matcher.find()) {
            return DEVICE_UNKNOWN
        }
        val group: String? = matcher.group(1)
        val group2: String? = matcher.group(2)
        if (group == null || group2 == null) {
            return DEVICE_UNKNOWN
        }
        val parseInt = group.toInt()
        val parseInt2 = group2.toInt()
        return if (parseInt != MTK_DIMENSITY || parseInt2 < D800) {
            DEVICE_PRIMARY
        } else {
            DEVICE_MIDDLE
        }
    }

    private fun getQualcommCpuLevel(str: String): Int {
        val matcher: Matcher = SM_PATTERN.matcher(str)
        if (!matcher.find()) {
            return DEVICE_UNKNOWN
        }
        val group: String? = matcher.group(1)
        val group2: String? = matcher.group(2)
        if (group == null || group2 == null || group.toLowerCase(Locale.ENGLISH) != SNAPDRAGON) {
            return DEVICE_UNKNOWN
        }
        val parseInt = group2.substring(0, 1).toInt()
        val parseInt2 = group2.substring(1).toInt()
        if (parseInt >= 8 && parseInt2 > MIDDLE_EIGHT_SERIES) {
            return DEVICE_HIGH_END
        }
        return if (parseInt >= 7) {
            DEVICE_MIDDLE
        } else {
            DEVICE_PRIMARY
        }
    }

    class CpuInfo {
        var id: Int? = null
        var implementor: Int? = null
        var architecture: Int? = null
        var part: Int? = null
        var maxFreq: Int = 0

        override fun toString(): String {
            return "CpuInfo(id=$id, implementor=$implementor, architecture=$architecture, part=$part, maxFreq=$maxFreq)"
        }
    }

    class CpuStats {
        var level: Int = DEVICE_UNKNOWN
        var maxFreq: Int = 0
        var bigCoreCount: Int = 0
        var smallCoreCount: Int = 0

        override fun toString(): String {
            return "CpuStats(level=$level, maxFreq=$maxFreq, bigCoreCount=$bigCoreCount, smallCoreCount=$smallCoreCount)"
        }
    }
}