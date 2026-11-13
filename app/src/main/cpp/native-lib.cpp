
#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_nexus_ps2android_MainActivity_getEngineStatus(
    JNIEnv* env,
    jobject /* this */
) {
    std::string status =
        "NEXUS ENGINE 0.1\n"
        "Native C++ initialized\n"
        "PS2 compatibility: Not implemented yet";

    return env->NewStringUTF(status.c_str());
}
