package jp.co.canon.cks.eec.fs.rssportal.controller;

import jp.co.canon.ckbs.eec.fs.configuration.Category;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnector;
import jp.co.canon.ckbs.eec.fs.manage.FileServiceManageConnectorFactory;
import jp.co.canon.ckbs.eec.fs.manage.service.CategoryList;
import jp.co.canon.ckbs.eec.fs.manage.service.MachineList;
import jp.co.canon.ckbs.eec.fs.manage.service.configuration.Machine;
import jp.co.canon.cks.eec.fs.rssportal.downloadlist.DownloadListService;
import jp.co.canon.cks.eec.fs.rssportal.downloadlist.DownloadListVo;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.AccessToken;
import jp.co.canon.cks.eec.fs.rssportal.model.auth.RefreshToken;
import jp.co.canon.cks.eec.fs.rssportal.model.plans.RSSPlanCollectionPlan;
import jp.co.canon.cks.eec.fs.rssportal.service.CollectPlanService;
import jp.co.canon.cks.eec.fs.rssportal.service.JwtService;
import jp.co.canon.cks.eec.fs.rssportal.vo.CollectPlanVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlanControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PlanController planController;
    private final LoginController loginController;
    private final DownloadListService downloadListService;
    private final CollectPlanService collectPlanService;
    private final FileServiceManageConnectorFactory connectorFactory;
    private FileServiceManageConnector connector;

    @Value("${rssportal.file-service-manager.addr}")
    private String fileServiceAddress;

    @Autowired
    public PlanControllerTest(PlanController planController,
                              LoginController loginController,
                              FileServiceController fileServiceController,
                              DownloadListService downloadListService,
                              CollectPlanService collectPlanService,
                              FileServiceManageConnectorFactory connectorFactory) {
        this.planController = planController;
        this.loginController = loginController;
        this.downloadListService = downloadListService;
        this.collectPlanService = collectPlanService;
        this.connectorFactory = connectorFactory;
    }

    @PostConstruct
    void __init() {
        assertNotNull(connectorFactory);
        connector = connectorFactory.getConnector(fileServiceAddress);
        assertNotNull(connector);
    }

    @Test
    void addPlan() throws Exception {
        printSeparator("addPlan");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/rest/plan/add");


        // first off, test fail cases.
        assertEquals(planController.addPlan(request, null).getStatusCode(), HttpStatus.BAD_REQUEST);
        Map failParam = new HashMap<>();
        failParam.put("unused", "value");
        assertEquals(planController.addPlan(request,failParam).getStatusCode(), HttpStatus.BAD_REQUEST);

        // generate parameters
        Map<String, Object> param = createAddPlanRequestBody();

        // add a plan
        ResponseEntity resp = planController.addPlan(request, param);
        assertNotNull(resp);
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("planID"));
        int planId = (int) body.get("planID");
        assertNotEquals(planId, -1);

        // delete the plan
        request.setServletPath("/rest/plan//delete");
        assertEquals(planController.deletePlan(request, String.valueOf(planId)).getStatusCode(), HttpStatus.OK);

        // test ParseException
        printSeparator("ParseException");
        param.remove("start");
        param.put("start", "--!@$!@$412412448585858jfhfhf");
        ResponseEntity resp1 = planController.addPlan(request, param);
        assertEquals(resp1.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void addInfinitePlan() throws Exception {

        printSeparator("addInfinitePlan");

        // Generate parameters
        Map<String, Object> param = createAddPlanRequestBody();

        // Set the end to blank.
        param.remove("to");
        param.put("to", "");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/rest/plan/add");

        planController.addPlan(request, param);
    }

    @Test
    void listPlan1() throws Exception {
        printSeparator("listPlan1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("listPlan");
        planController.listPlan(request, new HashMap<>());
    }
    
    @Test
    void listPlan() throws Exception {
        printSeparator("listPlan");
        MockHttpServletRequest request = new MockHttpServletRequest();

        // fail cases.
        request.setServletPath("list");
        assertEquals(planController.listPlan(request, null).getStatusCode(), HttpStatus.BAD_REQUEST);

        int[] planIds = new int[3];
        // add plans
        request.setServletPath("/rest/plan/add");
        Map<String, Object> param = createAddPlanRequestBody();
        for(int i=0; i<planIds.length; ++i) {
            ResponseEntity resp = planController.addPlan(request, param);
            assertEquals(resp.getStatusCode(), HttpStatus.OK);
            planIds[i] = (int) getResponseBody(resp,"planID");
            assertNotEquals(planIds[i], -1);
        }

        // get a plan list
        ResponseEntity resp = planController.listPlan(request, param);  // no need to think of parameters
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        List<RSSPlanCollectionPlan> plans = (List<RSSPlanCollectionPlan>) getResponseBody(resp, "lists");
        assertTrue(plans.size()>=3);

        assertFalse(collectPlanService.stopPlan(-1));
        assertFalse(collectPlanService.restartPlan(-1));
        collectPlanService.deletePlan(-1);

        // stop/restart
        assertTrue(collectPlanService.stopPlan(planIds[0]));
        assertTrue(collectPlanService.stopPlan(planIds[0]));
        assertTrue(collectPlanService.restartPlan(planIds[0]));

        // delete the plans
        for(int i=0; i<planIds.length; ++i)
            assertTrue(collectPlanService.stopPlan(planIds[i]));

        Map<String, Object> listParam = new HashMap<>();
        listParam.put("withExpired", "true");
        for(int i=0; i<planIds.length; ++i) {
            loop_top:
            while(true) {
                request.setServletPath("listPlan");
                ResponseEntity responseEntity = planController.listPlan(request, listParam);
                assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
                List<RSSPlanCollectionPlan> _plans = (List<RSSPlanCollectionPlan>) getResponseBody(resp, "lists");
                for(RSSPlanCollectionPlan plan: _plans) {
                    if(plan.getPlanId()==planIds[i] &&
                            !plan.getDetailedStatus().equalsIgnoreCase("collecting")) {
                        break loop_top;
                    }
                }
                Thread.sleep(1000);
            }
            request.setServletPath("/rest/plan//delete");
            assertEquals(planController.deletePlan(request, String.valueOf(planIds[i])).getStatusCode(), HttpStatus.OK);
        }
    }

    private Object getResponseBody(ResponseEntity response, String paramName) {
        assertNotNull(response);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey(paramName));
        return body.get(paramName);
    }

    @Test
    @Timeout(300)
    void deletePlan() throws Exception {
        printSeparator("deletePlan");
        // deletePlan() method will be tested many place in this file.
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setServletPath("/rest/plan/delete");
        assertEquals(planController.deletePlan(request, "").getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void download() throws Exception {
        printSeparator("download");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Field field = PlanController.class.getDeclaredField("jwtService");
        assertNotNull(field);
        field.setAccessible(true);
        field.set(planController, dummyJwtService);

        request.setServletPath("/rest/plan/download");
        assertEquals(planController.download(request, response, "", "123").getStatusCode(), HttpStatus.UNAUTHORIZED);

        // get rid of injected session and replace it dummy which has username in its context
        MockHttpSession session = new MockHttpSession();

        // generate parameters
        Map<String, Object> param = createAddPlanRequestBody();

        // add a plan
        request.setServletPath("/rest/plan/add");
        ResponseEntity resp = planController.addPlan(request, param);
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        int planId = (int) getResponseBody(resp, "planID");
        assertNotEquals(planId, -1);

        // wait the first collecting done
        int downloadId;
        while(true) {
            List<DownloadListVo> list = downloadListService.getList(planId);
            assertNotNull(list);
            if (list.size() > 0) {
                DownloadListVo item = list.get(0);
                downloadId = item.getId();
                assertNotEquals(downloadId, 0);
                assertNotNull(item.getPath());
                break;
            }
            Thread.sleep(1000);
        }

        request.setServletPath("/rest/plan/download");
        assertEquals(planController.download(request, response, String.valueOf(downloadId), "123").getStatusCode(), HttpStatus.OK);

        request.setServletPath("/rest/plan/delete");
        assertEquals(planController.deletePlan(request, String.valueOf(planId)).getStatusCode(), HttpStatus.OK);
    }

    @Test
    void modify() throws Exception {
        printSeparator("modify");
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setServletPath("modify");
        assertEquals(planController.modify(request, "", null).getStatusCode(), HttpStatus.NOT_FOUND);

        // generate parameters
        Map<String, Object> param = createAddPlanRequestBody();

        // add a plan
        request.setServletPath("/rest/plan/add");
        ResponseEntity addPlanResponse = planController.addPlan(request, param);
        assertEquals(addPlanResponse.getStatusCode(), HttpStatus.OK);
        int planId = (int) getResponseBody(addPlanResponse, "planID");
        assertNotEquals(planId, -1);

        // get a plan list
        ResponseEntity listResponse = planController.listPlan(request, param);
        assertEquals(listResponse.getStatusCode(), HttpStatus.OK);
        List<RSSPlanCollectionPlan> list = (List<RSSPlanCollectionPlan>) getResponseBody(listResponse, "lists");
        RSSPlanCollectionPlan plan = null;
        for(RSSPlanCollectionPlan p: list) {
            if(p.getPlanId()==planId) {
                plan = p;
                break;
            }
        }
        assertNotNull(plan);

        // pause, restart and then pause again
        request.setServletPath("/rest/plan/stop");
        planController.changePlanStatus(request, String.valueOf(planId), "stop");

        // when the plan is on work, it might not be able to stop immediately.
        Thread.sleep(3000);
        request.setServletPath("/rest/plan/restart");
        planController.changePlanStatus(request, String.valueOf(planId), "restart");
        Thread.sleep(3000);
        request.setServletPath("/rest/plan/stop");
        planController.changePlanStatus(request, String.valueOf(planId), "stop");
        Thread.sleep(3000);

        param.put("planId", "this is modified plan");
        request.setServletPath("/rest/plan/modify");
        ResponseEntity modifyResponse = planController.modify(request, String.valueOf(planId), param);
        assertEquals(modifyResponse.getStatusCode(), HttpStatus.OK);

        // delete the plan
        request.setServletPath("/rest/plan//delete");
        assertEquals(planController.deletePlan(request, String.valueOf(planId)).getStatusCode(), HttpStatus.OK);
    }

    @Test
    void stopPlan() {
        printSeparator("stopPlan");
        // stopPlan() method will be tested another test case in this file.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/rest/plan/stop");
        assertEquals(planController.changePlanStatus(request, "", "stop").getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void restartPlan() {
        printSeparator("restartPlan");
        // restartPlan() method will be tested another test case in this file.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/rest/plan/restart");
        assertEquals(planController.changePlanStatus(request, "", "restart").getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void createDownloadFilename() throws Exception {
        printSeparator("createDownloadFilename");

        assertNotNull(loginController);

        Method method = PlanController.class.getDeclaredMethod("createDownloadFilename",
                CollectPlanVo.class, DownloadListVo.class, String.class);
        assertNotNull(method);
        method.setAccessible(true);

        Field field = planController.getClass().getDeclaredField("jwtService");
        assertNotNull(field);
        field.setAccessible(true);
        field.set(planController, dummyJwtService);

        CollectPlanVo plan = new CollectPlanVo();
        DownloadListVo item = new DownloadListVo();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("login");
        ResponseEntity response = loginController.login(request, "user", "c4ca4238a0b923820dcc509a6f75849b");
        assertEquals(response.getStatusCode(), HttpStatus.OK);

        assertNull(method.invoke(planController, plan, item, "123123"));

        plan.setFab("");
        item.setCreated(new Timestamp(System.currentTimeMillis()));
        assertNotNull(method.invoke(planController, plan, item, "123123"));

        plan.setFab("fab1,fab2");
        assertNotNull(method.invoke(planController, plan, item, "123123"));
    }

    @Test
    void test() throws NoSuchFieldException, IllegalAccessException {
        printSeparator("test");
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setServletPath("/-1");
        ResponseEntity entity = planController.getPlan(request, "-1");
        assertNotNull(entity);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());

        request.setServletPath("/null");
        entity = planController.getPlan(request, null);
        assertNotNull(entity);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());

        Field field = PlanController.class.getDeclaredField("jwtService");
        assertNotNull(field);
        field.setAccessible(true);
        field.set(planController, dummyJwtService);

        request.setServletPath("/storage/-1");
        entity = planController.download(request, new MockHttpServletResponse(), "-1", "123");
        assertNotNull(entity);
        //assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    private Map<String, Object> createAddPlanRequestBody() throws Exception {

        assertNotNull(connectorFactory);
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
        for(int i=0; i<2; ++i) {
            Category category = categoryList.getCategories()[i];
            _categoryNames.add(category.getCategoryName());
            _categoryCodes.add(category.getCategoryCode());
        }
        param.put("categoryNames", _categoryNames);
        param.put("categoryCodes", _categoryCodes);

        long cur = System.currentTimeMillis();
        long aday = 24*3600000;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        param.put("start", format.format(new Date(cur)));
        param.put("from", format.format(new Date(cur-aday)));
        param.put("to", format.format(new Date(cur+aday)));
        param.put("type", "cycle");
        param.put("interval", "3600000");
        param.put("description", "");
        return param;
    }

    JwtService dummyJwtService = new JwtService() {

        AccessToken accessToken;

        @Override
        public <T> String create(T data, String subject) {
            return null;
        }

        @Override
        public String getCurrentAccessToken() {
            return null;
        }

        @Override
        public String getCurAccTokenUserName() {
            return "user";
        }

        @Override
        public int getCurAccTokenUserID() {
            return 0;
        }

        @Override
        public String getCurAccTokenUserPermission() {
            return null;
        }

        @Override
        public AccessToken decodeAccessToken(String jwt) {
            accessToken = new AccessToken();
            accessToken.setUserName("ted");
            return accessToken;
        }

        @Override
        public RefreshToken decodeRefreshToken(String jwt) {
            return null;
        }

        @Override
        public boolean isUsable(String jwt) {
            return false;
        }
    };

    @PreDestroy
    void _exit() {
        printSeparator("exit");
    }

    private void printSeparator(String name) {
        log.info("-------------------- "+name+" --------------------");
    }
}