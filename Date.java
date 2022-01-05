package uk.ac.ed.inf;

/**
 * Creates a Date object
 */
public class Date {
    private String year;
    private String month;
    private String day;

    /**
     * Class constructor instantiating given date
     * @param year	year of the date to create.
     * @param month month of the date to create.
     * @param day	day of the date to create.
     */
    protected Date(String day, String month, String year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    /**
     * @return year of the specific date.
     */
    protected String getYear() {
        return this.year;
    }
    /**
     * @return month of the specific date.
     */
    protected String getMonth() {
        return this.month;
    }
    /**
     * @return day of the specific date.
     */
    protected String getDay() {
        return this.day;
    }
}
