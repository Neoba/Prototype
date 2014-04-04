#include<jni.h>
#include<cstdlib>
#include<google/vcencoder.h>
#include<google/vcdecoder.h>
#include"openvcdiffjavajni.h"
#include<iostream>


JNIEXPORT jbyteArray JNICALL Java_openvcdiffjava_Openvcdiffjava_vcdiffEncode
(JNIEnv *env, jobject thisObj, jstring jdictionary, jstring jtarget) {
    const char *dictionary = env->GetStringUTFChars(jdictionary, NULL);
    if (NULL == dictionary) {
        return NULL;
    }
    const char *target = env->GetStringUTFChars(jtarget, NULL);
    if (NULL == target) {
        return NULL;
    }

    std::string delta;

    open_vcdiff::VCDiffEncoder encoder(dictionary, strlen(dictionary));
    encoder.SetFormatFlags(open_vcdiff::VCD_FORMAT_INTERLEAVED);
    encoder.Encode(target, strlen(target), &delta);

    env->ReleaseStringUTFChars(jdictionary, dictionary);
    env->ReleaseStringUTFChars(jtarget, target);
    
    jbyteArray jdelta=env->NewByteArray(delta.size());
    env->SetByteArrayRegion(jdelta,0,delta.size(),(jbyte*)delta.c_str());
    return jdelta;
    
}

JNIEXPORT jstring JNICALL Java_openvcdiffjava_Openvcdiffjava_vcdiffDecode
(JNIEnv *env, jobject thisObj, jstring jdictionary, jbyteArray jdelta){
    const char *dictionary = env->GetStringUTFChars(jdictionary, NULL);
    if (NULL == dictionary) {
        return NULL;
    }
    int length=env->GetArrayLength((jarray)jdelta);
    
    //not cool
    std::string delta((char *)env->GetByteArrayElements(jdelta,NULL),length);
    std::string target;
        
    open_vcdiff::VCDiffDecoder decoder;
    decoder.Decode(dictionary, strlen(dictionary), delta, &target);
    
    return env->NewStringUTF(target.c_str());
}

