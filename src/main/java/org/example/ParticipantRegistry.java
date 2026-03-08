package org.example;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ParticipantRegistry {

    private final Map<String, String> namesByCode = new HashMap<String, String>();
    private final File storageFile;

    public ParticipantRegistry(File storageFile) {
        this.storageFile = storageFile;
    }

    public void putAll(Map<String, String> data) {
        namesByCode.putAll(data);
    }

    public String getNameByCode(String code) {
        if (code == null) {
            return "";
        }
        String value = namesByCode.get(code.trim());
        return value == null ? "" : value;
    }

    public int size() {
        return namesByCode.size();
    }

    public void clear() {
        namesByCode.clear();
    }

    public void load() throws IOException {
        namesByCode.clear();

        if (!storageFile.exists()) {
            return;
        }

        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(storageFile);
        try {
            properties.load(new InputStreamReader(inputStream, "UTF-8"));
        } finally {
            inputStream.close();
        }

        for (String key : properties.stringPropertyNames()) {
            namesByCode.put(key, properties.getProperty(key, ""));
        }
    }

    public void save() throws IOException {
        File parent = storageFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : namesByCode.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }

        OutputStream outputStream = new FileOutputStream(storageFile);
        try {
            properties.store(new OutputStreamWriter(outputStream, "UTF-8"), "Participants registry");
        } finally {
            outputStream.close();
        }
    }
}