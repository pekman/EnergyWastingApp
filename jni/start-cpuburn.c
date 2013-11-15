#include <jni.h>
#include <unistd.h>

extern void cpuburn(void);

// forks cpuburn process and returns its PID
jint Java_com_github_robinbj86_energywastingapp_components_CPUBurn_startCPUBurn(void)
{
	pid_t pid = fork();
	if (pid == 0) {
		// this is the child process
		cpuburn(); // this should never return
		_exit(0);
	}
	return pid;
}
