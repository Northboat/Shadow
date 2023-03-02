package com.northboat.shadow.utils;

public class StringUtil {
    public static boolean containAt(String str){
        for(char c: str.toCharArray()){
            if(c == '@'){
                return true;
            }
        }
        return false;
    }
}
