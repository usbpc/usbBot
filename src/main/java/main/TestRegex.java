package main;

import java.util.regex.Pattern;

/**
 * Created by usbpc on 01.05.2017.
 */
public class TestRegex {
    public static void main(String...args) {
        String first = "|";
        String second = Pattern.quote(first);
        String third = Pattern.quote(second);
        String forth = Pattern.quote(third);

        System.out.printf("Does %s match %s? %b %n", first, second, first.matches(second));
        System.out.printf("Does %s match %s? %b %n", second, third, second.matches(third));
        System.out.printf("Does %s match %s? %b %n", third, forth, third.matches(forth));
    }
}
