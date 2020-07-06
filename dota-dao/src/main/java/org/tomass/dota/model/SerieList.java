package org.tomass.dota.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerieList implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Serie> series;

    public SerieList() {
        this(new ArrayList<>());
    }

    public SerieList(List<Serie> list) {
        this.series = list;
    }

    public List<Serie> getSeries() {
        return series;
    }

    public void setSeries(List<Serie> series) {
        this.series = series;
    }

}
