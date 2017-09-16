package com.hesine.nmsg.config;

import java.lang.reflect.Field;

import android.content.SharedPreferences;

public abstract class BaseConfig {

    public void Get() {
        SharedPreferences prefs = GetSharedPrefs();
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (prefs.contains(name)) {
                Class<?> c = field.getType();
                try {
                    if (c == String.class) {
                        String v = prefs.getString(name, "");
                        field.set(this, v);
                    } else if (c == boolean.class) {
                        boolean v = prefs.getBoolean(name, false);
                        field.set(this, v);
                    } else if (c == int.class) {
                        int v = prefs.getInt(name, 0);
                        field.set(this, v);
                    } else if (c == float.class) {
                        float v = prefs.getFloat(name, 0);
                        field.set(this, v);
                    } else if (c == long.class) {
                        long v = prefs.getLong(name, 0);
                        field.set(this, v);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    protected abstract SharedPreferences GetSharedPrefs();

    public void Save() {
        SharedPreferences.Editor editor = GetSharedPrefs().edit();
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            String name = field.getName();
            try {
                Object value = field.get(this);
                if (value == null)
                    editor.remove(name);
                else {
                    Class<?> c = field.getType();
                    if (c == String.class) {
                        editor.putString(name, (String) value);
                    } else if (c == boolean.class) {
                        editor.putBoolean(name, (Boolean) value);
                    } else if (c == int.class) {
                        editor.putInt(name, (Integer) value);
                    } else if (c == float.class) {
                        editor.putFloat(name, (Float) value);
                    } else if (c == long.class) {
                        editor.putLong(name, (Long) value);
                    }
                }
            } catch (Exception e) {
            }
        }
        editor.commit();
    }

    public void Reset() {
        SharedPreferences.Editor editor = GetSharedPrefs().edit();
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            String name = field.getName();
            editor.remove(name);
        }
        editor.commit();
    }
}
