package com.sulakov.db_service;

public class CountryStats {
    private String name;
    private int rank;
    private int cases;
    private float percentOfAll;

    public CountryStats(String name, int rank, int cases, float percentOfAll) {
        this.name = name;
        this.rank = rank;
        this.cases = cases;
        this.percentOfAll = percentOfAll;
    }

    public String getName() {
        return name;
    }

    public int getRank() {
        return rank;
    }

    public int getCases() {
        return cases;
    }

    public float getPercentOfAll() {
        return percentOfAll;
    }
}
