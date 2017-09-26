package com.sundayschool.persistence;

public class RegionToChurch {
    private int id;
    private String region;
    private String church;
    private boolean venue;

    public RegionToChurch() {
    }

    public RegionToChurch(String region, String church, boolean venue) {
        this.region = region;
        this.church = church;
        this.venue = venue;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getChurch() {
        return church;
    }

    public void setChurch(String church) {
        this.church = church;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getVenue() {
        return venue;
    }

    public void setVenue(boolean venue) {
        this.venue = venue;
    }
}
