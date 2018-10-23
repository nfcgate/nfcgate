package tud.seemuh.nfcgate.db.export;

import java.io.DataOutputStream;
import java.io.IOException;

public interface PcapWriteableObject {
    int write(DataOutputStream out) throws IOException;
}
