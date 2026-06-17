package cl.duoc.ms_tiendas.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.ms_tiendas.repository.MetricaTiendaRepositorio;
import cl.duoc.ms_tiendas.repository.TiendaRepositorio;

@ExtendWith(MockitoExtension.class)
public class TiendaServiceTest {

    @Mock
    private TiendaRepositorio tiendaRepositorio;

    @InjectMocks  //injectamos el mock falso, para que no utilizar la clase real, sino el mock
    private TiendaServicio tiendaServicio;
    
    @Mock
    private MetricaTiendaRepositorio metricaTiendaRepositorio;
    



}
