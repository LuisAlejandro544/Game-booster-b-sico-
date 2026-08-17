#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <sys/sysinfo.h>
#include <fstream>
#include <sstream>

#define TAG "GameBoosterNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static unsigned long long prevUser = 0, prevNice = 0, prevSys = 0, prevIdle = 0, prevIowait = 0, prevIrq = 0, prevSoftirq = 0;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_util_NativeEngineBridge_getNativeEngineVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "C++ Native Turbo Engine v1.1.0";
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_util_NativeEngineBridge_getNativeFreeRamBytes(
        JNIEnv* env,
        jobject /* this */) {
    struct sysinfo info;
    if (sysinfo(&info) == 0) {
        return (jlong)(info.freeram * info.mem_unit);
    }
    return -1;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_util_NativeEngineBridge_getNativeCpuUsage(
        JNIEnv* env,
        jobject /* this */) {
    std::ifstream statFile("/proc/stat");
    if (!statFile.is_open()) {
        return -1;
    }

    std::string line;
    if (std::getline(statFile, line)) {
        std::istringstream ss(line);
        std::string cpuLabel;
        unsigned long long user, nice, sys, idle, iowait, irq, softirq;
        if (ss >> cpuLabel >> user >> nice >> sys >> idle >> iowait >> irq >> softirq) {
            unsigned long long prevTotal = prevUser + prevNice + prevSys + prevIdle + prevIowait + prevIrq + prevSoftirq;
            unsigned long long currentTotal = user + nice + sys + idle + iowait + irq + softirq;
            unsigned long long prevIdleTotal = prevIdle + prevIowait;
            unsigned long long currentIdleTotal = idle + iowait;

            unsigned long long totalDelta = currentTotal - prevTotal;
            unsigned long long idleDelta = currentIdleTotal - prevIdleTotal;

            prevUser = user;
            prevNice = nice;
            prevSys = sys;
            prevIdle = idle;
            prevIowait = iowait;
            prevIrq = irq;
            prevSoftirq = softirq;

            if (totalDelta > 0 && totalDelta >= idleDelta) {
                unsigned long long activeDelta = totalDelta - idleDelta;
                int usage = (int)((activeDelta * 100) / totalDelta);
                return (usage >= 0 && usage <= 100) ? usage : 15;
            }
        }
    }
    return -1;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_util_NativeEngineBridge_getNativeOptimalBuffer(
        JNIEnv* env,
        jobject /* this */,
        jint currentPing,
        jint ramUsagePercent) {
    int baseBuffer = 64;
    if (currentPing > 100) {
        baseBuffer = 128;
    }
    if (ramUsagePercent > 80) {
        baseBuffer *= 2;
    }
    return baseBuffer;
}

