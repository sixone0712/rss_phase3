package jp.co.canon.ckbs.eec.fs.manage.service;

import jp.co.canon.ckbs.eec.fs.configuration.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class CategoryListTest {
    @Test
    void test_001(){
        CategoryList categoryList = new CategoryList();

        ArrayList<Category> categoryArrayList = new ArrayList<>();
        categoryArrayList.add(new Category());
        categoryArrayList.add(new Category());
        categoryArrayList.add(new Category());

        categoryList.setCategories(categoryArrayList.toArray(new Category[0]));
        Assertions.assertArrayEquals(categoryArrayList.toArray(new Category[0]), categoryList.getCategories());
    }
}
