package com.jessica.masterproject.helpers;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.text.TextUtils;

public class FileSaver {

    public FileSaver() { }

    public static boolean save(String filename, String format, String[] data, boolean append) {
        File folder = new File(String.valueOf(Environment.getExternalStorageDirectory())+"/annoyme");
        String state = Environment.getExternalStorageState();

        // Check if media is available
        if(!Environment.MEDIA_MOUNTED.equals(state)) {
            System.err.println("Media not mounted");
            return false;
        }

        // check if folder exists and if not, try to create it
        if(!folder.exists()) {
            if(!(folder.mkdir() || folder.isDirectory())) {
                System.err.println("Can't mkdir or doesn't exist");
                return false;
            }
        }

        File file = new File(folder.getPath()+"/"+filename+format);
        boolean fileExists = false;

        // checks if file exists
        if(!file.exists()) {
            // if not, try to create it
            try {
                fileExists = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Couldn't create file "+file.getName());
                return false;
            }
        }
        else fileExists = true;

        // Write data to file
        try {
            if (fileExists) {
                FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), append);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                if(append)
                    bufferedWriter.write(","+TextUtils.join(",", data));
                else
                    bufferedWriter.write(TextUtils.join(",", data));
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't write to file " + file.getName());
            return false;
        }
        return true;
    }
}
