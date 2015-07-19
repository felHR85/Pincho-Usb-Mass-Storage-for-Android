package com.felhr.usbmassstorageforandroid.filesystems.fat32;

import java.util.Random;

/**
 * Created by Felipe Herranz(felhr85@gmail.com) on 18/7/15.
 */
public class ShortNameGenerator
{
    private ShortNameGenerator()
    {

    }

    /*
       Trying to generate shortnames that will force Windows to read the LFN instead
     */

    public static String[] getRandomShortName(String name)
    {
        String[] fileAndExtension = new String[2];
        String strStep1 = name.replaceAll("\\s+", "");
        boolean first = false;
        StringBuilder strStep2Builder = new StringBuilder(strStep1);
        for(int i=strStep1.length()-1;i>=0;i--)
        {
            if(strStep1.charAt(i) == '.' && !first)
            {
                first = true;
            }else if(strStep1.charAt(i) == '.' && first)
            {
                strStep2Builder.deleteCharAt(i);
            }
        }

        String[] strStep3 = strStep2Builder.toString().split("\\.");
        if(strStep3.length > 1)
        {
            if(strStep3[1].length() > 3)
                strStep3[1] = strStep3[1].substring(0, 3);
            fileAndExtension[1] = strStep3[1];
        }else
            fileAndExtension[1] = "";

        String shortName = "";
        Random r = new Random();
        for(int i=0; i<=5;i++)
        {
            char c = (char)(r.nextInt(26) + 'a');
            shortName += String.valueOf(c);
        }

        shortName += "~";
        shortName += String.valueOf((int) (Math.random() * 10));
        fileAndExtension[0] = shortName;
        return fileAndExtension;
    }
}
