package utility;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Created by zhangbaoshan on 2016/3/23.
 */
public class TestUtility {
    //public static boolean IS_FULLY_FILL=true;
    //public static boolean IS_PARTIALLY_FILL=true;

    public final static double Delta = 0.0000000001; //1/billion

    public static TestPurpose Purpose;

    public static boolean checkFileEmpty(String filePath){
        try{
            File file = new File(filePath);
            String content= FileUtils.readFileToString(file);
            return content.isEmpty();
        }catch (Exception ex){
            System.out.println("check file empty error!"+ex);
            return false;
        }
    }
}
