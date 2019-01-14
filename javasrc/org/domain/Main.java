package org.domain;

import java.lang.reflect.*;

public class Main {
    public static String FIELD = "Main.class: public static String FIELD = \"...\"";

    public static void main(String[] args) {
        String s = "";
        for (int i = 0; i < args.length; i++) {
            s += (i == 0 ? "" : ", ") + args[i];
        }
        System.out.println("main: args: "+s);
    }
    public static String staticFn0() {
        return "staticFn0: no-args";
    }
    public static String staticFn1(String arg0) {
        return "staticFn1: arg0: " + arg0;
    }
    public static String staticFn2(String arg0, String arg1) {
        return "staticFn1: arg0: " + arg0 + ", arg1: " + arg1;
    }
    public String instanceFn0() {
        return "instanceFn0: no-args";
    }
    public String instanceFn1(String arg0) {
        return "instanceFn0: arg0: " + arg0;
    }
    public String instanceFn2(String arg0, String arg1) {
        return "instanceFn0: arg0: " + arg0 + ", arg1: " + arg1;
    }

    public static void dumpMethods() {
        try {
            Class c = Class.forName("java.util.Date");
            Method m[] = c.getDeclaredMethods();
            for (int i = 0; i < m.length; i++)
                System.out.println(m[i].toString());
        }
        catch (Throwable e) {
            System.err.println(e);
        }
    }
}
