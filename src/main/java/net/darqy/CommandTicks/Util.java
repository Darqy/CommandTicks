package net.darqy.CommandTicks;

public class Util {
    
    public static String joinStrings(String[] strings, int start, char separator) {
        StringBuilder sb = new StringBuilder(strings[start++]);
        for (int i = start; i < strings.length; i++) {
            sb.append(separator);
            sb.append(strings[i]);
        }
        return sb.toString();
    }
    
}
