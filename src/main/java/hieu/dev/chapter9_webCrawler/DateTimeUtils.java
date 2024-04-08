package hieu.dev.chapter9_webCrawler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {
    public static String convertDateToString(Date date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }
    public static String convertTodayToString(String pattern) {
        return convertDateToString(new Date(), pattern);
    }
    public static String convertDayAfterToString(int days, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(yesterday);
    }
}
