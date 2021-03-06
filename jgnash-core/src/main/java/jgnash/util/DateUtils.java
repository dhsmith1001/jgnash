/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2013 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Static methods to make working with dates a bit easier
 * 
 * @author Craig Cavanaugh
 * @author Vincent Frison
 */

@SuppressWarnings("MagicConstant")
public class DateUtils {

    private static final int DAYS_PER_YEAR = 365;

    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

    private static final long AVERAGE_MILLISECONDS_PER_MONTH = (long) (365f * 24f * 60f * 60f * 1000f / 12f);

    private static String[] localizedMonthNames;

    private static Locale lastLocale;

    /**
     * Local object pool for calendar objects because they are expensive to
     * create
     */
    private static final ObjectPool<GregorianCalendar> calendarPool = new ObjectPool<GregorianCalendar>() {

        @Override
        public GregorianCalendar createInstance() {
            return new GregorianCalendar();
        }
    };

    private static final Pattern MONTH_PATTERN = Pattern.compile("M{1,2}");

    private static final Pattern DAY_PATTERN = Pattern.compile("d{1,2}");

    private DateUtils() {
    }

    private static void updateMonthNames() {
        if (lastLocale != Locale.getDefault()) {

            DateFormatSymbols symbols = new DateFormatSymbols();
            localizedMonthNames = symbols.getMonths();

            lastLocale = Locale.getDefault();
        }
    }

