package pl.enterprise.vpn.client.data;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

public class Prefs {
    private static final String NAME = "strongswan.prefs";

    private static SharedPreferences p;


    /**
     * Must be called, when Application starts
     */
    public static void init(Context c) {
        p = c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }


    public static void put(String key, boolean     val){ p.edit().putBoolean  (key, val).apply(); }
    public static void put(String key, int         val){ p.edit().putInt      (key, val).apply(); }
    public static void put(String key, String      val){ p.edit().putString   (key, val).apply(); }
    public static void put(String key, Set<String> val){ p.edit().putStringSet(key, val).apply(); }
    public static void put(String key, Long val)       { p.edit().putLong     (key, val).apply(); }
    public static void put(String key, Float val)      { p.edit().putFloat    (key, val).apply(); }

    public static void put(String key)                 { p.edit().putBoolean  (key,true).apply(); }


    public static boolean del(String key){
        boolean has = has(key); if (has) { p.edit().remove(key).apply(); } return has;
    }


    public static boolean get(String key, Boolean def) { return p.getBoolean(key, def); }
    public static int     get(String key, Integer def) { return p.getInt    (key, def); }
    public static String  get(String key, String  def) { return p.getString (key, def); }
    public static Long    get(String key, Long    def) { return p.getLong   (key, def); }
    public static Float   get(String key, Float   def) { return p.getFloat  (key, def); }
    public static Set<String>   get(String key, Set<String>  def) { return p.getStringSet (key, def); }
    public static String  def(String key)              { return p.getString (key, ""); }

    public static boolean has(String key) { return p.contains(key); }


    // NOTE: hack. No standart option to store "double" in SharedPreferences
    public static double getDouble(String key, double def) { return fromLong(p.getLong (key, toLong(def))); }
    public static void   putDouble(String key, double val) { p.edit().putLong(key, toLong(val)).apply(); }

    private static long toLong(double d) {
        return Double.doubleToRawLongBits(d);
    }
    private static double fromLong(long d) {
        return Double.longBitsToDouble(d);
    }

    public static void clear() { p.edit().clear().apply(); }

    public static Map<String, ?> getAll() {
        return p.getAll();
    }
}
