package org.tomass.dota.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DotaClientInfoList implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<DotaClientInfo> clientInfo;

    public DotaClientInfoList() {
        this(new ArrayList<>());
    }

    public DotaClientInfoList(List<DotaClientInfo> clientInfo) {
        this.clientInfo = clientInfo;
    }

    public List<DotaClientInfo> getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(List<DotaClientInfo> clientInfo) {
        this.clientInfo = clientInfo;
    }

}
