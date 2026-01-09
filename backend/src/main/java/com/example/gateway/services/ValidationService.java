package com.example.gateway.services;

import com.example.gateway.models.CardNetwork;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final Pattern VPA_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");

    /**
     * Validate VPA (Virtual Payment Address) format for UPI
     * Pattern: username@provider
     * Valid: user@paytm, john.doe@okhdfcbank, user_123@phonepe
     * Invalid: user @paytm, @paytm, user@@bank, user@
     */
    public boolean validateVPA(String vpa) {
        if (vpa == null || vpa.trim().isEmpty()) {
            return false;
        }
        return VPA_PATTERN.matcher(vpa).matches();
    }

    /**
     * Validate card number using Luhn Algorithm
     * Steps:
     * 1. Remove spaces and dashes
     * 2. Check length is 13-19
     * 3. Apply Luhn algorithm:
     *    - Start from rightmost digit
     *    - Double every 2nd digit from right (2nd, 4th, 6th from right)
     *    - If doubled > 9, subtract 9
     *    - Sum all digits
     *    - Valid if sum % 10 == 0
     */
    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }

        // Remove spaces and dashes
        String cleanedNumber = cardNumber.replaceAll("[\\s-]", "");

        // Check if all characters are digits
        if (!cleanedNumber.matches("\\d+")) {
            return false;
        }

        // Check length (13-19 digits)
        if (cleanedNumber.length() < 13 || cleanedNumber.length() > 19) {
            return false;
        }

        // Apply Luhn algorithm
        return isValidLuhn(cleanedNumber);
    }

    /**
     * Luhn algorithm implementation
     */
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean isEvenPosition = false;

        // Process digits from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            // Double every 2nd digit from the right (even positions)
            if (isEvenPosition) {
                digit *= 2;
                // If result > 9, subtract 9
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            isEvenPosition = !isEvenPosition;
        }

        // Valid if sum is divisible by 10
        return sum % 10 == 0;
    }

    /**
     * Detect card network from card number
     * Visa: starts with 4
     * Mastercard: starts with 51-55
     * Amex: starts with 34 or 37
     * RuPay: starts with 60, 65, or 81-89
     * Unknown: no match
     */
    public CardNetwork getCardNetwork(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return CardNetwork.UNKNOWN;
        }

        String cleaned = cardNumber.replaceAll("[\\s-]", "");

        if (cleaned.isEmpty()) {
            return CardNetwork.UNKNOWN;
        }

        // Check first digit/digits
        if (cleaned.startsWith("4")) {
            return CardNetwork.VISA;
        } else if (cleaned.startsWith("51") || cleaned.startsWith("52") || 
                   cleaned.startsWith("53") || cleaned.startsWith("54") || 
                   cleaned.startsWith("55")) {
            return CardNetwork.MASTERCARD;
        } else if (cleaned.startsWith("34") || cleaned.startsWith("37")) {
            return CardNetwork.AMEX;
        } else if (cleaned.startsWith("60") || cleaned.startsWith("65")) {
            return CardNetwork.RUPAY;
        } else if (cleaned.length() >= 2) {
            // Check for RuPay 81-89 range
            String firstTwo = cleaned.substring(0, 2);
            try {
                int firstTwoDigits = Integer.parseInt(firstTwo);
                if (firstTwoDigits >= 81 && firstTwoDigits <= 89) {
                    return CardNetwork.RUPAY;
                }
            } catch (NumberFormatException e) {
                // Continue to UNKNOWN
            }
        }

        return CardNetwork.UNKNOWN;
    }

    /**
     * Validate card expiry date
     * Checks if expiry date is not in the past
     */
    public boolean validateCardExpiry(int month, int year) {
        try {
            // Validate month is 1-12
            if (month < 1 || month > 12) {
                return false;
            }

            // Create expiry date as last day of the month
            YearMonth expiry = YearMonth.of(year, month);
            YearMonth now = YearMonth.now();

            // Valid if expiry >= current month/year
            return !expiry.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse and validate expiry date from string inputs
     * Handles both 2-digit years (e.g., "25" -> 2025) and 4-digit years (e.g., "2025")
     */
    public boolean validateExpiryInput(String monthStr, String yearStr) {
        try {
            // Parse month
            int month = Integer.parseInt(monthStr);
            if (month < 1 || month > 12) {
                return false;
            }

            // Parse year
            int year = Integer.parseInt(yearStr);

            // Handle 2-digit year format (convert to 4-digit)
            if (year < 100) {
                year += 2000;
            }

            // Validate expiry
            return validateCardExpiry(month, year);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Helper method to extract last 4 digits of card number
     */
    public String getCardLast4(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }

        String cleaned = cardNumber.replaceAll("[\\s-]", "");

        if (cleaned.length() < 4) {
            return null;
        }

        return cleaned.substring(cleaned.length() - 4);
    }
}
