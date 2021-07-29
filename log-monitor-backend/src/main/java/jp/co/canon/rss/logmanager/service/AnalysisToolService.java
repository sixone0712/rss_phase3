package jp.co.canon.rss.logmanager.service;

import jp.co.canon.rss.logmanager.config.ReqURLController;
import jp.co.canon.rss.logmanager.dto.analysis.ResEquipmentDTO;
import jp.co.canon.rss.logmanager.dto.analysis.ResLogData;
import jp.co.canon.rss.logmanager.dto.analysis.ResLogTimeDTO;
import jp.co.canon.rss.logmanager.dto.job.ResLogDataTimeDTO;
import jp.co.canon.rss.logmanager.dto.site.ResSitesDetailDTO;
import jp.co.canon.rss.logmanager.dto.analysis.ResEquipmentsListDTO;
import jp.co.canon.rss.logmanager.repository.SiteRepository;
import jp.co.canon.rss.logmanager.util.CallRestAPI;
import jp.co.canon.rss.logmanager.vo.SiteVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Service()
public class AnalysisToolService {
    SiteRepository siteRepository;
    CallRestAPI callRestAPI = new CallRestAPI();

    public AnalysisToolService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public ResEquipmentsListDTO getAllMpaList() throws Exception {
        try {
            List<SiteVo> getAllSite = Optional.ofNullable(siteRepository.findAll())
                    .orElse(Collections.emptyList());
            if(getAllSite.isEmpty())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            Map<String, Map<String, List<String>>> companies = new HashMap<>();

            for (SiteVo site : getAllSite) {
                String GET_ALL_MPA_LIST_URL = String.format(ReqURLController.API_GET_ALL_MPA_LIST,
                        site.getCrasAddress(),
                        site.getCrasPort());

                ResponseEntity<?> response = callRestAPI.getRestAPI(GET_ALL_MPA_LIST_URL, ResEquipmentDTO[].class);
                List<ResEquipmentDTO> equipments = Arrays.asList((ResEquipmentDTO[]) response.getBody());
                for (ResEquipmentDTO equipment : equipments) {
                    if (!companies.containsKey(equipment.getUser_name())) {
                        List<String> equipmentNames = new ArrayList<String>();
                        equipmentNames.add(equipment.getEquipment_name());

                        Map<String, List<String>> fabs = new HashMap<>();
                        fabs.put(equipment.getFab_name(), equipmentNames);

                        companies.put(equipment.getUser_name(), fabs);
                    } else {
                        if (!companies.get(equipment.getUser_name()).containsKey(equipment.getFab_name())) {
                            List<String> equipmentNames = new ArrayList<String>();
                            equipmentNames.add(equipment.getEquipment_name());
                            companies.get(equipment.getUser_name()).put(equipment.getFab_name(), equipmentNames);
                        } else {
                            companies.get(equipment.getUser_name()).get(equipment.getFab_name()).add(equipment.getEquipment_name());
                        }
                    }
                }
            }

            ResEquipmentsListDTO resEquipmentsListDTO = new ResEquipmentsListDTO();
            return resEquipmentsListDTO.setEquipments(companies);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResLogTimeDTO getLogTime(String logName, String equipment) throws Exception {
        try {
            String[] findSite = equipment.split("_");
            ResSitesDetailDTO sitesDetail = siteRepository.findByCrasCompanyNameAndCrasFabName(findSite[0], findSite[1])
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            String GET_LOG_DATA_TIME_URL = String.format(ReqURLController.API_GET_LOG_DATA_TIME,
                    sitesDetail.getCrasAddress(),
                    sitesDetail.getCrasPort(),
                    logName,
                    equipment);

            ResponseEntity<?> response = callRestAPI.getRestAPI(GET_LOG_DATA_TIME_URL, ResLogDataTimeDTO.class);
            ResLogDataTimeDTO logDataTime = (ResLogDataTimeDTO) response.getBody();

            ResLogTimeDTO resLogTimeDTO = new ResLogTimeDTO()
                    .setEnd(logDataTime.getEnd()!=null ? logDataTime.getEnd().split(" ") : null)
                    .setStart(logDataTime.getStart()!=null ? logDataTime.getStart().split(" ") : null);

            return resLogTimeDTO;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResLogData getLogData(String equipment, String logName, String start, String end) throws Exception {
        String[] findSite = equipment.split("_");
        ResSitesDetailDTO sitesDetail = siteRepository.findByCrasCompanyNameAndCrasFabName(findSite[0], findSite[1])
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String GET_LOG_DATA_URL = String.format(ReqURLController.API_GET_LOG_DATA,
                sitesDetail.getCrasAddress(),
                sitesDetail.getCrasPort(),
                logName,
                start,
                end,
                equipment);

        ResponseEntity<?> response = callRestAPI.getRestAPI(GET_LOG_DATA_URL, ResLogData.class);
        ResLogData logData = (ResLogData) response.getBody();
        if(logData.getLogData() == null)
            logData.setLogData("");

        return logData;
    }
}
