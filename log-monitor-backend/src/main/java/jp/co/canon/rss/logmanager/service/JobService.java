package jp.co.canon.rss.logmanager.service;

import jp.co.canon.rss.logmanager.dto.job.*;
import jp.co.canon.rss.logmanager.manager.JobManager;
import jp.co.canon.rss.logmanager.manager.JobStatusException;
import jp.co.canon.rss.logmanager.manager.NotiManager;
import jp.co.canon.rss.logmanager.manager.NotiStatusException;
import jp.co.canon.rss.logmanager.mapper.job.LocalJobVoResLocalJobDtoMapper;
import jp.co.canon.rss.logmanager.mapper.job.RemoteJobVoResRemoteJobDtoMapper;
import jp.co.canon.rss.logmanager.repository.*;
import jp.co.canon.rss.logmanager.vo.LocalJobFileIdVo;
import jp.co.canon.rss.logmanager.vo.LocalJobVo;
import jp.co.canon.rss.logmanager.vo.RemoteJobVo;
import jp.co.canon.rss.logmanager.vo.address.JobAddressBookEntity;
import jp.co.canon.rss.logmanager.vo.address.JobGroupBookEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service()
public class JobService {
	RemoteJobRepository remoteJobRepository;
	LocalJobRepository localJobRepository;
	LocalJobFileIdVoRepository localJobFileIdVoRepository;
	JobAddressBookRepository jobAddressBookRepository;
	JobGroupBookRepository jobGroupBookRepository;
	AddressBookRepository addressBookRepository;
	GroupBookRepository groupBookRepository;
	MailContextRepository mailContextRepository;
	JobManager job;
	NotiManager noti;

	public JobService(RemoteJobRepository remoteJobRepository,
					  LocalJobRepository localJobRepository, LocalJobFileIdVoRepository localJobFileIdVoRepository,
					  JobAddressBookRepository jobAddressBookRepository, JobGroupBookRepository jobGroupBookRepository,
					  AddressBookRepository addressBookRepository, GroupBookRepository groupBookRepository, MailContextRepository mailContextRepository,
					  JobManager job, NotiManager noti) {
		this.remoteJobRepository = remoteJobRepository;
		this.localJobRepository = localJobRepository;
		this.localJobFileIdVoRepository = localJobFileIdVoRepository;
		this.jobAddressBookRepository = jobAddressBookRepository;
		this.jobGroupBookRepository = jobGroupBookRepository;
		this.addressBookRepository = addressBookRepository;
		this.groupBookRepository = groupBookRepository;
		this.mailContextRepository = mailContextRepository;
		this.job = job;
		this.noti = noti;
	}

