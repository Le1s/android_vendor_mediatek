/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.csm.tests;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection utils.
 */
public class Reflection {

    /**
     * Returns the value of the field with the given name in the specified object.
     * @param owner the object to access
     * @param fieldName property name
     * @return the field value
     */
    public static Object getProperty(Object owner, String fieldName) {
        Class ownerClass = owner.getClass();
        Object property = null;
        try {
            Field field = ownerClass.getField(fieldName);
            property = field.get(owner);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return property;
    }

    /**
     * Returns the value of the field with the given name which is declared in the specified object.
     * @param owner the object to access
     * @param fieldName property name
     * @return the field value
     */
    public static Object getDeclaredProperty(Object owner, String fieldName) {
        Class ownerClass = owner.getClass();
        Object property = null;
        try {
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            property = field.get(owner);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return property;
    }

    /**
     * Returns the value of the static field with the given name in the class.
     * @param className class name
     * @param fieldName property name
     * @return the field value
     */
    public static Object getStaticProperty(String className, String fieldName) {
        Object property = null;
        try {
            Class ownerClass = Class.forName(className);
            Field field = ownerClass.getField(fieldName);
            property = field.get(ownerClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return property;
    }

    /**
     * Returns the value of the static field with the given name which is declared in the class.
     * @param className class name
     * @param fieldName property name
     * @return the field value
     */
    public static Object getStaticDeclaredProperty(String className,
            String fieldName) {
        Object property = null;
        try {
            Class ownerClass = Class.forName(className);
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            property = field.get(ownerClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return property;
    }

    /**
     * Sets the value of the field with the given name in the specified object to the value.
     * @param owner the object to access
     * @param fieldName property name
     * @param value the new value
     */
    public static void setProperty(Object owner, String fieldName, Object value) {
        Class ownerClass = owner.getClass();
        try {
            Field field = ownerClass.getField(fieldName);
            field.set(owner, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the value of the field with the given name which is declared in the specified object to
     * the value.
     * @param owner the object to access
     * @param fieldName property name
     * @param value the new value
     */
    public static void setDeclaredProperty(Object owner, String fieldName,
            Object value) {
        Class ownerClass = owner.getClass();
        try {
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(owner, value);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the value of the static field with the given name in the class to the value.
     * @param className class name
     * @param fieldName property name
     * @param value the new value
     */
    public static void setStaticProperty(String className, String fieldName,
            Object value) {
        try {
            Class ownerClass = Class.forName(className);
            Field field = ownerClass.getField(fieldName);
            field.set(ownerClass, value);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the value of the static field with the given name which is declared in the class to the
     * value.
     * @param className class name
     * @param fieldName property name
     * @param value the new value
     */
    public static void setStaticDeclaredProperty(String className,
            String fieldName, Object value) {
        try {
            Class ownerClass = Class.forName(className);
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(ownerClass, value);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the result of dynamically invoking the method.
     * @param owner the object on which to call this method
     * @param methodName to invoke method name
     * @param args the arguments to the method
     * @return the result
     */
    public static Object invokeMethod(Object owner, String methodName,
            Object... args) {
        Class ownerClass = owner.getClass();
        Class[] argsClass = null;
        int length = args.length;
        if (args != null && length != 0) {
            argsClass = new Class[length];
            for (int i = 0; i < length; i++) {
                argsClass[i] = args[i].getClass();
            }
        }

        Object result = null;
        Method method = null;
        try {
            if (argsClass != null) {
                method = ownerClass.getMethod(methodName, argsClass);
                result = method.invoke(owner, args);
            } else {
                method = ownerClass.getMethod(methodName);
                result = method.invoke(owner);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the result of dynamically invoking the method that is declared by the class.
     * @param owner the object on which to call this method
     * @param methodName to invoke method name that is declared by the class
     * @param args the arguments to the method
     * @return the result
     */
    public static Object invokeDeclaredMethod(Object owner, String methodName,
            Object... args) {
        Class ownerClass = owner.getClass();
        Class[] argsClass = null;
        int length = args.length;
        if (args != null && length != 0) {
            argsClass = new Class[length];
            for (int i = 0; i < length; i++) {
                argsClass[i] = args[i].getClass();
            }
        }

        Object result = null;
        Method method = null;
        try {
            if (argsClass != null) {
                method = ownerClass.getDeclaredMethod(methodName, argsClass);
                method.setAccessible(true);
                result = method.invoke(owner, args);
            } else {
                method = ownerClass.getDeclaredMethod(methodName);
                method.setAccessible(true);
                result = method.invoke(owner);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the result of dynamically invoking the static method.
     * @param className the specified class name
     * @param methodName to invoke static method name
     * @param args the arguments to the method
     * @return the result
     */
    public static Object invokeStaticMethod(String className,
            String methodName, Object... args) {
        Object result = null;
        Class[] argsClass = null;
        int length = args.length;
        if (args != null && length != 0) {
            argsClass = new Class[length];
            for (int i = 0; i < length; i++) {
                argsClass[i] = args[i].getClass();
            }
        }

        Method method = null;
        try {
            Class ownerClass = Class.forName(className);
            if (argsClass != null) {
                method = ownerClass.getMethod(methodName, argsClass);
                result = method.invoke(null, args);
            } else {
                method = ownerClass.getMethod(methodName);
                result = method.invoke(null);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the result of dynamically invoking the static method that is declared by the class.
     * @param className the specified class name
     * @param methodName to invoke static method name that is declared by the class
     * @param args the arguments to the method
     * @return the result
     */
    public static Object invokeStaticDeclaredMethod(String className,
            String methodName, Object... args) {
        Object result = null;
        Class[] argsClass = null;
        if (args != null && args.length != 0) {
            argsClass = new Class[args.length];
            for (int i = 0, j = args.length; i < j; i++) {
                argsClass[i] = args[i].getClass();
            }
        }

        Method method = null;
        try {
            Class ownerClass = Class.forName(className);
            if (argsClass != null) {
                method = ownerClass.getDeclaredMethod(methodName, argsClass);
                method.setAccessible(true);
                result = method.invoke(null, args);
            } else {
                method = ownerClass.getDeclaredMethod(methodName);
                method.setAccessible(true);
                result = method.invoke(null);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns a new instance of the declaring class, initialized by dynamically invoking the
     * constructor represented by this Constructor object.
     * @param className the specified class name
     * @param args the arguments to the constructor
     * @return the new, initialized, object
     */
    public static Object newInstance(String className, Object[] args) {
        Class[] argsClass = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        Object instance = null;
        try {
            Class newoneClass = Class.forName(className);
            Constructor cons = newoneClass.getConstructor(argsClass);
            instance = cons.newInstance(args);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Tests whether the given object can be cast to the class represented by cls.
     * @param obj test object
     * @param cls calss object
     * @return true if object can be cast to the type represented by this Class;
     *         false if object is null or cannot be cast.
     */
    public static boolean isInstance(Object obj, Class cls) {
        return cls.isInstance(obj);
    }

    /**
     * Returns the element of the array at the specified index.
     * @param array the specified array object
     * @param index the specified position
     * @return the element of the array at the specified index
     */
    public static Object getByArray(Object array, int index) {
        return Array.get(array, index);
    }
}
