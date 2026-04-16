package com.carebridge.dtos;

import java.util.List;
import java.util.Objects;

public class LinkResidentsRequest {
    private List<Long> residentIds;

    public LinkResidentsRequest() {
    }

    public LinkResidentsRequest(List<Long> residentIds) {
        this.residentIds = residentIds;
    }

    public List<Long> getResidentIds() {
        return residentIds;
    }

    public void setResidentIds(List<Long> residentIds) {
        this.residentIds = residentIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkResidentsRequest that = (LinkResidentsRequest) o;
        return Objects.equals(residentIds, that.residentIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(residentIds);
    }

    @Override
    public String toString() {
        return "LinkResidentsRequest{" +
                "residentIds=" + residentIds +
                '}';
    }
}
