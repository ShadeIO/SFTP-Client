package sftp;

public final class IpValidator {
    private IpValidator() {}

    public static boolean isValidIPv4(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String part : parts) {
            if (part.isEmpty() || (part.length() > 1 && part.startsWith("0"))) return false;
            try {
                int v = Integer.parseInt(part);
                if (v < 0 || v > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
