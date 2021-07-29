package jp.co.canon.cks.eec.fs.rssportal.model.plans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSSPlanCollectionPlanTest {

    @Test
    void test() {
        RSSPlanCollectionPlan plan = new RSSPlanCollectionPlan();
        final String smile = ":)";
        plan.setPlanId(10);
        assertEquals(10, plan.getPlanId());
        plan.setPlanType(smile);
        assertEquals(smile, plan.getPlanType());
        plan.setOwnerId(20);
        assertEquals(20, plan.getOwnerId());
        plan.setPlanName(smile);
        assertEquals(smile, plan.getPlanName());
        plan.setFabNames(smile);
        assertTrue(plan.getFabNames().contains(smile));
        plan.setMachineNames(smile);
        assertTrue(plan.getMachineNames().contains(smile));
        plan.setCategoryCodes(smile);
        assertTrue(plan.getCategoryCodes().contains(smile));
        plan.setCategoryNames(smile);
        assertTrue(plan.getCategoryNames().contains(smile));
        plan.setCommands(smile);
        assertTrue(plan.getCommands().contains(smile));
        plan.setType(smile);
        assertEquals(smile, plan.getType());
        plan.setInterval(smile);
        assertEquals(smile, plan.getInterval());
        plan.setDescription(smile);
        assertEquals(smile, plan.getDescription());
        plan.setStart(smile);
        assertEquals(smile, plan.getStart());
        plan.setFrom(smile);
        assertEquals(smile, plan.getFrom());
        plan.setTo(smile);
        assertEquals(smile, plan.getTo());
        plan.setLastCollection(smile);
        assertEquals(smile, plan.getLastCollection());
        plan.setStatus(smile);
        assertEquals(smile, plan.getStatus());
        plan.setDetailedStatus(smile);
        assertEquals(smile, plan.getDetailedStatus());
    }

}