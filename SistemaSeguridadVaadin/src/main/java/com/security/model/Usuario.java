
package com.security.model;

import jakarta.persistence.*;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String correo;
    private String telefono;
    private String documento;
    private String clave;

    public Usuario() {}

    public Usuario(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    public Usuario(String nombre, String correo, String telefono, String documento) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.documento = documento;
    }

    public Usuario(String nombre, String correo, String telefono, String documento, String clave) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.documento = documento;
        this.clave = clave;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    @Override
    public String toString() {
        return nombre + " - " + correo;
    }
}
