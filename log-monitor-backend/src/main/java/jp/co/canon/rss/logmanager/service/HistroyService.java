package jp.co.canon.rss.logmanager.service;

import jp.co.canon.rss.logmanager.config.ReqURLController;
import jp.co.canon.rss.logmanager.dto.history.ReqHistoryDTO;
import jp.co.canon.rss.logmanager.dto.history.ResHistoryDTO;
import jp.co.canon.rss.logmanager.mapper.history.ReqHistoryDTOResHistoryConDTOMapper;
import jp.co.canon.rss.logmanager.mapper.history.ReqHistoryDTOResHistoryErrCrasMapper;
import jp.co.canon.rss.logmanager.repository.LocalJobRepository;
import jp.co.canon.rss.logmanager.repository.RemoteJobRepository;
import jp.co.canon.rss.logmanager.util.CallRestAPI;
import jp.co.canon.rss.logmanager.vo.LocalJobVo;
import jp.co.canon.rss.logmanager.vo.RemoteJobVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service()
public class HistroyService {
    RemoteJobRepository remoteJobRepository;
    LocalJobRepository localJobRepository;
    CallRestAPI callRestAPI = new CallRestAPI();

    public HistroyService(RemoteJobRepository remoteJobRepository,
                      LocalJobRepository localJobRepository) {
        this.remoteJobRepository = remoteJobRepository;
        this.localJobRepository = localJobRepository;
    }

    public List<ResHistoryDTO>  getBuildLogList(String jobTypeFlag, int jobId, String flag) {
        RemoteJobVo getRemoteJobInfo = new RemoteJobVo();
        LocalJobVo getLocalJobInfo = new LocalJobVo();

        String GET_BUILD_LOG_LIST_URL = null;

        switch (jobTypeFlag) {
            case "remote" :
                getRemoteJobInfo = remoteJobRepository.findByJobId(jobId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                break;
            case "local" :
                getLocalJobInfo = localJobRepository.findByJobId(jobId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                break;
        }

        switch (flag) {
            case "convert":
                if(jobTypeFlag.equals("remote")) {
                    GET_BUILD_LOG_LIST_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_CONVERT,
                            getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort());
                }
                else if(jobTypeFlag.equals("local")) {
                    GET_BUILD_LOG_LIST_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_CONVERT,
                            getLocalJobInfo.getSiteVoListLocal().getCrasAddress(), getLocalJobInfo.getSiteVoListLocal().getCrasPort());
                }
                break;
            case "error":
                GET_BUILD_LOG_LIST_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_ERROR,
                        getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort());
                break;
            case "cras":
                GET_BUILD_LOG_LIST_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_CARS,
                        getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort());
                break;
            case "version":
                GET_BUILD_LOG_LIST_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_VERSION,
                        getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort());
                break;
        }

        ResponseEntity<?> response = callRestAPI.getRestAPI(GET_BUILD_LOG_LIST_URL, ReqHistoryDTO[].class);
        List<ReqHistoryDTO> buildLogList = (List<ReqHistoryDTO>) response.getBody();
        List<ResHistoryDTO> resHistoryDTOS = new ArrayList<>();

        if(flag.equals("convert") && jobTypeFlag.equals("remote")) {
            for (ReqHistoryDTO reqHistoryDTO : buildLogList) {
                if (reqHistoryDTO.getJob_type().equals("rapid")) {
                    ResHistoryDTO resHistoryDTO = ReqHistoryDTOResHistoryConDTOMapper.INSTANCE.mapResHistoryDTO(reqHistoryDTO);
                    resHistoryDTOS.add(resHistoryDTO);
                }
            }
        }
        else if(flag.equals("convert") && jobTypeFlag.equals("local")) {
            for (ReqHistoryDTO reqHistoryDTO : buildLogList) {
                if (reqHistoryDTO.getJob_type().equals("local")) {
                    ResHistoryDTO resHistoryDTO = ReqHistoryDTOResHistoryConDTOMapper.INSTANCE.mapResHistoryDTO(reqHistoryDTO);
                    resHistoryDTOS.add(resHistoryDTO);
                }
            }
        }
        else if(flag.equals("error") || flag.equals("cras")) {
            for (ReqHistoryDTO reqHistoryDTO : buildLogList) {
                ResHistoryDTO resHistoryDTO = ReqHistoryDTOResHistoryErrCrasMapper.INSTANCE.mapResHistoryDTO(reqHistoryDTO);
                resHistoryDTOS.add(resHistoryDTO);
            }
        }
        else if(flag.equals("version")) {
            for (ReqHistoryDTO reqHistoryDTO : buildLogList) {
                ResHistoryDTO resHistoryDTO = ReqHistoryDTOResHistoryConDTOMapper.INSTANCE.mapResHistoryDTO(reqHistoryDTO);
                resHistoryDTOS.add(resHistoryDTO);
            }
        }

        return resHistoryDTOS;
    }

    public String getBuildLogText(String jobTypeFlag, int jobId, String flag, String buildLogId) {
        RemoteJobVo getRemoteJobInfo = new RemoteJobVo();
        LocalJobVo getLocalJobInfo = new LocalJobVo();

        String GET_BUILD_LOG_TEXT_URL = null;

        switch (jobTypeFlag) {
            case "remote" :
                getRemoteJobInfo = remoteJobRepository.findByJobId(jobId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                break;
            case "local" :
                getLocalJobInfo = localJobRepository.findByJobId(jobId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                break;
        }

        switch (flag) {
            case "convert":
                if(jobTypeFlag.equals("remote")) {
                    GET_BUILD_LOG_TEXT_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_CONVERT_DETAIL,
                            getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort(), buildLogId);
                }
                else if(jobTypeFlag.equals("local")) {
                    GET_BUILD_LOG_TEXT_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_CONVERT_DETAIL,
                            getLocalJobInfo.getSiteVoListLocal().getCrasAddress(), getLocalJobInfo.getSiteVoListLocal().getCrasPort(), buildLogId);
                }break;
            case "error":
                GET_BUILD_LOG_TEXT_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_ERROR_DETAIL,
                        getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort(), buildLogId);
                break;
            case "cras":
                GET_BUILD_LOG_TEXT_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_CARS_DETAIL,
                        getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort(), buildLogId);
                break;
            case "version":
                GET_BUILD_LOG_TEXT_URL = String.format(ReqURLController.API_GET_BUILD_LOG_LIST_VERSION_DETAIL,
                        getRemoteJobInfo.getSiteVoList().getCrasAddress(), getRemoteJobInfo.getSiteVoList().getCrasPort(), buildLogId);
                break;
        }

        ResponseEntity<?> response = callRestAPI.getRestAPI(GET_BUILD_LOG_TEXT_URL, String.class);

        return (String) response.getBody();
    }
}
