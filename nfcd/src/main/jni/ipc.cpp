#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <pthread.h>
#include <unistd.h>
#include <pwd.h>

#include "nfcd.h"
#include "ipc.h"

#define LOGIPC(...) __android_log_print(ANDROID_LOG_INFO, "NFCIPC-S", __VA_ARGS__ )

pthread_t thread;
void *ipc_run(void *n);

// handle a connected client and process commands
static void handleClient(int sock) {
    while(1) {
        ipcpacket p;
        int n = recv(sock, &p, sizeof(ipcpacket), 0);
        if (n <= 0) break;

        switch(p.type) {
            case ipctype::ENABLE:
                LOGIPC("ENABLE");
                patchEnabled = true;
                uploadPatchConfig();
            break;
            case ipctype::DISABLE:
                LOGIPC("DISABLE");
                patchEnabled = false;
                uploadOriginalConfig();
            break;
            case ipctype::CONFIGURE:
                LOGIPC("CONFIGURE");
                patchValues.atqa = p.atqa;
                patchValues.sak = p.sak;
                patchValues.hist = p.hist;
                patchValues.uid_len = p.uid_len;
                if(p.uid_len > sizeof(patchValues.uid)) {
                    LOGIPC("E invalid ipc packet");
                }
                memcpy(patchValues.uid, p.uid, p.uid_len);
            break;
        }
    }
    if(patchEnabled) {
        LOGIPC("connection lost, disabling patch");
        patchEnabled = false;
        uploadOriginalConfig();
    }
/*
    if (send(sock, str, n, 0) < 0) {
        LOGIPC("E send");
    }
    */
}

// this is called in root-context inside the cygote process.
// to allow the nfc daemon to set up the socket, create an directory with write access for this user
void ipc_prepare() {
    //LOGIPC("ipc_prepare");
    if(access(IPC_SOCK_DIR, F_OK) == -1) {
        mkdir(IPC_SOCK_DIR, 0777); // TODO permissions
        struct passwd usr = *getpwnam("nfc");
        chown(IPC_SOCK_DIR, usr.pw_uid, usr.pw_gid);

    }
}

void ipc_init() {
    //LOGIPC("ipc_init");
    pthread_create(&thread, NULL, ipc_run, NULL);
}


void *ipc_run(void *n) {
    LOGIPC("ipc_run");

    int s = socket(AF_UNIX, SOCK_STREAM, 0);
    if (s == -1) {
        LOGIPC("E socket: %s", strerror(errno));
        return NULL;
    }

    if(access(IPC_SOCK_FILE, F_OK) != -1) {
        unlink(IPC_SOCK_FILE);
    }

    struct sockaddr_un sa;
    sa.sun_family = AF_LOCAL;
    strcpy(sa.sun_path, IPC_SOCK_FILE);
    int len = strlen(sa.sun_path) + sizeof(sa.sun_family);
    if (bind(s, (struct sockaddr *)&sa, len) == -1) {
        LOGIPC("E bind: %s", strerror(errno));
        return NULL;
    }
    chmod(IPC_SOCK_FILE, 0777); // TODO permissions

    if (listen(s, 0) == -1) {
        LOGIPC("E listen: %s", strerror(errno));
        return NULL;
    }

    while(1) {
        int client = accept(s, NULL, 0);
        if (client == -1) {
            LOGIPC("E accept: %s", strerror(errno));
            return NULL;
        }

        LOGIPC("Connected.\n");

        handleClient(client);
    }

    return NULL;

}
