
/**
 * types of a single ipc message
 */
enum ipctype {
    ENABLE,
    DISABLE,
    CONFIGURE,
    STATUS
};

/**
 * ipc packet that is transmitted via the socket
 */
typedef struct {
    ipctype type;
    uint8_t atqa;
    uint8_t sak;
    uint8_t hist;
    uint8_t uid[64];
    uint8_t uid_len;
} ipcpacket;

/**
 * path to the domain socket
 */
#define IPC_SOCK_DIR "/data/data/tud.seemuh.nfcgate/ipc"
#define IPC_SOCK_FILE IPC_SOCK_DIR "/sock"
