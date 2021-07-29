package jp.co.canon.rss.logmanager.mapper.history;

import jp.co.canon.rss.logmanager.dto.history.ReqHistoryDTO;
import jp.co.canon.rss.logmanager.dto.history.ResHistoryDTO;
import jp.co.canon.rss.logmanager.dto.job.ResRemoteJobDetailDTO;
import jp.co.canon.rss.logmanager.vo.NotificationVo;
import jp.co.canon.rss.logmanager.vo.RemoteJobVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReqHistoryDTOResHistoryConDTOMapper {
    ReqHistoryDTOResHistoryConDTOMapper INSTANCE = Mappers.getMapper(ReqHistoryDTOResHistoryConDTOMapper.class);

    @Mapping(target="name", expression = "java(mapName(reqHistoryDTO))")
    ResHistoryDTO mapResHistoryDTO(ReqHistoryDTO reqHistoryDTO);

    default String mapName(ReqHistoryDTO reqHistoryDTO) { return reqHistoryDTO.getTo_char(); }
}
