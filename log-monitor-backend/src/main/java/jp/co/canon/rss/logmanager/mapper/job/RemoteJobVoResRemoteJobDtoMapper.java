package jp.co.canon.rss.logmanager.mapper.job;

import jp.co.canon.rss.logmanager.dto.address.AddressBookDTO;
import jp.co.canon.rss.logmanager.dto.job.ResMailContextDTO;
import jp.co.canon.rss.logmanager.dto.job.ResRemoteJobDetailAddDTO;
import jp.co.canon.rss.logmanager.dto.job.ResRemoteJobDetailDTO;
import jp.co.canon.rss.logmanager.util.AscendingObj;
import jp.co.canon.rss.logmanager.vo.NotificationVo;
import jp.co.canon.rss.logmanager.vo.RemoteJobVo;
import jp.co.canon.rss.logmanager.vo.SiteVo;
import jp.co.canon.rss.logmanager.vo.address.JobAddressBookEntity;
import jp.co.canon.rss.logmanager.vo.address.JobGroupBookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RemoteJobVoResRemoteJobDtoMapper {
    RemoteJobVoResRemoteJobDtoMapper INSTANCE = Mappers.getMapper(RemoteJobVoResRemoteJobDtoMapper.class);

    @Mapping(target="sendingTimes", expression = "java(mapSendingTimes(remoteJobVo.getNotification()))")
    @Mapping(target="isErrorSummary", expression = "java(mapIsErrorSummary(remoteJobVo.getNotification()))")
    @Mapping(target="isCrasData", expression = "java(mapIsCrasData(remoteJobVo.getNotification()))")
    @Mapping(target="isMpaVersion", expression = "java(mapIsMpaVersion(remoteJobVo.getNotification()))")
    @Mapping(target="errorSummary", expression = "java(mapErrorSummary(remoteJobVo.getNotification()))")
    @Mapping(target="crasData", expression = "java(mapCrasData(remoteJobVo.getNotification()))")
    @Mapping(target="mpaVersion", expression = "java(mapMpaVersion(remoteJobVo.getNotification()))")
    ResRemoteJobDetailDTO mapRemoteJobVoToDto(RemoteJobVo remoteJobVo);

    default String [] mapSendingTimes(NotificationVo notification) { return notification.getSendingTime(); }
    default Boolean mapIsErrorSummary(NotificationVo notification) { return notification.getErrorSummaryEnable(); }
    default Boolean mapIsCrasData(NotificationVo notification) { return notification.getCrasEnable(); }
    default Boolean mapIsMpaVersion(NotificationVo notification) { return notification.getVersionEnable(); }
    default ResMailContextDTO mapErrorSummary(NotificationVo notification) {
        ResMailContextDTO resMailContextDTO =
                MailContextVoResMailContextDTOMapper.INSTANCE.toDto(notification.getMailContextVoError());
        resMailContextDTO.setEmailBook(setAddress(notification, "ERR"));
        resMailContextDTO.setGroupBook(setGroup(notification, "ERR"));
        return resMailContextDTO;
    }
    default ResMailContextDTO mapCrasData(NotificationVo notification) {
        ResMailContextDTO resMailContextDTO =
                MailContextVoResMailContextDTOMapper.INSTANCE.toDto(notification.getMailContextVoCras());
        resMailContextDTO.setEmailBook(setAddress(notification, "CRAS"));
        resMailContextDTO.setGroupBook(setGroup(notification, "CRAS"));
        return resMailContextDTO;
    }
    default ResMailContextDTO mapMpaVersion(NotificationVo notification) {
        ResMailContextDTO resMailContextDTO =
                MailContextVoResMailContextDTOMapper.INSTANCE.toDto(notification.getMailContextVoVersion());
        resMailContextDTO.setEmailBook(setAddress(notification, "VERSION"));
        resMailContextDTO.setGroupBook(setGroup(notification, "VERSION"));
        return resMailContextDTO;
    }

    default List<AddressBookDTO> setAddress(NotificationVo notification, String flag) {
        Iterator<JobAddressBookEntity> jobAddressBookEntityIterator = null;
        List<AddressBookDTO> address = new ArrayList<>();

        if(flag.equals("ERR"))
            jobAddressBookEntityIterator = notification.getMailContextVoError().getAddress().iterator();
        else if(flag.equals("CRAS"))
            jobAddressBookEntityIterator = notification.getMailContextVoCras().getAddress().iterator();
        else if(flag.equals("VERSION"))
            jobAddressBookEntityIterator = notification.getMailContextVoVersion().getAddress().iterator();

        while (jobAddressBookEntityIterator.hasNext()) {
            JobAddressBookEntity jobAddressBookEntity = jobAddressBookEntityIterator.next();
            AddressBookDTO addressBookDTO = new AddressBookDTO(
                    jobAddressBookEntity.getAddress().getId(),
                    jobAddressBookEntity.getAddress().getName(),
                    jobAddressBookEntity.getAddress().getEmail(),
                    false
            );
            address.add(addressBookDTO);
        }
        Collections.sort(address, new AscendingObj());
        return address;
    }

    default List<AddressBookDTO> setGroup(NotificationVo notification, String flag) {
        Iterator<JobGroupBookEntity> jobGroupBookEntityIterator = null;
        List<AddressBookDTO> group = new ArrayList<>();

        if(flag.equals("ERR"))
            jobGroupBookEntityIterator = notification.getMailContextVoError().getGroup().iterator();
        else if(flag.equals("CRAS"))
            jobGroupBookEntityIterator = notification.getMailContextVoCras().getGroup().iterator();
        else if(flag.equals("VERSION"))
            jobGroupBookEntityIterator = notification.getMailContextVoVersion().getGroup().iterator();

        while (jobGroupBookEntityIterator.hasNext()) {
            JobGroupBookEntity jobGroupBookEntity = jobGroupBookEntityIterator.next();
            AddressBookDTO addressBookDTO = new AddressBookDTO(
                    jobGroupBookEntity.getGroup().getGid(),
                    jobGroupBookEntity.getGroup().getName(),
                    "",
                    true
            );
            group.add(addressBookDTO);
        }
        Collections.sort(group, new AscendingObj());
        return group;
    }

    default RemoteJobVo mapResRemoteJobDtoToVo(ResRemoteJobDetailAddDTO resRemoteJobDetailDTO) {
        SiteVo siteVo = new SiteVo()
                .setSiteId(resRemoteJobDetailDTO.getSiteId());
        NotificationVo notificationVo = new NotificationVo()
                .setSendingTime(resRemoteJobDetailDTO.getSendingTimes())
                .setErrorSummaryEnable(resRemoteJobDetailDTO.getIsErrorSummary())
                .setCrasEnable(resRemoteJobDetailDTO.getIsCrasData())
                .setVersionEnable(resRemoteJobDetailDTO.getIsMpaVersion())
                .setMailContextVoError(MailContextVoResMailContextAddDTOMapper.INSTANCE.toEntity(resRemoteJobDetailDTO.getErrorSummary()))
                .setMailContextVoCras(MailContextVoResMailContextAddDTOMapper.INSTANCE.toEntity(resRemoteJobDetailDTO.getCrasData()))
                .setMailContextVoVersion(MailContextVoResMailContextAddDTOMapper.INSTANCE.toEntity(resRemoteJobDetailDTO.getMpaVersion()));
        RemoteJobVo remoteJobVo = new RemoteJobVo()
                .setSiteId(resRemoteJobDetailDTO.getSiteId())
                .setPlanIds(resRemoteJobDetailDTO.getPlanIds())
                .setCollectStatus("notbuild")
                .setErrorSummaryStatus("notbuild")
                .setCrasDataStatus("notbuild")
                .setMpaVersionStatus("notbuild")
                .setStop(true)
                .setOwner(1)
                .setCreated(LocalDateTime.now())
                .setLastAction(LocalDateTime.now())
                .setNotification(notificationVo)
                .setSiteVoList(siteVo);

        return remoteJobVo;
    }

    default RemoteJobVo mapResRemoteJobEditDtoToVo(RemoteJobVo remoteJobVo, ResRemoteJobDetailAddDTO resRemoteJobDetailDTO) {
        remoteJobVo
                .setPlanIds(resRemoteJobDetailDTO.getPlanIds())
                .setLastAction(LocalDateTime.now());
        remoteJobVo.getNotification()
                .setSendingTime(resRemoteJobDetailDTO.getSendingTimes())
                .setErrorSummaryEnable(resRemoteJobDetailDTO.getIsErrorSummary())
                .setCrasEnable(resRemoteJobDetailDTO.getIsCrasData())
                .setVersionEnable(resRemoteJobDetailDTO.getIsMpaVersion())
                .setMailContextVoError(MailContextVoResMailContextAddDTOMapper.INSTANCE.toEntity(resRemoteJobDetailDTO.getErrorSummary()))
                .setMailContextVoCras(MailContextVoResMailContextAddDTOMapper.INSTANCE.toEntity(resRemoteJobDetailDTO.getCrasData()))
                .setMailContextVoVersion(MailContextVoResMailContextAddDTOMapper.INSTANCE.toEntity(resRemoteJobDetailDTO.getMpaVersion()));

        return remoteJobVo;
    }
}
