package src;

import java.util.ArrayList;
import java.util.List;

public class FaceData {
    private List<String> imagePaths = new ArrayList<>();
    
    public void addImagePath(String p){
        imagePaths.add(p);
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }
    
}