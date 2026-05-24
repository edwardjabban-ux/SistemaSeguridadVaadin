package com.security.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class SolicitudServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @Column(length = 1000)
    private String servicios;

    @Column(length = 1000)
    private String personalizacion;

    private LocalDate fechaServicio;
    private LocalTime horaServicio;
    private double costoTotal;
    private String metodoPago;
    private String estadoPago;
    private String comprobantePago;
    private String escoltaAsignado;
    private String vehiculoAsignado;
    private String monitoreo;
    private boolean emergenciaActiva;
    private Integer calificacion;
    private String comentario;
    private String estado;
    private LocalDateTime fechaCreacion;

    public SolicitudServicio() {}

    public SolicitudServicio(Usuario usuario, String servicios, double costoTotal, String metodoPago) {
        this.usuario = usuario;
        this.servicios = servicios;
        this.costoTotal = costoTotal;
        this.metodoPago = metodoPago;
        this.estadoPago = "Pendiente";
        this.estado = "Solicitud creada";
        this.fechaCreacion = LocalDateTime.now();
    }

    public SolicitudServicio(Usuario usuario, String servicios, String personalizacion, LocalDate fechaServicio, LocalTime horaServicio, double costoTotal, String metodoPago) {
        this.usuario = usuario;
        this.servicios = servicios;
        this.personalizacion = personalizacion;
        this.fechaServicio = fechaServicio;
        this.horaServicio = horaServicio;
        this.costoTotal = costoTotal;
        this.metodoPago = metodoPago;
        this.estadoPago = "Pendiente";
        this.estado = "Solicitud guardada";
        this.monitoreo = "Pendiente de asignacion";
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getServicios() { return servicios; }
    public void setServicios(String servicios) { this.servicios = servicios; }
    public String getPersonalizacion() { return personalizacion; }
    public void setPersonalizacion(String personalizacion) { this.personalizacion = personalizacion; }
    public LocalDate getFechaServicio() { return fechaServicio; }
    public void setFechaServicio(LocalDate fechaServicio) { this.fechaServicio = fechaServicio; }
    public LocalTime getHoraServicio() { return horaServicio; }
    public void setHoraServicio(LocalTime horaServicio) { this.horaServicio = horaServicio; }
    public double getCostoTotal() { return costoTotal; }
    public void setCostoTotal(double costoTotal) { this.costoTotal = costoTotal; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
    public String getComprobantePago() { return comprobantePago; }
    public void setComprobantePago(String comprobantePago) { this.comprobantePago = comprobantePago; }
    public String getEscoltaAsignado() { return escoltaAsignado; }
    public void setEscoltaAsignado(String escoltaAsignado) { this.escoltaAsignado = escoltaAsignado; }
    public String getVehiculoAsignado() { return vehiculoAsignado; }
    public void setVehiculoAsignado(String vehiculoAsignado) { this.vehiculoAsignado = vehiculoAsignado; }
    public String getMonitoreo() { return monitoreo; }
    public void setMonitoreo(String monitoreo) { this.monitoreo = monitoreo; }
    public boolean isEmergenciaActiva() { return emergenciaActiva; }
    public void setEmergenciaActiva(boolean emergenciaActiva) { this.emergenciaActiva = emergenciaActiva; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
