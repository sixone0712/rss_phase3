package jp.co.canon.ckbs.eec.servicemanager.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

@Component
public class CheckPasswordFile implements ApplicationListener<ApplicationStartedEvent> {

    @Value("${servicemanager.admin-init-password}")
    private String adminInitPassword;

    @Value("${servicemanager.admin-init-password-filename}")
    private String adminInitPasswordFilename;

    @Override public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        if(!isAdminPasswordFile()) {
            createAdminPasswordFile();
        }
    }

    public boolean isAdminPasswordFile() {
        try {
            FileReader file = new FileReader(adminInitPasswordFilename);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public void createAdminPasswordFile() {
        try {
            HashMap<String, String> password = new HashMap<>();
            password.put("Administrator", adminInitPassword);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(password);

            FileWriter file = new FileWriter(adminInitPasswordFilename, false);
            file.write(jsonString);
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