	public List<ResRemoteJobListDTO> getRemoteJobs() throws Exception {
		try {
			List<ResRemoteJobListDTO> resultRemoteList = Optional
				.ofNullable(remoteJobRepository.findBy(Sort.by(Sort.Direction.DESC, "jobId")))
				.orElse(Collections.emptyList());

			if (resultRemoteList == null)
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);

			for (ResRemoteJobListDTO remoteJob : resultRemoteList) {
				remoteJob.setCollectStatus(setRemoteJobStatus(remoteJob.getCollectStatus()));
				remoteJob.setErrorSummaryStatus(setRemoteJobStatus(remoteJob.getErrorSummaryStatus()));
				remoteJob.setCrasDataStatus(setRemoteJobStatus(remoteJob.getCrasDataStatus()));
				remoteJob.setMpaVersionStatus(setRemoteJobStatus(remoteJob.getMpaVersionStatus()));
			}
			return resultRemoteList;
		} catch (ResponseStatusException e) {
			log.error(e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String setRemoteJobStatus(String getStatus) {
		String setStatus = null;
		if (getStatus.equals("running"))
			setStatus = "processing";
		else if (getStatus.equals("success"))
			setStatus = "success";
		else if (getStatus.equals("error"))
			setStatus = "failure";
		else if (getStatus.isEmpty() || getStatus.equals("notbuild"))
			setStatus = "notbuild";

		return setStatus;
	}

	public ResRemoteJobDetailDTO getRemoteJobDetail(int remoteJobId) throws Exception {
		try {
			RemoteJobVo remoteJobVo = remoteJobRepository.findByJobId(remoteJobId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			ResRemoteJobDetailDTO resRemoteJobDetailDTO = RemoteJobVoResRemoteJobDtoMapper.INSTANCE.mapRemoteJobVoToDto(remoteJobVo);
			return resRemoteJobDetailDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResJobIdDTO addRemoteJob(ResRemoteJobDetailAddDTO resRemoteJobDetailDTO) {

		RemoteJobVo setRemoteJobVo = null;

		try {
			RemoteJobVo remoteJobVo = RemoteJobVoResRemoteJobDtoMapper.INSTANCE.mapResRemoteJobDtoToVo(resRemoteJobDetailDTO);
			setRemoteJobVo = remoteJobRepository.save(remoteJobVo);
			ResJobIdDTO resJobIdDTO = new ResJobIdDTO()
				.setJobId(setRemoteJobVo.getJobId());
			callAddJobAddressGroupBook(resRemoteJobDetailDTO, setRemoteJobVo);
			return resJobIdDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			if(!ObjectUtils.isEmpty(setRemoteJobVo) && setRemoteJobVo.getJobId() > 0) {
				remoteJobRepository.deleteById(setRemoteJobVo.getJobId());
			}
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void callAddJobAddressGroupBook(ResRemoteJobDetailAddDTO resRemoteJobDetailDTO, RemoteJobVo setRemoteJobVo) {
		addJobAddressGroupBook(resRemoteJobDetailDTO.getErrorSummary().getEmailBookIds(),
				setRemoteJobVo.getNotification().getMailContextVoError().getId(), 1);
		addJobAddressGroupBook(resRemoteJobDetailDTO.getErrorSummary().getGroupBookIds(),
				setRemoteJobVo.getNotification().getMailContextVoError().getId(), 2);
		addJobAddressGroupBook(resRemoteJobDetailDTO.getCrasData().getEmailBookIds(),
				setRemoteJobVo.getNotification().getMailContextVoCras().getId(), 1);
		addJobAddressGroupBook(resRemoteJobDetailDTO.getCrasData().getGroupBookIds(),
				setRemoteJobVo.getNotification().getMailContextVoCras().getId(), 2);
		addJobAddressGroupBook(resRemoteJobDetailDTO.getMpaVersion().getEmailBookIds(),
				setRemoteJobVo.getNotification().getMailContextVoVersion().getId(), 1);
		addJobAddressGroupBook(resRemoteJobDetailDTO.getMpaVersion().getGroupBookIds(),
				setRemoteJobVo.getNotification().getMailContextVoVersion().getId(), 2);
	}

	public void addJobAddressGroupBook(long [] ids, int mail_context_id, int flag) {	// flag 1 : address / 2 : group
		if(ids.length != 0) {
			for (long id : ids) {
				switch (flag) {
					case 1:
						JobAddressBookEntity jobAddressBookEntity = new JobAddressBookEntity()
								.setAddress(addressBookRepository.findById(id).get())
								.setMailContext(mailContextRepository.findById(mail_context_id).get());
						jobAddressBookRepository.save(jobAddressBookEntity);
						break;
					case 2:
						JobGroupBookEntity jobGroupBookEntity = new JobGroupBookEntity()
								.setGroup(groupBookRepository.findById(id).get())
								.setMailContext(mailContextRepository.findById(mail_context_id).get());
						jobGroupBookRepository.save(jobGroupBookEntity);
						break;
				}
			}
		}
	}

	public void deleteRemoteJob(int remoteJobId) {
		try {
			RemoteJobVo delJob = remoteJobRepository.findById(remoteJobId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			remoteJobRepository.delete(delJob);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResJobIdDTO editRemoteJob(int remoteJobId, ResRemoteJobDetailAddDTO resRemoteJobDetailDTO) {
		try {
			RemoteJobVo remoteJobVo = remoteJobRepository.findById(remoteJobId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

			RemoteJobVo reqRemoteJobVo = RemoteJobVoResRemoteJobDtoMapper.INSTANCE.mapResRemoteJobEditDtoToVo(remoteJobVo, resRemoteJobDetailDTO);

			RemoteJobVo setRemoteJobVo = remoteJobRepository.save(reqRemoteJobVo);
			ResJobIdDTO resJobIdDTO = new ResJobIdDTO()
					.setJobId(setRemoteJobVo.getJobId());
			callAddJobAddressGroupBook(resRemoteJobDetailDTO, setRemoteJobVo);

			return resJobIdDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResRemoteJobStatusDTO getStatusRemoteJob(int remoteJobId) {
		try {
			RemoteJobVo getJobInfo = remoteJobRepository.findById(remoteJobId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			ResRemoteJobStatusDTO resRemoteJobStatusDTO = new ResRemoteJobStatusDTO()
				.setStop(getJobInfo.isStop());

			return resRemoteJobStatusDTO;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResJobIdDTO runStopRemoteJob(int remoteJobId, String flag) {
		try {
			RemoteJobVo getJobInfo = remoteJobRepository.findById(remoteJobId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			ResJobIdDTO resJobIdDTO = new ResJobIdDTO();

			if (flag.equals("run")) {
				getJobInfo.setStop(Boolean.FALSE);

				job.requestRemoteStatus(remoteJobId);
				noti.requestRemoteNotiStatus(remoteJobId);
			} else if (flag.equals("stop")) {
				getJobInfo.setStop(Boolean.TRUE);
			}

			resJobIdDTO.setJobId(remoteJobRepository.save(getJobInfo).getJobId());
			return resJobIdDTO;
		} catch (Exception | JobStatusException | NotiStatusException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<ResLocalJobListDTO> localJobListDTOS() {
		try {
			List<ResLocalJobListDTO> resultLocalList = Optional
				.ofNullable(localJobRepository.findBy(Sort.by(Sort.Direction.DESC, "jobId")))
				.orElse(Collections.emptyList());

			if (resultLocalList == null)
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);

			for (ResLocalJobListDTO localJob : resultLocalList) {
				if (localJob.getCollectStatus().equals("running")) {
					localJob.setCollectStatus("processing");
				} else if (localJob.getCollectStatus().equals("error")) {
					localJob.setCollectStatus("failure");
				} else if (localJob.getCollectStatus().isEmpty()) {
					localJob.setCollectStatus("notbuild");
				}
			}
			return resultLocalList;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResJobIdDTO addLocalJob(ResLocalJobListDTO resLocalJobListDTO) {
		try {
			int[] fileIndex = resLocalJobListDTO.getFileIndices();
			List<String> fileOriginalName = new ArrayList<>();
			for (int fileIdx : fileIndex) {
				LocalJobFileIdVo localJobFileIdVo = localJobFileIdVoRepository.findById(fileIdx)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
				fileOriginalName.add(localJobFileIdVo.getFileOriginalName());
			}

			LocalJobVo localJobVo = LocalJobVoResLocalJobDtoMapper.INSTANCE.mapResLocalJobDtoToVo(resLocalJobListDTO, fileOriginalName);
			int localJobId = localJobRepository.save(localJobVo).getJobId();
			job.requestLocalStatus(localJobId);
			ResJobIdDTO resJobIdDTO = new ResJobIdDTO()
				.setJobId(localJobId);
			return resJobIdDTO;
		} catch (Exception | JobStatusException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void deleteLocalJob(int localJobId) {
		try {
			LocalJobVo delJob = localJobRepository.findById(localJobId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			localJobRepository.delete(delJob);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
