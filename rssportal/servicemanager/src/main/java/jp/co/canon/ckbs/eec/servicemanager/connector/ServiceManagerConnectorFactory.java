package jp.co.canon.ckbs.eec.servicemanager.connector;

public interface ServiceManagerConnectorFactory {
    ServiceManagerConnector getConnector(String host, int port);
}
