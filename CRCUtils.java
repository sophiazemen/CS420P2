public class CRCUtils {
    public static String calculateCRC(String data, String divisor) {
        String dividend = data + "0".repeat(divisor.length() - 1);
        String remainder = dividend.substring(0, divisor.length());

        for (int i = 0; i <= dividend.length() - divisor.length(); i++) {
            if (remainder.charAt(0) == '1') {
                remainder = xor(remainder, divisor);
            }
            if (i + divisor.length() < dividend.length()) {
                remainder += dividend.charAt(i + divisor.length());
            }
            remainder = remainder.substring(1);
        }

        return remainder;
    }

    private static String xor(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            result.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
        }
        return result.toString();
    }
}
