package com.cmnd97.moneyvate;

/**
 * Created by cristi-mnd on 15.02.18.
 */

class Tag {
    String id;
    String description;
    String loc_lat;
    String loc_long;


    public Tag(String id, String description, String loc_lat, String loc_long) {
        this.id = id;
        this.description = description;
        this.loc_lat = loc_lat;
        this.loc_long = loc_long;
    }
}
