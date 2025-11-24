package de.tu_darmstadt.seemoo.nfcgate.util;

/**
 * Utility class for classifying APDU command types based on hex data
 * Ported from Python parsing logic
 */
public class ApduClassifier {

    /**
     * Classifies an APDU hex string into a command type
     * @param hexData The hex string of the APDU (without spaces)
     * @param isCard True if data is from card (response), false if from reader (command)
     * @param prevCommandType The previous command type for context
     * @return The classified command type string
     */
    public static String classifyApdu(String hexData, boolean isCard, String prevCommandType) {
        if (hexData == null || hexData.isEmpty()) {
            return "UNKNOWN";
        }

        // Convert to lowercase for easier matching
        String data = hexData.toLowerCase();

        // If it's a response (from card)
        if (isCard) {
            return classifyResponse(data, prevCommandType);
        }
        // If it's a command (from reader)
        else {
            return classifyCommand(data);
        }
    }

    /**
     * Classifies a command APDU
     */
    private static String classifyCommand(String data) {
        // Status only (CONTROL FLOW response)
        if (data.equals("9000") || (data.length() == 4 && data.endsWith("9000"))) {
            return "CONTROL FLOW response";
        }

        // Need at least 6 characters for command classification
        if (data.length() < 6) {
            return "UNKNOWN";
        }

        // SELECT
        if (data.startsWith("00a40400")) {
            return "SELECT";
        }

        // SPAKE2+ REQUEST
        if (data.startsWith("80300000")) {
            return "SPAKE2+ REQUEST";
        }

        // SPAKE2+ VERIFY
        if (data.startsWith("80320000")) {
            return "SPAKE2+ VERIFY";
        }

        // AUTH0 and AUTH1
        String cla = data.substring(0, 2);
        String ins = data.substring(2, 4);

        if (cla.equals("80") && ins.equals("80")) {
            return "AUTH0";
        }
        if (cla.equals("80") && ins.equals("81")) {
            return "AUTH1";
        }

        // WRITE DATA
        if (data.startsWith("84d4")) {
            return "WRITE DATA";
        }

        // GET DATA
        if (data.startsWith("84ca")) {
            return "GET DATA";
        }

        // GET RESPONSE
        if (data.startsWith("84c0")) {
            return "GET RESPONSE";
        }

        // EXCHANGE
        if (data.startsWith("84c9")) {
            return "EXCHANGE";
        }

        // CONTROL FLOW
        if (data.startsWith("803c")) {
            return "CONTROL FLOW";
        }

        return "UNKNOWN";
    }

    /**
     * Classifies a response APDU
     */
    private static String classifyResponse(String data, String prevCommandType) {
        // Status only (CONTROL FLOW response)
        if (data.equals("9000") || (data.length() == 4 && data.endsWith("9000"))) {
            return "CONTROL FLOW response";
        }

        // Check if it ends with 9000 or 61xx (success status)
        boolean hasSuccessStatus = data.endsWith("9000") ||
                                   (data.length() >= 4 && data.substring(data.length() - 4, data.length() - 2).equals("61"));

        if (hasSuccessStatus) {
            // SELECT response
            if (data.startsWith("5a") || data.startsWith("5c") || data.startsWith("d4")) {
                return "SELECT response";
            }

            // SPAKE2+ REQUEST response (50h, 5Fh)
            if (data.startsWith("50") || data.startsWith("5f")) {
                return "SPAKE2+ REQUEST response";
            }

            // SPAKE2+ VERIFY response (58h)
            if (data.startsWith("58")) {
                return "SPAKE2+ VERIFY response";
            }

            // AUTH0 response (86h or 9Dh tags)
            if (data.startsWith("86") || data.startsWith("9d")) {
                return "AUTH0 response";
            }

            // AUTH1 response (9Eh tag)
            if (data.startsWith("9e")) {
                return "AUTH1 response";
            }

            // Use previous command type for context
            if (prevCommandType != null && !prevCommandType.isEmpty()) {
                if (prevCommandType.contains("AUTH0")) {
                    return "AUTH0 response";
                } else if (prevCommandType.contains("AUTH1")) {
                    return "AUTH1 response";
                } else if (prevCommandType.contains("WRITE DATA")) {
                    return "WRITE DATA response";
                } else if (prevCommandType.contains("SPAKE2+ REQUEST")) {
                    return "SPAKE2+ REQUEST response";
                } else if (prevCommandType.contains("SPAKE2+ VERIFY")) {
                    return "SPAKE2+ VERIFY response";
                } else if (prevCommandType.contains("GET RESPONSE")) {
                    return "GET RESPONSE response";
                } else if (prevCommandType.contains("GET DATA")) {
                    return "GET DATA response";
                } else if (prevCommandType.contains("EXCHANGE")) {
                    return "EXCHANGE response";
                } else if (prevCommandType.contains("CONTROL FLOW")) {
                    return "CONTROL FLOW response";
                }
            }

            return "UNKNOWN response";
        }

        return "UNKNOWN";
    }

    /**
     * Gets a shortened version of the command type for display
     */
    public static String getShortCommandType(String commandType) {
        if (commandType == null || commandType.equals("UNKNOWN")) {
            return "";
        }
        // Remove " response" suffix for cleaner display
        return commandType.replace(" response", "");
    }
}
