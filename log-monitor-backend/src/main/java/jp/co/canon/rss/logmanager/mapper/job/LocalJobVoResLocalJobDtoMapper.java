package jp.co.canon.rss.logmanager.mapper.job;

import jp.co.canon.rss.logmanager.dto.job.ResLocalJobListDTO;
import jp.co.canon.rss.logmanager.vo.LocalJobVo;
import jp.co.canon.rss.logmanager.vo.SiteVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocalJobVoResLocalJobDtoMapper {
    LocalJobVoResLocalJobDtoMapper INSTANCE = Mappers.getMapper(LocalJobVoResLocalJobDtoMapper.class);

    default LocalJobVo mapResLocalJobDtoToVo(ResLocalJobListDTO resLocalJobListDTO, List<String> fileOriginalName) {
        SiteVo siteVo = new SiteVo()
                .setSiteId(resLocalJobListDTO.getSiteId());
        LocalJobVo localJobVo = new LocalJobVo()
                .setSiteId(resLocalJobListDTO.getSiteId())
                .setCollectStatus("notbuild")
                .setFileIndices(resLocalJobListDTO.getFileIndices())
                .setFileOriginalNames(fileOriginalName.toArray(new String[fileOriginalName.size()]))
                .setRegisteredDate(LocalDateTime.now())
                .setStop(false)
                .setSiteVoListLocal(siteVo);
        return localJobVo;
    }
}
