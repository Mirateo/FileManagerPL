package com.example.filemanager.tools;

import java.util.Arrays;
import java.util.List;

public final class FileType {
    private static final List<String> IMG = Arrays.asList("ai", "arw", "bmp", "cr2", "crw", "dng", "gif", "jpeg", "jpg", "nef", "nrw", "orf", "png", "psd", "tif", "tiff", "sr2", "svg", "svgz");
    private static final List<String> AUDIO = Arrays.asList("wav", "mp3", "m4a", "aac");
    private static final List<String> MOVIE = Arrays.asList("3gp", "mpeg", "avi", "dv", "mp4", "wmv");
    private static final List<String> DOC = Arrays.asList("eps", "doc", "docm", "docx", "dot", "dotm", "dotx", "numbers", "pages", "pdf", "xls", "xslsm", "xlsx", "xlt", "xltm", "xltx");
    private static final List<String> EXEC = Arrays.asList("apk", "exe", "exec");

    public static boolean isExtIMG(String extension) {
        return IMG.contains(extension);
    }
    public static boolean isExtAUDIO(String extension) {
        return AUDIO.contains(extension);
    }
    public static boolean isExtMOVIE(String extension) {
        return MOVIE.contains(extension);
    }
    public static boolean isExtDOC(String extension) {
        return DOC.contains(extension);
    }
    public static boolean isExtEXEC(String extension) {
        return EXEC.contains(extension);
    }

    public static boolean isFileIMG(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if(dot == -1) {
            return false;
        }
        return isExtIMG(fileName.substring( dot + 1).toLowerCase());
    }
    public static boolean isFileAUDIO(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if(dot == -1) {
            return false;
        }
        return isExtAUDIO(fileName.substring( dot + 1).toLowerCase());
    }
    public static boolean isFileVIDEO(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if(dot == -1) {
            return false;
        }
        return isExtMOVIE(fileName.substring( dot + 1).toLowerCase());
    }
    public static boolean isFileDOC(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if(dot == -1) {
            return false;
        }
        return isExtDOC(fileName.substring( dot + 1).toLowerCase());
    }
    public static boolean isFileEXEC(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if(dot == -1) {
            return false;
        }
        return isExtEXEC(fileName.substring( dot + 1).toLowerCase());
    }

    public static String fileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        }
        String ext = url.substring(url.lastIndexOf(".") + 1);
        if (ext.contains("%")) {
            ext = ext.substring(0, ext.indexOf("%"));
        }
        if (ext.contains("/")) {
            ext = ext.substring(0, ext.indexOf("/"));
        }
        return ext.toLowerCase();

    }
}

