package org.tomass.dota.gc.service.serie;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tomass.dota.dao.SerieDao;
import org.tomass.dota.model.Serie;

@Service
public class SerieService {

    @Autowired
    private SerieDao serieDao;

    public List<Serie> listAllSeries() {
        return serieDao.list(new Serie());
    }

    public void addSerie(Serie serie) {
        serieDao.insert(serie);
    }

    public Serie getSerie(Serie serie) {
        return serieDao.get(serie);
    }

    public void editSerie(Serie serie) {
        serieDao.update(serie);
    }

    public void deleteSerie(Serie serie) {
        serieDao.delete(serie);
    }

}
