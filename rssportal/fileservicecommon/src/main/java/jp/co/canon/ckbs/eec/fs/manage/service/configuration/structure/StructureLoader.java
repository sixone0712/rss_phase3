package jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StructureLoader {
    ObjectMapper objectMapper = new ObjectMapper();

    File file = null;
    long lastModified = 0;

    Structure structure = null;

    Map<String, MpaInfoEx> mpaInfoExMap = new HashMap<>();

    public StructureLoader(String structureFileName){
        log.info("structureFileName {}", structureFileName);
        file = new File(structureFileName);
        structure = new Structure();
        structure.setVersion("-");
        structure.setMpaList(new MpaInfo[0]);
        structure.setOtsList(new OtsInfo[0]);
    }

    OtsInfo getOts(String name){
        for(OtsInfo ots : structure.getOtsList()){
            if (ots.getName().equals(name)){
                return ots;
            }
        }
        return null;
    }

    public void load() throws IOException {
        if (file.exists() && file.isFile()){
            if (lastModified != file.lastModified()){
                log.info("reload structure configuration...");
                structure = objectMapper.readValue(file, Structure.class);
                mpaInfoExMap.clear();
                for(MpaInfo mpa : structure.getMpaList()){
                    OtsInfo ots = getOts(mpa.getOts());
                    MpaInfoEx mpaInfoEx = new MpaInfoEx();
                    mpaInfoEx.setMpa(mpa);
                    mpaInfoEx.setOts(ots);
                    mpaInfoExMap.put(mpa.getName(), mpaInfoEx);
                }
                lastModified = file.lastModified();
            }
            return;
        }
        throw new IOException("Error loading structure file.");
    }

    public OtsInfo[] getOtsList() throws IOException{
        load();
        return structure.getOtsList();
    }

    public MpaInfo[] getMpaList() throws IOException{
        load();
        return structure.getMpaList();
    }

    public String getVersion() throws IOException{
        load();
        return structure.getVersion();
    }

    public MpaInfoEx getMpaInfoEx(String name){
        try {
            load();
        } catch (IOException e){

        }
        return mpaInfoExMap.get(name);
    }



}
