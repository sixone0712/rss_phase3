package jp.co.canon.cks.eec.fs.rssportal.downloadlist;

import jp.co.canon.ckbs.eec.fs.configuration.Category;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.CategoryList;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineList;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.Machine;
import jp.co.canon.cks.eec.fs.rssportal.controller.PlanController;
import jp.co.canon.cks.eec.fs.rssportal.dao.CollectionPlanDao;
import jp.co.canon.cks.eec.fs.rssportal.model.plans.RSSPlanFileList;
import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DownloadListControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final DownloadListController downloadController;
    private final DownloadListService downloadService;
    private final CollectionPlanDao planDao;
    private final FileServiceManageConnectorFactory connectorFactory;
    private final PlanController planController;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Autowired
    public DownloadListControllerTest(DownloadListController controller,
                                      DownloadListService downloadService,
                                      CollectionPlanDao planDao,
                                      FileServiceManageConnectorFactory connectorFactory,
                                      PlanController planController) {
        this.downloadController = controller;
        this.downloadService = downloadService;
        this.planDao = planDao;
        this.connectorFactory = connectorFactory;
        this.planController = planController;
    }

    @Test
    void isReady() {
        log.info("test isReady()");
        assertTrue(downloadService.isReady());
    }

    @Test
    void getList() {
        log.info("test getList()");
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity resp = downloadController.getList(request, "0", "false");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void delete() throws Exception {
        log.info("test delete()");

        // generate parameters
        Map<String, Object> param = createAddPlanRequestBody();

        // add a plan
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/rest/plan/add");
        ResponseEntity resp = planController.addPlan(request, param);
        assertNotNull(resp);
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("planID"));
        int planId = (int) body.get("planID");
        assertNotEquals(planId, -1);

        request.setServletPath("");
        request.setServletPath("/rest/plan/stop");
        planController.changePlanStatus(request, String.valueOf(planId), "stop");

        // insert a row temporarily.
        DownloadListVo item = new DownloadListVo(new Timestamp(System.currentTimeMillis()),
                "new", planId, "/test/tmp");
        assertTrue(downloadService.insert(item));

        // get a download list index
        request.setServletPath("/"+planId+"/filelists");
        ResponseEntity entity = downloadController.getList(request, String.valueOf(planId), "true");
        assertNotNull(entity);
        List<RSSPlanFileList> list = (List<RSSPlanFileList>) getParam(entity, "lists");
        assertNotNull(list);
        assertTrue(list.size()>0);

        assertNotNull(list);
        assertNotEquals(list.size(), 0);

        request.setServletPath("/"+planId+"/filelists/"+list.get(0).getFileId());
        ResponseEntity entity1 = downloadController.getfile(request,  String.valueOf(list.get(0).getPlanId()), String.valueOf(list.get(0).getFileId()));
        assertEquals(HttpStatus.OK, entity1.getStatusCode());

        entity1 = downloadController.getfile(request,  null, null);
        assertEquals(HttpStatus.NOT_FOUND, entity1.getStatusCode());

        // request delete an item
        assertEquals(HttpStatus.NOT_FOUND, downloadController.delete(request, ":)", ":)").getStatusCode());
        assertEquals(downloadController.delete(request,
                String.valueOf(list.get(0).getPlanId()), String.valueOf(list.get(0).getFileId())).getStatusCode(), HttpStatus.OK);

        // check that the item was deleted
        downloadController.getList(request, String.valueOf(list.get(0).getPlanId()), String.valueOf(list.get(0).getFileId()));

        // delete the plan
        request.setServletPath("/rest/plan//delete");
        assertEquals(planController.deletePlan(request, String.valueOf(planId)).getStatusCode(), HttpStatus.OK);
    }

    private Map<String, Object> createAddPlanRequestBody() throws Exception {

        assertNotNull(connectorFactory);
        FileServiceManageConnector connector = connectorFactory.getConnector(fileServiceAddress);
        assertNotNull(connector);

        MachineList machineList = connector.getMachineList();
        assertNotNull(machineList);
        assertTrue(machineList.getMachineCount()>0);

        CategoryList categoryList = connector.getCategoryList();
        assertNotNull(categoryList);
        assertTrue(categoryList.getCategories().length>0);

        Map<String, Object> param = new HashMap<>();

        param.put("planName", "___ted_test___");
        param.put("planType", "ftp");

        List<String> _fabs = new ArrayList<>();
        List<String> _machines = new ArrayList<>();
        for(int i=0; i<1; ++i) {
            Machine machine = machineList.getMachines()[i];
            _machines.add(machine.getMachineName());
//            _fabs.add(machine.getFabName());
        }
        param.put("fabNames", _fabs);
        param.put("machineNames", _machines);

        List<String> _categoryNames = new ArrayList<>();
        List<String> _categoryCodes = new ArrayList<>();
        for(int i=1; i<3; ++i) {
            Category category = categoryList.getCategories()[i];
            _categoryNames.add(category.getCategoryName());
            _categoryCodes.add(category.getCategoryCode());
        }
        param.put("categoryNames", _categoryNames);
        param.put("categoryCodes", _categoryCodes);

        long cur = System.currentTimeMillis();
        long week = 7*24*3600000;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        param.put("start", format.format(new Date(cur)));
        param.put("from", format.format(new Date(cur-week)));
        param.put("to", format.format(new Date(cur+week)));
        param.put("type", "cycle");
        param.put("interval", "3600000");
        param.put("description", "");
        return param;
    }

    private Object getParam(ResponseEntity entity, String key) {
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertNotNull(body);
        return body.get(key);
    }

}