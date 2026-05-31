package com.lmco.jsf.fmt.dataprovider.security.apikey.dto;

public class ClaimKeyRequest {

    private String approvedEmail;
    private String claimCode;

    public String getApprovedEmail() {
        return approvedEmail;
    }

    public void setApprovedEmail(String approvedEmail) {
        this.approvedEmail = approvedEmail;
    }

    public String getClaimCode() {
        return claimCode;
    }

    public void setClaimCode(String claimCode) {
        this.claimCode = claimCode;
    }
}
