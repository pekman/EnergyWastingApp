#include <jni.h>
#include <unistd.h>
#include <sys/prctl.h>

extern void cpuburn(void);

// forks cpuburn process and returns its PID
jint Java_com_github_robinbj86_energywastingapp_components_CPUBurn_startCPUBurn(void)
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
