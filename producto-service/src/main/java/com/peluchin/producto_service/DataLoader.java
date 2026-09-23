package com.peluchin.producto_service;

import com.peluchin.producto_service.model.Producto;
import com.peluchin.producto_service.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ProductoRepository productoRepository) {

        return args -> {

            // Solo cargar productos si la tabla está vacía
            if (productoRepository.count() == 0) {

                Producto pikachu = new Producto();
                pikachu.setNombre("Pikachu");
                pikachu.setPrecio(new BigDecimal("15990"));
                pikachu.setImagenUrl("https://ansaldo.cl/cdn/shop/files/1_206346b9-b27c-4f3a-b6fc-b3c826a257b4.png?v=1762805643&width=1200");
                pikachu.setStock(10);

                Producto sonic = new Producto();
                sonic.setNombre("Sonic");
                sonic.setPrecio(new BigDecimal("17990"));
                sonic.setImagenUrl("https://ansaldo.cl/products/peluche-basico-sonic-de-23-cms-sonic?variant=43475879002205&country=CL&currency=CLP&utm_medium=product_sync&utm_source=google&utm_content=sag_organic&utm_campaign=sag_organic&gad_source=1&gad_campaignid=24198143190&gbraid=0AAAAADKTmCMB7-jiYXadXUOz1o7omD1lT&gclid=Cj0KCQjw8c3VBhCsARIsAA_xJ911eoEAw59sFlNCJguelM2oFzCztnDIx4EDNhTT1jSXadoOES1jSoMaAvzaEALw_wcB");
                sonic.setStock(8);

                Producto mario = new Producto();
                mario.setNombre("Mario");
                mario.setPrecio(new BigDecimal("18990"));
                mario.setImagenUrl("https://i5.walmartimages.com/seo/Super-Mario-Bros-Doctor-Mario-10-Plush-Figure_83827083-453c-42c5-ba8a-8ca404e14caf.fe971da94ee23683f1689b022cdd49f7.jpeg");
                mario.setStock(7);

                Producto totoro = new Producto();
                totoro.setNombre("Totoro");
                totoro.setPrecio(new BigDecimal("19990"));
                totoro.setImagenUrl("https://cdnx.jumpseller.com/peluches-pichinco/image/60528833/resize/640/500?1740431538");
                totoro.setStock(5);

                Producto luffy = new Producto();
                luffy.setNombre("Luffy");
                luffy.setPrecio(new BigDecimal("21990"));
                luffy.setImagenUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR4eImjiwyS0T_oZuyP0qWyAeE0sknpufFdFn71ZkA2xBsTX0rWNlQS8Rc&s=10");
                luffy.setStock(6);

                List<Producto> productosIniciales = List.of(
                        pikachu,
                        sonic,
                        mario,
                        totoro,
                        luffy
                );

                productoRepository.saveAll(productosIniciales);

                System.out.println(
                        "Productos iniciales cargados en la base de datos exitosamente."
                );
            }
        };
    }

}
