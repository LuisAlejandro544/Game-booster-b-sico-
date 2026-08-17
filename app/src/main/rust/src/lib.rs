use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jint, jlong, jstring};

/// Returns the Rust Core version string to the Kotlin application.
#[no_mangle]
pub extern "system" fn Java_com_example_util_RustCoreBridge_getRustCoreVersion(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let output = env
        .new_string("Rust Core v0.1.0 (Zero-Cost Memory & Latency Engine)")
        .expect("Couldn't create java string!");
    output.into_raw()
}

/// Calculates memory pressure score in pure Rust (0-100 scale).
#[no_mangle]
pub extern "system" fn Java_com_example_util_RustCoreBridge_calculateMemoryPressure(
    _env: JNIEnv,
    _class: JClass,
    used_mb: jlong,
    total_mb: jlong,
) -> jint {
    if total_mb <= 0 {
        return 0;
    }
    let ratio = (used_mb as f64) / (total_mb as f64);
    let pressure = (ratio * 100.0).round() as jint;
    pressure.clamp(0, 100)
}

/// Computes dynamic jitter score from recent latency measurements.
#[no_mangle]
pub extern "system" fn Java_com_example_util_RustCoreBridge_calculateJitter(
    _env: JNIEnv,
    _class: JClass,
    ping_a: jint,
    ping_b: jint,
) -> jint {
    (ping_a - ping_b).abs()
}
