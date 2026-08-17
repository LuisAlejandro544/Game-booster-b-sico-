#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include <sys/sysinfo.h>
#include <algorithm>
#include <vector>
#include <numeric>
#include <cmath>

#define TAG "GameBoosterNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static unsigned long long prevUser = 0, prevNice = 0, prevSys = 0, prevIdle = 0, prevIowait = 0, prevIrq = 0, prevSoftirq = 0;
static bool hasPrevCpu = false;

// Zero-allocation ring buffer for high-precision frametimes (120 frames history)
static constexpr int FRAMETIME_HISTORY_SIZE = 120;
static float s_frametime_history[FRAMETIME_HISTORY_SIZE] = {0};
static int s_frametime_head = 0;
static int s_frametime_count = 0;
static uint64_t s_last_frame_nanos = 0;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_util_NativeEngineBridge_getNativeEngineVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "C++ Native Turbo Engine v2.0 (Zero-Alloc & High-Precision)";
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

// Zero-allocation CPU reading using raw POSIX open/read into stack buffer
extern "C" JNIEXPORT jint JNICALL
Java_com_example_util_NativeEngineBridge_getNativeCpuUsage(
        JNIEnv* env,
        jobject /* this */) {
    int fd = open("/proc/stat", O_RDONLY);
    if (fd < 0) return -1;

    char buffer[256];
    ssize_t bytesRead = read(fd, buffer, sizeof(buffer) - 1);
    close(fd);

    if (bytesRead <= 0) return -1;
    buffer[bytesRead] = '\0';

    unsigned long long user = 0, nice = 0, sys = 0, idle = 0;
    unsigned long long iowait = 0, irq = 0, softirq = 0;

    int matched = sscanf(buffer, "cpu %llu %llu %llu %llu %llu %llu %llu",
                         &user, &nice, &sys, &idle, &iowait, &irq, &softirq);
    if (matched < 4) return -1;

    if (!hasPrevCpu) {
        prevUser = user; prevNice = nice; prevSys = sys; prevIdle = idle;
        prevIowait = iowait; prevIrq = irq; prevSoftirq = softirq;
        hasPrevCpu = true;
        return 20;
    }

    unsigned long long prevTotal = prevUser + prevNice + prevSys + prevIdle + prevIowait + prevIrq + prevSoftirq;
    unsigned long long currentTotal = user + nice + sys + idle + iowait + irq + softirq;
    unsigned long long prevIdleTotal = prevIdle + prevIowait;
    unsigned long long currentIdleTotal = idle + iowait;

    unsigned long long totalDelta = currentTotal - prevTotal;
    unsigned long long idleDelta = currentIdleTotal - prevIdleTotal;

    prevUser = user; prevNice = nice; prevSys = sys; prevIdle = idle;
    prevIowait = iowait; prevIrq = irq; prevSoftirq = softirq;

    if (totalDelta > 0 && totalDelta >= idleDelta) {
        unsigned long long activeDelta = totalDelta - idleDelta;
        int usage = (int)((activeDelta * 100) / totalDelta);
        return (usage >= 0 && usage <= 100) ? usage : 20;
    }
    return 20;
}

// Zero-allocation kernel thermal zone reading
extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_util_NativeEngineBridge_getNativeSocTemperature(
        JNIEnv* env,
        jobject /* this */) {
    const char* paths[] = {
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/class/thermal/thermal_zone1/temp",
        "/sys/devices/virtual/thermal/thermal_zone0/temp"
    };

    for (const char* path : paths) {
        int fd = open(path, O_RDONLY);
        if (fd >= 0) {
            char buf[32];
            ssize_t n = read(fd, buf, sizeof(buf) - 1);
            close(fd);
            if (n > 0) {
                buf[n] = '\0';
                float raw = (float)atof(buf);
                float temp = (raw > 1000.0f) ? (raw / 1000.0f) : raw;
                if (temp >= 15.0f && temp <= 105.0f) {
                    return temp;
                }
            }
        }
    }
    return -1.0f;
}

// High-precision frame timing tracker using CLOCK_MONOTONIC_RAW
extern "C" JNIEXPORT void JNICALL
Java_com_example_util_NativeEngineBridge_recordFrameTimestamp(
        JNIEnv* env,
        jobject /* this */,
        jlong frameNanos) {
    uint64_t current = (frameNanos > 0) ? (uint64_t)frameNanos : 0;
    if (current == 0) {
        struct timespec ts;
        clock_gettime(CLOCK_MONOTONIC_RAW, &ts);
        current = (uint64_t)ts.tv_sec * 1000000000ULL + (uint64_t)ts.tv_nsec;
    }

    if (s_last_frame_nanos > 0 && current > s_last_frame_nanos) {
        float delta_ms = (float)(current - s_last_frame_nanos) / 1000000.0f;
        if (delta_ms > 0.5f && delta_ms < 500.0f) {
            s_frametime_history[s_frametime_head] = delta_ms;
            s_frametime_head = (s_frametime_head + 1) % FRAMETIME_HISTORY_SIZE;
            if (s_frametime_count < FRAMETIME_HISTORY_SIZE) {
                s_frametime_count++;
            }
        }
    }
    s_last_frame_nanos = current;
}

// Computes [0] = avgFrametimeMs, [1] = 1% Low FPS, [2] = 0.1% Low FPS
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_util_NativeEngineBridge_getHighPrecisionFrametimeStats(
        JNIEnv* env,
        jobject /* this */) {
    jfloatArray result = env->NewFloatArray(3);
    if (!result) return nullptr;

    if (s_frametime_count < 5) {
        float defs[3] = {16.6f, 58.0f, 55.0f};
        env->SetFloatArrayRegion(result, 0, 3, defs);
        return result;
    }

    float sorted[FRAMETIME_HISTORY_SIZE];
    float sum = 0.0f;
    for (int i = 0; i < s_frametime_count; ++i) {
        sorted[i] = s_frametime_history[i];
        sum += sorted[i];
    }
    float avg = sum / (float)s_frametime_count;

    std::sort(sorted, sorted + s_frametime_count);

    // 99th percentile slowest frame corresponds to 1% low FPS
    int idx99 = (int)(s_frametime_count * 0.99f);
    if (idx99 >= s_frametime_count) idx99 = s_frametime_count - 1;
    float worst99Frametime = sorted[idx99];
    float onePercentLowFps = (worst99Frametime > 0.0f) ? (1000.0f / worst99Frametime) : 60.0f;

    // 99.9th percentile slowest frame corresponds to 0.1% low FPS
    int idx999 = s_frametime_count - 1;
    float worst999Frametime = sorted[idx999];
    float zeroPointOneLowFps = (worst999Frametime > 0.0f) ? (1000.0f / worst999Frametime) : 55.0f;

    float output[3] = {avg, onePercentLowFps, zeroPointOneLowFps};
    env->SetFloatArrayRegion(result, 0, 3, output);
    return result;
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

