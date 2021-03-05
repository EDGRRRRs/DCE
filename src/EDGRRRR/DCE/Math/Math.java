package EDGRRRR.DCE.Math;

public class Math {

    /**
     * Calculates the number of ticks from the seconds provided
     *
     * @param seconds - The number of seconds
     * @return int - Ticks
     */
    public static int getTicks(int seconds) {
        return seconds * 20;
    }

    /**
     * Calculates the number of ticks from the milliseconds provided
     *
     * @param milliseconds - The number of milliseconds
     * @return int - Ticks
     */
    public static int getTicks(long milliseconds) {
        return getTicks((int) milliseconds / 1000);
    }

    /**
     * A rounding function for rounding double
     *
     * @param amount         - The amount to round
     * @param roundingDigits - The number of digits to round to
     * @return double - The rounded number
     */
    public static double round(double amount, int roundingDigits) {
        // Rounds the amount to the number of digits specified
        // Does this by 10**digits (100 or 10**2 = 2 digits)
        try {
            double roundAmount = java.lang.Math.pow(10, roundingDigits);
            return java.lang.Math.round(amount * roundAmount) / roundAmount;
        } catch (Exception e) {
            return amount;
        }
    }

    /**
     * A function for extracting a double from a String
     * will return null if an error occurs (such as the string not containing a double)
     *
     * @param arg - A string to convert to a double
     * @return double - A potentially converted double
     */
    public static double getDouble(String arg) {
        // Instantiate amount
        double amount;

        // Try to parse the double
        // Catch the error and set to null
        try {
            amount = Double.parseDouble(arg);
        } catch (Exception e) {
            amount = 0.0;
        }

        return amount;
    }

    public static int getInt(String arg) {
        return (int) Math.getDouble(arg);
    }


    /**
     * Gets the level of inflation based on the parameters supplied
     * Just returns getScale(default, actual)
     * @param defaultMarketSize   - The base quantity of materials in the market
     * @param actualMarketSize - The actual current quantity of materials in the market
     * @return double - The level of inflation
     */
    public static double getInflation(double defaultMarketSize, double actualMarketSize) {
        return getScale(defaultMarketSize, actualMarketSize);
    }

    /**
     * Calculates the price of an amount of items
     * @param baseQuantity - The base quantity of the item
     * @param currentQuantity - The current quantity of the item
     * @param defaultMarketSize - The default market size
     * @param marketSize - The current market size
     * @param amount - The amount of the item to buy
     * @param scale - The price scaling (e.g. tax)
     * @param purchase - Whether this is a purchase or a sale.
     * @return double
     */
    public static double calculatePrice(double baseQuantity, double currentQuantity, double defaultMarketSize, double marketSize, double amount, double scale, boolean purchase) {
        double value = 0;

        // Loop for amount
        // Get the price and add it to the value
        // if purchase = true
        // remove 1 stock to simulate decrease
        // if purchase = false
        // add 1 stock to simulate increase
        for (int i = 1; i <= amount; i++) {
            value += getPrice(baseQuantity, currentQuantity, scale, getInflation(defaultMarketSize, marketSize));
            if (purchase) {
                currentQuantity -= 1;
                marketSize -= 1;
            } else {
                currentQuantity += 1;
                marketSize += 1;
            }
        }

        return value;
    }

    /**
     * Gets the price of a product based on the parameters supplied
     * @param baseQuantity - The base quantity of items in the market
     * @param currentQuantity - The current quantity of items in the market
     * @param scale - The scaling to apply to the price
     * @param inflation - The inflation of the market
     * @return double
     */
    public static double getPrice(double baseQuantity, double currentQuantity, double scale, double inflation) {
        // Price breakdown
        // Prices were balanced in data.csv
        // Prices are determined by quantity
        // Price = $1 @ 1000000 (1 million) items
        // Price = $2 @ 500000 (5 hundred-thousand) items
        // Price is then scaled - such as the addition of tax (20% by default)
        // Price is then scaled for inflation
        // Inflation works by calculating the default total items and dividing it by the new total items
        // This results in an increase in price when there are less items in the market than default
        // Or a decrease in price when there are more items in the market than default
        return (getScale(baseQuantity, currentQuantity)) * scale * inflation;
    }

    /**
     * Returns the scale of a number compared to it's base value
     * base / current
     * @param baseQuantity - The base quantity of items
     * @param currentQuantity - The current quantity of items
     * @return double
     */
    public static double getScale(double baseQuantity, double currentQuantity) {
        return baseQuantity / currentQuantity;
    }
}
