package com.trainhub.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BulkRegisterRequest {

    @NotEmpty(message = "La lista de usuarios no puede estar vacía")
    @Valid
    private List<RegisterRequest> users;

    public BulkRegisterRequest() {}

    public List<RegisterRequest> getUsers() {
        return users;
    }

    public void setUsers(List<RegisterRequest> users) {
        this.users = users;
    }
}
