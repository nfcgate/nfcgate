#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <pthread.h>
#include <unistd.h>

#include "nfcd.h"

#define LOGIPC(...) __android_log_print(ANDROID_LOG_INFO, "NFCIPC", __VA_ARGS__ )

pthread_t thread;
void *ipc_run(void *n);

void ipc_init() {
    LOGIPC("ipc_init");
    pthread_create(&thread, NULL, ipc_run, NULL);
}


void *ipc_run(void *n) {
    LOGIPC("ipc_run");
    return NULL;
}
