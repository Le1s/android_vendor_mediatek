package com.mtk.test.imei.writetest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

public class ConfigParser {
    public static final String DEFAULT_FILE_NAME = "/sdcard/config.writeimei.xml";
    private static String[] sImei = null;

    public static String[] getConfig() {
        if (sImei == null) {
            sImei = readConfig(DEFAULT_FILE_NAME);
        }
        return sImei;
    }

    private static String[] readConfig(String filename) {
        String[] imei = new String[2];

        Document document = load(filename);
        Element root = document.getRootElement();

        Element temp = root.element("imei1");
        imei[0] = temp.getTextTrim();

        temp = root.element("imei2");
        imei[1] = temp.getTextTrim();

        return imei;
    }

    public static Document load(String filename) {
        Document document = null;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(new File(filename));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }

    public static void main(String[] args) {
        getConfig();
    }
}
