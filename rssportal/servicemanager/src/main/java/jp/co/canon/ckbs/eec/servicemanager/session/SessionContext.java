package jp.co.canon.ckbs.eec.servicemanager.session;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SessionContext {
    private boolean authenticated = false;
    private String username = "";
    private List<String> permission;
}


