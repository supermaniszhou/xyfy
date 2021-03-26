package com.seeyon.apps.ext.DTdocument.util;

import java.io.*;
import java.util.Properties;

/**
 * 周刘成   2019/7/24
 */
public class ReadConfigTools {
    private static ReadConfigTools configTools;

    public static ReadConfigTools getInstance() {
        return configTools = new ReadConfigTools();
    }

    private Properties properties;

    public ReadConfigTools() {
        String path=Thread.currentThread().getContextClassLoader().getResource("law/docPlugin.properties").getPath();
        File file=new File(path);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.properties = new Properties();
        try {
            this.properties.load(new InputStreamReader(inputStream, "UTF-8"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getString(String key) {
        if ((key == null) || (key.equals("")) || (key.equals("null"))) {
            return "";
        }
        String result = "";
        try {
            result = properties.getProperty(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
