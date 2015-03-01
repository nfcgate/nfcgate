
enum ipctype {
    ENABLE,
    DISABLE,
    CONFIGURE,
    STATUS
};
typedef struct {
    ipctype type;
    uint8_t atqa;
    uint8_t sak;
    uint8_t hist;
    uint8_t uid[64];
    uint8_t uid_len;
} ipcpacket;

#define IPC_SOCK_DIR "/data/data/tud.seemuh.nfcgate/ipc"
#define IPC_SOCK_FILE IPC_SOCK_DIR "/sock"
