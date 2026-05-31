package com.lmco.jsf.fmt.dataprovider.aircraft.model;

/**
 * TEMPORARY CODEX WORKSPACE SHIM.
 *
 * Placeholder used only to satisfy compilation while the real aircraft model is
 * absent from this workspace. Do not add persistence mappings or query logic here.
 */
public class Aircraft {

    private final String uai;
    private final String sourceNode;
    private final String aircraftType;
    private final String status;

    public Aircraft(String uai, String sourceNode, String aircraftType, String status) {
        this.uai = uai;
        this.sourceNode = sourceNode;
        this.aircraftType = aircraftType;
        this.status = status;
    }

    public String getUai() {
        return uai;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public String getStatus() {
        return status;
    }
}
