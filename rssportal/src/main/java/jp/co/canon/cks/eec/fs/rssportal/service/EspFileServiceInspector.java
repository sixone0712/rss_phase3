package jp.co.canon.cks.eec.fs.rssportal.service;

import jp.co.canon.ckbs.eec.fs.collect.controller.param.MachineStatusRequestResponse;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.MpaInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.OtsInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.StructureLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EspFileServiceInspector implements FileServiceInspector {

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Value("${rssportal.configuration.path}")
    private String configurationPath;

    @Value("${rssportal.configuration.structureFile}")
    private String systemMachineStructure;

    @Autowired
    private FileServiceManageConnectorFactory connectorFactory;

    private StructureLoader machineLoader;

    @PostConstruct
    private void _init() {
        this.machineLoader = new StructureLoader(configurationPath + systemMachineStructure);
    }

    @Override
    public int[] checkMachine(String machine) {

        if(machine==null || machine.isEmpty()) {
            return new int[]{disconnected, disconnected};
        }

        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
        if(connector==null) {
            return new int[]{disconnected, disconnected};
        }

        MachineStatusRequestResponse response = connector.getMachineStatus(machine);

        if(response.getErrorCode()!=0 || response.getErrorMessage()!=null ||
                response.getFtpStatus().equalsIgnoreCase("disconnected")) {
            return new int[]{disconnected, disconnected};
        }

        int[] status = new int[] {connected, connected};
        if(response.getFtpStatus().equalsIgnoreCase("disconnected")) {
            status[FTP_STATUS] = disconnected;
        }
        if(response.getVFtpStatus().equalsIgnoreCase("disconnected")) {
            status[VFTP_STATUS] = disconnected;
        }
        return status;
    }

    @Override
    public int checkOts(String ots) {

        if(ots==null || ots.isEmpty()) {
            return FileServiceInspector.disconnected;
        }

        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
        if(connector==null) {
            return FileServiceInspector.disconnected;
        }
        MachineStatusRequestResponse response = connector.getOtsStatus(ots);

        if(response.getErrorCode()!=0 || response.getErrorMessage()!=null ||
                response.getFtpStatus().equalsIgnoreCase("disconnected")) {

            return FileServiceInspector.disconnected;
        }

        return FileServiceInspector.connected;
    }

    @Override
    public String[] getMachineList() throws IOException {

        if(machineLoader==null) {
            return new String[0];
        }

        MpaInfo[] _machines = machineLoader.getMpaList();
        String[] machines = new String[_machines.length];
        for(int i=0; i<machines.length; ++i) {
            machines[i] = _machines[i].getName();
        }
        return machines;
    }

    @Override
    public String[] getOtsList() throws IOException {

        if(machineLoader==null) {
            return new String[0];
        }

        OtsInfo[] _otses = machineLoader.getOtsList();
        String[] otses = new String[_otses.length];
        for(int i=0; i<otses.length; ++i) {
            otses[i] = _otses[i].getName();
        }
        return otses;
    }

    @Override
    public Map<String, List<String>> getMachineHierarchy() throws IOException {

        if(machineLoader==null) {
            return null;
        }

        Map<String, List<String>> hierarchy = new HashMap<>();

        OtsInfo[] otses = machineLoader.getOtsList();
        MpaInfo[] machines = machineLoader.getMpaList();

        for(OtsInfo ots: otses) {
            if(hierarchy.containsKey(ots.getName())) {
                throw new IOException("ots configuration error");
            }
            hierarchy.put(ots.getName(), new ArrayList<>());
        }

        for(MpaInfo machine: machines) {
            if(!hierarchy.containsKey(machine.getOts())) {
                throw new IOException("machine configuration error");
            }
            hierarchy.get(machine.getOts()).add(machine.getName());
        }
        return hierarchy;
    }
}
