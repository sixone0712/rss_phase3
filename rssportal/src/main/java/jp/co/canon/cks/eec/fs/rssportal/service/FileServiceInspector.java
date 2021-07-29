package jp.co.canon.cks.eec.fs.rssportal.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileServiceInspector {

    /**
     * Indices representing response array.
     */
    int FTP_STATUS = 0;
    int VFTP_STATUS = 1;

    int disconnected = 0;
    int connected = 1;

    /**
     * Get the status of the specific machine.
     * @param machine
     * @return
     */
    int[] checkMachine(String machine);

    /**
     * Get the status of the specific ots.
     * @param ots
     * @return
     */
    int checkOts(String ots);

    /**
     * Retrieves machine names from configuration files.
     * @return     Machine names.
     */
    String[] getMachineList() throws IOException;

    /**
     * Retrieves ots names from configuration files.
     * @return      Ots names.
     */
    String[] getOtsList() throws IOException;

    /**
     * Returns ots and mpa hierarchy as a map object.
     * @return
     * @throws IOException
     */
    Map<String, List<String>> getMachineHierarchy() throws IOException;
}
