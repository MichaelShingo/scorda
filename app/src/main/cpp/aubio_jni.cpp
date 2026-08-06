#include <jni.h>
#include <string>
#include <vector>
#include "aubio.h"

extern "C" {

struct AubioContext {
    aubio_pitch_t *pitch_obj;
    fvec_t *in_vec;
    fvec_t *out_vec;
};

JNIEXPORT jlong JNICALL Java_com_example_scorda_audio_tuner_AubioPitchDetector_nativeInit(JNIEnv *env, jobject thiz, jint samplerate, jint buf_size, jint hop_size) {
    AubioContext *ctx = new AubioContext();
    // Using "yin" for better stability on musical notes
    ctx->pitch_obj = new_aubio_pitch("yin", (uint_t)buf_size, (uint_t)hop_size, (uint_t)samplerate);
    // Explicitly set silence threshold and units
    // Switched to "Hz" to allow high-precision calculation in Kotlin
    aubio_pitch_set_unit(ctx->pitch_obj, "Hz");
    aubio_pitch_set_silence(ctx->pitch_obj, -60.0f);

    ctx->in_vec = new_fvec((uint_t)hop_size);
    ctx->out_vec = new_fvec(1);
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT jfloatArray JNICALL Java_com_example_scorda_audio_tuner_AubioPitchDetector_nativeProcess(JNIEnv *env, jobject thiz, jlong handle, jfloatArray audio_data) {
    AubioContext *ctx = reinterpret_cast<AubioContext *>(handle);
    if (!ctx) return nullptr;

    jsize len = env->GetArrayLength(audio_data);
    jfloat *body = env->GetFloatArrayElements(audio_data, 0);

    // Copy data into fvec
    for (int i = 0; i < len && i < ctx->in_vec->length; i++) {
        ctx->in_vec->data[i] = body[i];
    }

    // Process
    aubio_pitch_do(ctx->pitch_obj, ctx->in_vec, ctx->out_vec);

    float midi = ctx->out_vec->data[0];
    float confidence = aubio_pitch_get_confidence(ctx->pitch_obj);

    env->ReleaseFloatArrayElements(audio_data, body, JNI_ABORT);

    jfloatArray result = env->NewFloatArray(2);
    float res_body[2] = {midi, confidence};
    env->SetFloatArrayRegion(result, 0, 2, res_body);

    return result;
}

JNIEXPORT void JNICALL Java_com_example_scorda_audio_tuner_AubioPitchDetector_nativeSetSilence(JNIEnv *env, jobject thiz, jlong handle, jfloat silence) {
    AubioContext *ctx = reinterpret_cast<AubioContext *>(handle);
    if (ctx && ctx->pitch_obj) {
        aubio_pitch_set_silence(ctx->pitch_obj, silence);
    }
}

JNIEXPORT void JNICALL Java_com_example_scorda_audio_tuner_AubioPitchDetector_nativeSetTolerance(JNIEnv *env, jobject thiz, jlong handle, jfloat tolerance) {
    AubioContext *ctx = reinterpret_cast<AubioContext *>(handle);
    if (ctx && ctx->pitch_obj) {
        aubio_pitch_set_tolerance(ctx->pitch_obj, tolerance);
    }
}

JNIEXPORT void JNICALL Java_com_example_scorda_audio_tuner_AubioPitchDetector_nativeCleanup(JNIEnv *env, jobject thiz, jlong handle) {
    AubioContext *ctx = reinterpret_cast<AubioContext *>(handle);
    if (ctx) {
        if (ctx->pitch_obj) del_aubio_pitch(ctx->pitch_obj);
        if (ctx->in_vec) del_fvec(ctx->in_vec);
        if (ctx->out_vec) del_fvec(ctx->out_vec);
        delete ctx;
    }
}

}
