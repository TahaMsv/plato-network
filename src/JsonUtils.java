import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

public class JsonUtils {
    public static String getJsonStringFromFile(String path){
        Scanner input;
        InputStream ins = FileHandler.fileInputStream(path);
        input = new Scanner(ins);
        String json = input.useDelimiter("\\Z").next();
        input.close();
        return json;
    }


    public static JSONObject getJsonObjectFromFile(String path){
        return new JSONObject(getJsonStringFromFile(path));
    }

    public static boolean objectExists(JSONObject jsonObject, String key){
        Object o;
        try {
            o = jsonObject.get(key);
        } catch (Exception e){
            return false;
        }
        return o!=null;
    }
}