    public static String getNameOfMonth(final Date date) {
        updateMonthNames();

        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            return localizedMonthNames[c.get(Calendar.MONTH)];
        } finally {
            calendarPool.put(c);
        }
    }

    public static Date addDay(final Date date) {
        return addDays(date, 1);
    }

    /**
     * Returns a date a given number of days after the requested date
     * 
     * @param date
     *            start date
     * @param days
     *            the number of days to add
     * @return new date
     */
    public static Date addDays(final Date date, final int days) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.add(Calendar.DATE, days);
            return c.getTime();
        } finally {
            calendarPool.put(c);
        }
    }

    public static Date addMonth(final Date date) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.add(Calendar.MONTH, 1);
            return c.getTime();
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Determines is Date d1 occurs after Date d2. The specified dates are
     * inclusive.
     * 
     * @param d1
     *            date 1
     * @param d2
     *            date 2
     * @return true if d1 is after d2
     */
    public static boolean after(final Date d1, final Date d2) {
        return before(d2, d1, true);
    }

    /**
     * Determines if Date d1 occurs after Date d2.
     * 
     * @param d1
     *            date 1
     * @param d2
     *            date 2
     * @param inclusive
     *            <code>true</code> is comparison is inclusive
     * @return <code>true</code> if d1 occurs after d2
     */
    public static boolean after(final Date d1, final Date d2, final boolean inclusive) {
        return before(d2, d1, inclusive);
    }

    /**
     * Determines if Date d1 occurs before Date d2. The specified dates are
     * inclusive
     * 
     * @param d1
     *            date 1
     * @param d2
     *            date 2
     * @return true if d1 is before d2 or the same date
     */
    public static boolean before(final Date d1, final Date d2) {
        return before(d1, d2, true);
    }

    /**
     * Determines if Date d1 occurs before Date d2.
     * 
     * @param d1
     *            date 1
     * @param d2
     *            date 2
     * @param inclusive
     *            <code>true</code> is comparison is inclusive
     * @return <code>true</code> if d1 occurs before d2
     */
    public static boolean before(final Date d1, final Date d2, final boolean inclusive) {
        if (inclusive) {
            return d1.getTime() <= d2.getTime();
        }

        return d1.getTime() < d2.getTime();
    }

    /**
     * Returns the current calendar year
     * 
     * @return the year
     */
    public static int getCurrentYear() {
        return getYear(new Date());
    }

    /**
     * Returns the calendar year given a date
     * 
     * @param date
     *            date
     * @return the year
     */
    public static int getYear(final Date date) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);

            return c.get(Calendar.YEAR);
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns the date given a year and a day of the year.
     * 
     * The first day of the year has value 1.
     * 
     * @param year
     *            the year
     * @param dayOfYear
     *            day of the year
     * @return the date
     */
    public static Date getDateOfTheYear(final int year, final int dayOfYear) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(trimDate(c.getTime()));
            c.set(Calendar.YEAR, year);
            c.set(Calendar.DAY_OF_YEAR, dayOfYear);

            return c.getTime();
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns the numerical day of the week given a date
     * 
     * @param date
     *            the base date to work from
     * @return the day of the week
     */
    public static int getDayOfTheWeek(final Date date) {
        return getDayOfX(date, Calendar.DAY_OF_WEEK);
    }

    /**
     * Returns the numerical day of the month given a date
     * 
     * @param date
     *            the base date to work from
     * @return the day of the month
     */
    public static int getDayOfTheMonth(final Date date) {
        return getDayOfX(date, Calendar.DAY_OF_MONTH);
    }

    /**
     * Returns the numerical day of the year given a date
     * 
     * @param date
     *            the base date to work from
     * @return the week of the year
     */
    public static int getDayOfTheYear(final Date date) {
        return getDayOfX(date, Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns a numerical value given a date and Calendar field
     * 
     * @param date
     *            the base date to work from
     * @param field
     *            Calendar field
     * @return numerical value
     */
    private static int getDayOfX(final Date date, final int field) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            return c.get(field);
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns the number of days in the month given a date
     * 
     * @param date
     *            date (Month)
     * @return the number of days in the month
     */
    public static int getDaysInMonth(final Date date) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMaximum(Calendar.DAY_OF_MONTH));

            return c.get(Calendar.DAY_OF_MONTH);
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns the number of days in the year
     * 
     * @param year
     *            calendar year
     * @return the number of days in the year
     */
    private static int getDaysInYear(final int year) {
        if (isLeapYear(year)) {
            return DAYS_PER_YEAR + 1;
        }
        return DAYS_PER_YEAR;
    }

    /**
     * Determines the difference in days of two dates
     * 
     * @param startDate
     *            start date
     * @param endDate
     *            end date
     * @return number of days
     */
    public static int getDifferenceInDays(final Date startDate, final Date endDate) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(trimDate(startDate));
            final long startMilli = c.getTimeInMillis() + c.getTimeZone().getOffset(c.getTimeInMillis());

            c.setTime(trimDate(endDate));
            final long endMilli = c.getTimeInMillis() + c.getTimeZone().getOffset(c.getTimeInMillis());

            return (int) ((endMilli - startMilli) / MILLISECONDS_PER_DAY);
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Determines the difference in months of two dates
     * 
     * @param startDate
     *            start date
     * @param endDate
     *            end date
     * @return number of days
     */
    public static float getDifferenceInMonths(final Date startDate, final Date endDate) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(trimDate(startDate));
            final long startMilli = c.getTimeInMillis() + c.getTimeZone().getOffset(c.getTimeInMillis());

            c.setTime(trimDate(endDate));
            final long endMilli = c.getTimeInMillis() + c.getTimeZone().getOffset(c.getTimeInMillis());

            return ((float) endMilli - (float) startMilli) / AVERAGE_MILLISECONDS_PER_MONTH;
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * @param year
     *            The year to generate the array for
     * @return The array of dates
     */
    public static Date[] getFirstDayBiWeekly(final int year) {
        List<Date> dates = new ArrayList<>();

        Date[] allWeeks = getFirstDayWeekly(year);

        for (int i = 0; i < allWeeks.length; i += 2) {
            dates.add(allWeeks[i]);
        }

        return dates.toArray(new Date[dates.size()]);
    }

    /**
     * Generates an array of dates starting on the first day of every month in
     * the specified year
     * 
     * @param year
     *            The year to generate the array for
     * @return The array of dates
     */
    public static Date[] getFirstDayMonthly(final int year) {
        Date[] list = new Date[12];
        for (int i = 0; i < 12; i++) {
            list[i] = getFirstDayOfTheMonth(i, year);
        }
        return list;
    }

    /**
     * Returns a leveled date representing the first day of the month based on a
     * specified date.
     * 
     * @param date
     *            the base date to work from
     * @return The last day of the month and year specified
     */
    public static Date getFirstDayOfTheMonth(final Date date) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMinimum(Calendar.DAY_OF_MONTH));

            return trimDate(c.getTime());
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns a leveled date representing the first day of the month
     * 
     * @param month
     *            The month (index starts at 0)
     * @param year
     *            The year (index starts at 1)
     * @return The last day of the month and year specified
     */
    private static Date getFirstDayOfTheMonth(final int month, final int year) {
        assert month >= 0 && month <= 11;

        GregorianCalendar c = calendarPool.take();

        try {
            c.set(year, month, 15);
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMinimum(Calendar.DAY_OF_MONTH));

            return trimDate(c.getTime());
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns an array of the starting date of each quarter in a year
     * 
     * @param year
     *            The year to generate the array for
     * @return The array of quarter bound dates
     */
    public static Date[] getFirstDayQuarterly(final int year) {
        GregorianCalendar c = calendarPool.take();

        try {
            Date[] bounds = new Date[4];

            c.set(year, Calendar.JANUARY, 1, 0, 0, 0);
            bounds[0] = c.getTime();

            c.set(year, Calendar.APRIL, 1, 0, 0, 0);
            bounds[1] = c.getTime();

            c.set(year, Calendar.JULY, 1, 0, 0, 0);
            bounds[2] = c.getTime();

            c.set(year, Calendar.OCTOBER, 1, 0, 0, 0);
            bounds[3] = c.getTime();

            return bounds;
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns an array of Dates starting with the first day of each week of the
     * year.
     * 
     * @param year
     *            The year to generate the array for
     * @return The array of dates
     * @see <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO_8601</a>
     * 
     */
    public static Date[] getFirstDayWeekly(final int year) {
        GregorianCalendar cal = calendarPool.take();
        GregorianCalendar testCal = calendarPool.take();

        try {
            List<Date> dates = new ArrayList<>();

            // level the date
            cal.setTime(trimDate(cal.getTime()));

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, 0);
            cal.set(Calendar.WEEK_OF_YEAR, 1);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            for (int i = 0; i < 53; i++) {

                testCal.setTime(cal.getTime());
                testCal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);

                if (testCal.get(Calendar.YEAR) == year) {
                    dates.add(cal.getTime());
                }

                cal.add(Calendar.DATE, 7); // add 7 days
            }

            return dates.toArray(new Date[dates.size()]);
        } finally {
            calendarPool.put(cal);
            calendarPool.put(testCal);
        }
    }

    /**
     * Returns an array of every day in a given year
     * 
     * @param year
     *            The year to generate the array for
     * @return The array of dates
     */
    public static Date[] getAllDays(final int year) {

        GregorianCalendar cal = calendarPool.take();

        try {
            List<Date> dates = new ArrayList<>();

            cal.setTime(trimDate(cal.getTime()));
            cal.set(Calendar.YEAR, year);
            cal.set(cal.get(Calendar.YEAR), cal.getActualMinimum(Calendar.MONTH), cal.getMinimum(Calendar.DAY_OF_MONTH));

            final Date startDate = cal.getTime();

            for (int i = 0; i < getDaysInYear(year); i++) {
                dates.add(addDays(startDate, i));
            }

            return dates.toArray(new Date[dates.size()]);

        } finally {
            calendarPool.put(cal);
        }

    }

    /**
     * Returns a leveled date representing the last day of the month based on a
     * specified date.
     * 
     * @param date
     *            the base date to work from
     * @return The last day of the month of the supplied date
     */
    public static Date getLastDayOfTheMonth(final Date date) {
        assert date != null;

        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMaximum(Calendar.DAY_OF_MONTH));

            return trimDate(c.getTime());
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns a leveled date representing the last day of the month
     * 
     * @param month
     *            The month (index starts at 0)
     * @param year
     *            The year (index starts at 1)
     * @return The last day of the month and year specified
     */
    public static Date getLastDayOfTheMonth(final int month, final int year) {
        assert month >= 0 && month <= 11;

        GregorianCalendar c = calendarPool.take();

        try {
            c.set(year, month, 15);
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMaximum(Calendar.DAY_OF_MONTH));

            return trimDate(c.getTime());
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Generates an array of dates ending on the last day of every month between
     * the start and stop dates.
     * 
     * @param startDate
     *            The date to start at
     * @param endDate
     *            The data to stop at
     * @return The array of dates
     */
    public static List<Date> getLastDayOfTheMonths(final Date startDate, final Date endDate) {
        ArrayList<Date> list = new ArrayList<>();

        Date end = DateUtils.getLastDayOfTheMonth(endDate);
        Date t = DateUtils.getLastDayOfTheMonth(startDate);

        /*
         * add a month at a time to the previous date until all of the months
         * have been captured
         */
        while (before(t, end)) {
            list.add(t);
            t = DateUtils.getLastDayOfTheMonth(DateUtils.addMonth(t));
        }
        return list;
    }

    /**
     * Generates an array of dates starting on the first day of every month
     * between the start and stop dates.
     * 
     * @param startDate
     *            The date to start at
     * @param endDate
     *            The data to stop at
     * @return The array of dates
     */
    public static List<Date> getFirstDayOfTheMonths(final Date startDate, final Date endDate) {
        ArrayList<Date> list = new ArrayList<>();

        Date end = DateUtils.getFirstDayOfTheMonth(endDate);
        Date t = DateUtils.getFirstDayOfTheMonth(startDate);

        /*
         * add a month at a time to the previous date until all of the months
         * have been captured
         */
        while (before(t, end)) {
            list.add(t);
            t = DateUtils.getFirstDayOfTheMonth(DateUtils.addMonth(t));
        }
        return list;
    }

    /**
     * Returns a leveled date representing the last day of the quarter based on
     * a specified date.
     * 
     * @param date
     *            the base date to work from
     * @return The last day of the quarter specified
     */
    public static Date getLastDayOfTheQuarter(final Date date) {
        assert date != null;

        Date result;

        Date leveledDate = trimDate(date);

        Date[] bounds = getQuarterBounds(leveledDate);

        if (leveledDate.compareTo(bounds[2]) < 0) {
            result = bounds[1];
        } else if (leveledDate.compareTo(bounds[4]) < 0) {
            result = bounds[3];
        } else if (leveledDate.compareTo(bounds[6]) < 0) {
            result = bounds[5];
        } else {
            result = bounds[7];
        }

        return trimDate(result);
    }

    /**
     * Returns a leveled date representing the last day of the year based on a
     * specified date.
     * 
     * @param date
     *            the base date to work from
     * @return The last day of the year specified
     */
    public static Date getLastDayOfTheYear(final Date date) {
        assert date != null;

        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.set(c.get(Calendar.YEAR), c.getActualMaximum(Calendar.MONTH), c.getMaximum(Calendar.DAY_OF_MONTH));

            return trimDate(c.getTime());
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns an array of quarter bound dates of the year based on a specified
     * date. The order is q1s, q1e, q2s, q2e, q3s, q3e, q4s, q4e.
     * 
     * @param date
     *            the base date to work from
     * @return The array of quarter bound dates
     */
    private static Date[] getQuarterBounds(final Date date) {
        assert date != null;

        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);

            final int year = c.get(Calendar.YEAR);

            Date[] bounds = new Date[8];

            c.set(year, Calendar.JANUARY, 1, 0, 0, 0);
            bounds[0] = c.getTime();

            c.set(year, Calendar.MARCH, 31, 0, 0, 0);
            bounds[1] = c.getTime();

            c.set(year, Calendar.APRIL, 1, 0, 0, 0);
            bounds[2] = c.getTime();

            c.set(year, Calendar.JUNE, 30, 0, 0, 0);
            bounds[3] = c.getTime();

            c.set(year, Calendar.JULY, 1, 0, 0, 0);
            bounds[4] = c.getTime();

            c.set(year, Calendar.SEPTEMBER, 30, 0, 0, 0);
            bounds[5] = c.getTime();

            c.set(year, Calendar.OCTOBER, 1, 0, 0, 0);
            bounds[6] = c.getTime();

            c.set(year, Calendar.DECEMBER, 31, 0, 0, 0);
            bounds[7] = c.getTime();

            return bounds;
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns the number of the quarter (i.e. 1, 2, 3 or 4) based on a
     * specified date.
     * 
     * @param date
     *            the base date to work from
     * @return The number of the quarter specified
     */
    public static int getQuarterNumber(final Date date) {
        assert date != null;

        int result;

        Date[] bounds = getQuarterBounds(date);

        if (date.compareTo(bounds[2]) < 0) {
            result = 1;
        } else if (date.compareTo(bounds[4]) < 0) {
            result = 2;
        } else if (date.compareTo(bounds[6]) < 0) {
            result = 3;
        } else {
            result = 4;
        }

        return result;
    }

    //  public static int getMonthByDayOfYear(int year, int dayOfYear) {
    //
    //      Calendar c = getGCal();
    //
    //      c.setTime(trimDate(c.getTime()));
    //      c.set(Calendar.YEAR, year);
    //      c.set(Calendar.DAY_OF_YEAR, dayOfYear);
    //
    //      return c.get(Calendar.MONTH);
    //   }

    /**
     * Generates a customized DateFormat with constant width for all dates.
     * 
     * @return a short DateFormat
     */
    public static DateFormat getShortDateFormat() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

        if (df instanceof SimpleDateFormat) {
            String pattern = ((SimpleDateFormat) df).toPattern();

            pattern = DAY_PATTERN.matcher(MONTH_PATTERN.matcher(pattern).replaceAll("MM")).replaceAll("dd");
            ((SimpleDateFormat) df).applyPattern(pattern);
        }
        return df;
    }

    /**
     * Returns the numerical week of the year given a date.
     * <p>
     * Minimal days of week is set to 4 to comply with ISO 8601
     * 
     * @param dateOfYear
     *            the base date to work from
     * @return the week of the year
     */
    public static int getWeekOfTheYear(final Date dateOfYear) {
        GregorianCalendar c = calendarPool.take();

        int minimal = c.getMinimalDaysInFirstWeek();

        try {
            c.setTime(dateOfYear);
            c.setMinimalDaysInFirstWeek(4);

            return c.get(Calendar.WEEK_OF_YEAR);
        } finally {
            c.setMinimalDaysInFirstWeek(minimal); // restore original minimal
            calendarPool.put(c);
        }
    }

    /**
     * Determines if the supplied year is a leap year
     * 
     * @param year
     *            integer based year (ex. 2000, 2011)
     * @return <code>true</code> if the given year is a leap year
     */
    public static boolean isLeapYear(final int year) {
        boolean isLeapYear;

        isLeapYear = year % 4 == 0;

        isLeapYear = isLeapYear && year % 100 != 0;

        return isLeapYear || year % 400 == 0;
    }

    /**
     * Subtract one day from the supplied date
     * 
     * @param date
     *            start date
     * @return one day less
     */
    public static Date subtractDay(final Date date) {
        return addDays(date, -1);
    }

    /**
     * Subtracts one month from the supplied date
     * 
     * @param date
     *            beginning date
     * @return prior month
     */
    public static Date subtractMonth(final Date date) {
        return subtractMonths(date, 1);
    }

    /**
     * Subtracts months from the supplied date
     * 
     * @param date
     *            beginning date
     * @param months
     *            number of months to subtract
     * @return prior month
     */
    private static Date subtractMonths(final Date date, final int months) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.add(Calendar.MONTH, months * -1);
            return c.getTime();
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns a date one year earlier than the provided date
     * 
     * @param date
     *            start date
     * @return date of the previous year
     */
    public static Date subtractYear(final Date date) {
        return addYears(date, -1);
    }

    /**
     * Returns a date incremented or decremented by a number of years
     * 
     * @param date
     *            start date
     * @param years
     *            number of years
     * @return date of the new year
     */
    private static Date addYears(final Date date, final int years) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);
            c.add(Calendar.YEAR, years);
            return c.getTime();
        } finally {
            calendarPool.put(c);
        }
    }

    /**
     * Returns a trimmed version of todays date
     * 
     * @return trimmed date
     */
    public static Date today() {
        return trimDate(new Date());
    }

    /**
     * Trims the date so that only the day, month, and year are significant.
     * 
     * @param date
     *            date to trim
     * @return leveled date
     */
    public static Date trimDate(final Date date) {
        GregorianCalendar c = calendarPool.take();

        try {
            c.setTime(date);

            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.HOUR_OF_DAY, 0);

            return c.getTime();
        } finally {
            calendarPool.put(c);
        }
    }
}
