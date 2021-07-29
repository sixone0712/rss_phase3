package jp.co.canon.ckbs.eec.service;

public class StopChecker {
    boolean stopped = false;

    public boolean isStopped(){
        return stopped;
    }

    public void setStopped(){
        stopped = true;
    }
}
