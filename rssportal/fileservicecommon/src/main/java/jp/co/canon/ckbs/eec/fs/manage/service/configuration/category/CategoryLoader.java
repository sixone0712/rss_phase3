package jp.co.canon.ckbs.eec.fs.manage.service.configuration.category;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CategoryLoader {
    ObjectMapper objectMapper = new ObjectMapper();
    File file;

    long lastModified = 0;

    Categories categories = null;
    Map<String, CategoryInfo> categoryInfoMap = new HashMap<>();

    public CategoryLoader(String categoryFileName){
        file = new File(categoryFileName);
        categories = new Categories();
        categories.setVersion("-");
        categories.setCategories(new CategoryInfo[0]);

    }

    public void load() throws IOException {
        if (file.exists() && file.isFile()){
            if (lastModified != file.lastModified()){
                categories = objectMapper.readValue(file, Categories.class);
                categoryInfoMap.clear();
                for(CategoryInfo info : categories.getCategories()){
                    categoryInfoMap.put(String.format("%03X", info.getNo()), info);
                }
                lastModified = file.lastModified();
            }
            return;
        }
        throw new IOException("Error loading categories file.");
    }

    public CategoryInfo[] getCategories() throws IOException {
        load();
        return categories.getCategories();
    }

    public CategoryInfo getCategory(String categoryCode){
        try {
            load();
            return categoryInfoMap.get(categoryCode);
        } catch (IOException e) {
            return null;
        }
    }

    public String getVersion() throws IOException {
        load();
        return categories.getVersion();
    }
}
