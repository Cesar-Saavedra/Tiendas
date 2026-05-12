package cl.duoc.ms_tiendas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.ms_tiendas.model.EstadoTienda;
import cl.duoc.ms_tiendas.model.MetricaTienda;
import cl.duoc.ms_tiendas.model.Tienda;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initData (TiendaRepository tiendaRepo, MetricaTiendaRepository metricaRepo) {
        return args -> {
            // Crear tiendas de ejemplo
            Tienda tienda1 = new Tienda(null, "Tienda A", "Descripción de la Tienda A", "https://example.com/tiendaA.jpg", null, null, EstadoTienda.ACTIVA, null);
            Tienda tienda2 = new Tienda(null, "Tienda B", "Descripción de la Tienda B", "https://example.com/tiendaB.jpg", null, null, EstadoTienda.PENDIENTE, null);
            tiendaRepo.save(tienda1);
            tiendaRepo.save(tienda2);

            // Crear métricas para cada tienda
            MetricaTienda metrica1 = new MetricaTienda(null, tienda1, 100, 20, 5);
            MetricaTienda metrica2 = new MetricaTienda(null, tienda2, 50, 10, 2);
            metricaRepo.save(metrica1);
            metricaRepo.save(metrica2);
        };
    }





}
