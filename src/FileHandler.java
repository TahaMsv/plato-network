import java.io.InputStream;

public class FileHandler {
    public  static InputStream fileInputStream(String path){

        try {
            InputStream inputStream = FileHandler.class.getResourceAsStream(path);
            return inputStream;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
