package jp.co.canon.rss.logmanager.service;

import jp.co.canon.rss.logmanager.config.ReqURLController;
import jp.co.canon.rss.logmanager.dto.job.ResRemoteJobListDTO;
import jp.co.canon.rss.logmanager.dto.site.*;
import jp.co.canon.rss.logmanager.exception.ConnectionFailException;
import jp.co.canon.rss.logmanager.mapper.site.SiteVoSiteDtoMapper;
import jp.co.canon.rss.logmanager.repository.RemoteJobRepository;
import jp.co.canon.rss.logmanager.repository.SiteRepository;
import jp.co.canon.rss.logmanager.system.ClientManageService;
import jp.co.canon.rss.logmanager.util.CallRestAPI;
import jp.co.canon.rss.logmanager.vo.SiteVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SiteService {
	private final static String PLAN_TYPE_FTP = "ftp";
	private final static String PLAN_TYPE_VFTP_COMPAT = "vftp_compat";
	private final static String PLAN_TYPE_VFTP_SSS = "vftp_sss";

	private SiteRepository siteRepositoryService;
	private RemoteJobRepository remoteJobRepository;
	private CallRestAPI callRestAPI = new CallRestAPI();

	public SiteService(SiteRepository siteRepository, RemoteJobRepository remoteJobRepository) {
		this.siteRepositoryService = siteRepository;
		this.remoteJobRepository = remoteJobRepository;
	}

	public List<ResPlanDTO> getPlanList(Integer siteId) throws Exception {
		try {
			SiteVo getSite = siteRepositoryService.findById(siteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

			String GET_PLAN_LIST_URL = String.format(ReqURLController.API_GET_PLAN_LIST,
				getSite.getCrasAddress(),
				getSite.getCrasPort(),
				getSite.getRssAddress(),
				getSite.getRssPort(),
				getSite.getRssUserName(),
				getSite.getRssPassword());

			ResponseEntity<?> response = callRestAPI.getRestAPI(GET_PLAN_LIST_URL, ResRCPlanDTO[].class);
			List<ResRCPlanDTO> rcPlanList = (List<ResRCPlanDTO>) response.getBody();
			List<ResPlanDTO> newPlanList = new ArrayList<>();

			for (ResRCPlanDTO plan : rcPlanList) {
				ResPlanDTO data = new ResPlanDTO()
					.setPlanId(plan.getPlanId())
					.setPlanName(plan.getPlanName())
					.setPlanType(plan.getPlanType())
					.setMachineNames(plan.getMachineNames())
					.setStatus(plan.getStatus())
					.setDescription(plan.getDescription());

				if (plan.getPlanType().equals(PLAN_TYPE_FTP)) {
					data.setTargetNames(plan.getCategoryNames());
				} else if (plan.getPlanType().equals(PLAN_TYPE_VFTP_COMPAT)) {
					List<String> newTarget = new ArrayList<>();
					for (String command : plan.getCommands()) {
						if (command.equals("none")) {
							newTarget.add(String.format("get %s_%s.log", plan.getFrom(), plan.getTo()));
						} else {
							newTarget.add(String.format("get " + command + ".log", plan.getFrom(), plan.getTo()));
						}
					}
				} else if (plan.getPlanType().equals(PLAN_TYPE_VFTP_SSS)) {
					List<String> newTarget = new ArrayList<>();
					for (String command : plan.getCommands()) {
						newTarget.add(String.format("cd " + command, plan.getFrom(), plan.getTo()));
					}
				}
			}
			return newPlanList;
		} catch (ResponseStatusException e) {
			log.error(e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<ResSitesNamesDTO> getSitesNamesList(Boolean notAdded) {
		List<ResSitesNamesDTO> resultSitesNamesList = Optional
				.ofNullable(siteRepositoryService.findBy())
				.orElse(Collections.emptyList());
		if(resultSitesNamesList==null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		Collections.sort(resultSitesNamesList);
		if(notAdded) {
			List<ResRemoteJobListDTO> remoteJobList = Optional
					.ofNullable(remoteJobRepository.findBy(Sort.by(Sort.Direction.DESC, "jobId")))
					.orElse(Collections.emptyList());
			if(remoteJobList.isEmpty())
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			List<ResSitesNamesDTO> jobSiteNamesList = new ArrayList<>();

			for (ResRemoteJobListDTO job : remoteJobList) {
				ResSitesNamesDTO sitesNames = new ResSitesNamesDTO(job.getSiteId(), job.getCompanyName(), job.getFabName());
				jobSiteNamesList.add(sitesNames);
			}
			resultSitesNamesList.removeAll(jobSiteNamesList);
		}
		return resultSitesNamesList;
	}

	public ResDuplicateErrDTO checkDuplicate(ReqAddSiteDTO newSite) {
		try {
			ResDuplicateErrDTO resDuplicateErrDTO = new ResDuplicateErrDTO();

			// Duplicate check(crasSiteName, crasAddress)
			List<ResSitesDetailDTO> resultSites = Optional
					.ofNullable(siteRepositoryService.findBy(Sort.by(Sort.Direction.ASC, "siteId")))
					.orElse(Collections.emptyList());
			if(resultSites.isEmpty())
				resDuplicateErrDTO.setErrorCode(200);

			for (ResSitesDetailDTO site : resultSites) {
				if (site.getCrasCompanyName().equals(newSite.getCrasCompanyName()) && site.getCrasFabName().equals(newSite.getCrasFabName())) {
					resDuplicateErrDTO.setErrorCode(400001);
					resDuplicateErrDTO.setErrorMsg("Duplicate Site Name '" + newSite.getCrasCompanyName() + "_" + newSite.getCrasFabName() + "' of Cras Server Setting");
				} else if (site.getCrasAddress().equals(newSite.getCrasAddress())) {
					resDuplicateErrDTO.setErrorCode(400002);
					resDuplicateErrDTO.setErrorMsg("Duplicate Address '" + newSite.getCrasAddress() + "' of Cras Server Setting");
				}
				else {
					resDuplicateErrDTO.setErrorCode(200);
				}
			}
			return resDuplicateErrDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResSiteIdDTO addSite(ReqAddSiteDTO reqAddSiteDTO) {
		try {
			ResSiteIdDTO resSiteIdDTO = new ResSiteIdDTO()
					.setSiteId(siteRepositoryService.save(SiteVoSiteDtoMapper.INSTANCE.toEntity(reqAddSiteDTO)).getSiteId());
			return resSiteIdDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResSiteIdDTO editSite(int siteId, ReqAddSiteDTO reqAddSiteDTO) {
		try {
			SiteVo getSiteInfo = siteRepositoryService.findById(siteId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

			ResSiteIdDTO resSiteIdDTO = new ResSiteIdDTO()
					.setSiteId(siteRepositoryService.save(SiteVoSiteDtoMapper.INSTANCE.updateFromDtoE(reqAddSiteDTO, getSiteInfo)).getSiteId());
			return resSiteIdDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResSiteJobStatus getSiteJobStatus(int siteId) {
		List<ResRemoteJobListDTO> remoteJobList = Optional
				.ofNullable(remoteJobRepository.findBy(Sort.by(Sort.Direction.DESC, "jobId")))
				.orElse(Collections.emptyList());
		if(remoteJobList.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		ResSiteJobStatus resSiteJobStatus = new ResSiteJobStatus();
		for (ResRemoteJobListDTO job : remoteJobList) {
			if (job.getSiteId() == siteId) {
				resSiteJobStatus.setStatus(job.isStop() ? "stopped" : "running");
				return resSiteJobStatus;
			}
		}
		// If there is no registered job, status is set to "none".
		resSiteJobStatus.setStatus("none");
		return resSiteJobStatus;
	}

	public void deleteSite(int siteId) {
		try {
			SiteVo delSite = siteRepositoryService.findById(siteId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			siteRepositoryService.delete(delSite);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResConnectDTO crasConnection(ReqConnectionCrasDTO reqConnectionCrasDTO) {
		try {
			ClientManageService client = new ClientManageService();
			String GET_CRAS_CONNECTION_URL = String.format(ReqURLController.API_GET_CRAS_CONNECTION,
					reqConnectionCrasDTO.getCrasAddress(),
					reqConnectionCrasDTO.getCrasPort());
			ResConnectDTO resConnectDTO = new ResConnectDTO();
			if(client.connectCheck(GET_CRAS_CONNECTION_URL)==200)
				resConnectDTO.setResult("ok");
			else
				throw new ConnectionFailException();
			return resConnectDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResConnectDTO rssConnection(ReqConnectionRssDTO reqConnectionRssDTO) {
		try {
			ClientManageService client = new ClientManageService();
			String GET_RSS_CONNECTION_URL = String.format(ReqURLController.API_GET_RSS_CONNECTION,
					reqConnectionRssDTO.getCrasAddress(),
					reqConnectionRssDTO.getCrasPort(),
					reqConnectionRssDTO.getRssAddress(),
					reqConnectionRssDTO.getRssPort(),
					reqConnectionRssDTO.getRssUserName(),
					reqConnectionRssDTO.getRssPassword());
			ResConnectDTO resConnectDTO = new ResConnectDTO();
			if(client.connectCheck(GET_RSS_CONNECTION_URL)==200)
				resConnectDTO.setResult("ok");
			else
				throw new ConnectionFailException();
			return resConnectDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResConnectDTO emailConnection(ReqConnectionEmailDTO reqConnectionEmailDTO) {
		try {
			Socket connectCheckResult = new Socket(reqConnectionEmailDTO.getEmailAddress(), reqConnectionEmailDTO.getEmailPort());
			connectCheckResult.isConnected();
			ResConnectDTO resConnectDTO = new ResConnectDTO().setResult("ok");
			return resConnectDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
