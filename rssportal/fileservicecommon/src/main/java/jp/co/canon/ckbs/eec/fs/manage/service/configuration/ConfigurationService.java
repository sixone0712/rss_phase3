package jp.co.canon.ckbs.eec.fs.manage.service.configuration;

import jp.co.canon.ckbs.eec.fs.configuration.Category;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.category.CategoryInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.MpaInfo;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.structure.MpaInfoEx;

public interface ConfigurationService {
    Machine[] getMachineList();
    Category[] getCategories(String machineName);
    String[] getAllFileServiceHost();
    String getFileServiceHost(String machineName);
    String getOtsServiceHost(String otsName);
    String getFileServiceDownloadUrlPath(String machineName, String filePath);
    MpaInfoEx getMpaInfoEx(String machineName);
    MpaInfo getMpaInfo(String machineName);
    CategoryInfo getCategory(String categoryName);
}
