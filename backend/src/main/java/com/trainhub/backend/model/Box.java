package com.trainhub.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad que representa un box (gimnasio) asociado a una ciudad.
 * Mapeada a la tabla "boxes" de la base de datos.
 */
@Entity
@Table(name = "boxes")
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 150)
    @NotNull
    @Size(max = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    @NotNull
    private City city;

    @Column(name = "address", length = 255)
    private String address;

    public Box() {}

    public Box(String name, City city) {
        this.name = name;
        this.city = city;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
