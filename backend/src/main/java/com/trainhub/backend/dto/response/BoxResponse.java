package com.trainhub.backend.dto.response;

/**
 * DTO de respuesta con los datos básicos de un box.
 */
public class BoxResponse {

    private Integer id;
    private String name;
    private String cityName;

    public BoxResponse() {}

    public BoxResponse(Integer id, String name, String cityName) {
        this.id = id;
        this.name = name;
        this.cityName = cityName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
