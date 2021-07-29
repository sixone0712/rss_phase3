package jp.co.canon.cks.eec.fs.rssportal.Defines;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestApiMachine {
	String machineName;
	String fabName;
	boolean ftpConnected;
	boolean vFtpConnected;
}
