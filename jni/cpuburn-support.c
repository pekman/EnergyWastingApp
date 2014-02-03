#include <jni.h>
#include <unistd.h>
#include <sys/prctl.h>
#include <dirent.h>
#include <ctype.h>
#include "cpu-features.h"

extern void cpuburn(void);

// forks cpuburn process and returns its PID
jint Java_fi_aalto_pekman_energywastingapp_components_CPUBurn_startCPUBurn(void)
{
	pid_t pid = fork();
	if (pid == 0) {
		// this is the child process

		// make kernel kill this child process when the parent process dies
		prctl(PR_SET_PDEATHSIG, SIGKILL);

		cpuburn(); // this should never return
		_exit(0);
	}
	return pid;
}

// Returns the number of processor cores.
// If there is a problem determining the number of cores, returns -1.
jint Java_fi_aalto_pekman_energywastingapp_components_CPUBurn_getNumCores(void)
{
	DIR *d;
	struct dirent *entry;
	int num_cores = 0;

	d = opendir("/sys/devices/system/cpu");
	if (!d)
		return -1;

	// count filenames of the form cpu[0-9]+
	while ((entry = readdir(d)) != NULL) {
		char *name = entry->d_name;
		if (name[0] == 'c' && name[1] == 'p' && name[2] == 'u' && isdigit(name[3])) {
			char *c = &name[4];
			while (isdigit(*c))
				c++;
			if (*c == '\0')
				num_cores++;
		}
	}

	closedir(d);

	if (num_cores == 0)
		return -1;
	else
		return num_cores;
}

// these signals are not defined in Java
jint Java_fi_aalto_pekman_energywastingapp_components_CPUBurn_getSIGSTOP(void)
{
	return SIGSTOP;
}

jint Java_fi_aalto_pekman_energywastingapp_components_CPUBurn_getSIGCONT(void)
{
	return SIGCONT;
}

// returns true if the processor supports ARM NEON instructions
jboolean Java_fi_aalto_pekman_energywastingapp_components_CPUBurn_isCpuSupported(void)
{
	return (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM) &&
			(android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON);
}
