package jp.co.canon.cks.eec.fs.rssportal;

import jp.co.canon.cks.eec.fs.rssportal.controller.ServiceManagerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DBRunner implements ApplicationRunner {

	private final ServiceManagerController serviceManagerController;

	@Autowired
	public DBRunner(ServiceManagerController serviceManagerController) {
			this.serviceManagerController = serviceManagerController;
	}

	@Override public void run(ApplicationArguments args) {
		serviceManagerController.immigratePermission();
		serviceManagerController.saveFileUserInfoFromDB();
	}
}
