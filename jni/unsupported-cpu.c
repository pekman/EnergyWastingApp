/*
 * This file is compiled for platforms that are not supported by the CPUburn
 * assembly code.
 */

#include <jni.h>

jboolean Java_fi_aalto_pekman_energywastingapp_components_CPUBurn_isCpuSupported(void)
{
	return JNI_FALSE;
}
