package com.security.view;

import com.security.model.Servicio;
import com.security.model.SolicitudServicio;
import com.security.model.Usuario;
import com.security.service.ServicioRepository;
import com.security.service.SolicitudServicioRepository;
import com.security.service.UsuarioRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Route("")
public class MainView extends VerticalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainView.class);
    private static final String ADMIN_CORREO = "admin@seguridad.com";
    private static final String ADMIN_CLAVE = "admin123";

    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final SolicitudServicioRepository solicitudRepository;
    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final Grid<SolicitudServicio> historialUsuarioGrid = new Grid<>(SolicitudServicio.class, false);
    private final Grid<SolicitudServicio> solicitudesAdminGrid = new Grid<>(SolicitudServicio.class, false);
    private final Grid<Usuario> usuariosGrid = new Grid<>(Usuario.class, false);
    private final Grid<Servicio> serviciosGrid = new Grid<>(Servicio.class, false);

    private final ComboBox<Usuario> usuarioSolicitud = new ComboBox<>("Usuario");
    private final MultiSelectComboBox<Servicio> serviciosSolicitud = new MultiSelectComboBox<>("Servicios");
    private final ComboBox<SolicitudServicio> solicitudUsuario = new ComboBox<>("Mi solicitud");
    private final ComboBox<SolicitudServicio> solicitudAdmin = new ComboBox<>("Solicitud");

    private final Span usuariosMetric = new Span();
    private final Span solicitudesMetric = new Span();
    private final Span emergenciasMetric = new Span();
    private final Span ingresosMetric = new Span();

    private Usuario usuarioActual;

    public MainView(
            UsuarioRepository usuarioRepository,
            ServicioRepository servicioRepository,
            SolicitudServicioRepository solicitudRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.servicioRepository = servicioRepository;
        this.solicitudRepository = solicitudRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f4f7fb").set("color", "#172033");

        configurarGrids();
        mostrarLogin();
    }

    private void mostrarLogin() {
        removeAll();
        add(crearLoginHeader(), crearLogin());
    }

    private Component crearLoginHeader() {
        H1 titulo = new H1("Seguridad Integral");
        titulo.getStyle().set("margin", "0").set("font-size", "34px");

        Paragraph subtitulo = new Paragraph("Inicia sesion como cliente o administrador para gestionar servicios de proteccion.");
        subtitulo.getStyle().set("margin", "8px 0 0 0").set("color", "#596579");

        VerticalLayout header = new VerticalLayout(titulo, subtitulo);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        header.setSpacing(false);
        header.getStyle().set("padding", "44px 20px 18px 20px");
        return header;
    }

    private Component crearLogin() {
        EmailField clienteCorreo = new EmailField("Correo del cliente");
        PasswordField clienteClave = new PasswordField("Clave");
        Button entrarCliente = new Button("Iniciar sesion cliente");
        entrarCliente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        entrarCliente.addClickListener(e -> {
            if (clienteCorreo.isEmpty() || clienteClave.isEmpty()) {
                Notification.show("Ingrese correo y clave");
                return;
            }

            try {
                usuarioRepository.findByCorreoAndClave(clienteCorreo.getValue(), clienteClave.getValue())
                        .ifPresentOrElse(usuario -> {
                            usuarioActual = usuario;
                            LOGGER.info("Inicio de sesion de cliente: {}", usuario.getCorreo());
                            mostrarPortalUsuario();
                        }, () -> {
                            LOGGER.warn("Intento fallido de inicio de sesion para cliente: {}", clienteCorreo.getValue());
                            Notification.show("Credenciales de cliente invalidas");
                        });
            } catch (RuntimeException ex) {
                manejarError("No fue posible iniciar sesion", ex);
            }
        });

        TextField nombreRegistro = new TextField("Nombre completo");
        EmailField correoRegistro = new EmailField("Correo");
        TextField telefonoRegistro = new TextField("Telefono");
        TextField documentoRegistro = new TextField("Documento");
        PasswordField claveRegistro = new PasswordField("Clave");
        Button crearCuenta = new Button("Crear cuenta cliente");
        crearCuenta.addClickListener(e -> {
            if (nombreRegistro.isEmpty() || correoRegistro.isEmpty() || claveRegistro.isEmpty()) {
                Notification.show("Nombre, correo y clave son obligatorios");
                return;
            }
            try {
                if (usuarioRepository.findByCorreo(correoRegistro.getValue()).isPresent()) {
                    Notification.show("Ya existe un cliente con ese correo");
                    return;
                }

                usuarioActual = usuarioRepository.save(new Usuario(
                        nombreRegistro.getValue(),
                        correoRegistro.getValue(),
                        telefonoRegistro.getValue(),
                        documentoRegistro.getValue(),
                        claveRegistro.getValue()
                ));
                LOGGER.info("Cliente creado: {}", usuarioActual.getCorreo());
                mostrarPortalUsuario();
                Notification.show("Cuenta creada e inicio de sesion realizado");
            } catch (RuntimeException ex) {
                manejarError("No fue posible crear la cuenta", ex);
            }
        });

        VerticalLayout cliente = tarjetaLogin(
                "Cliente",
                "Solicita servicios, realiza pagos, activa emergencias y revisa tu historial.",
                clienteCorreo,
                clienteClave,
                entrarCliente,
                new H3("Crear cuenta"),
                nombreRegistro,
                correoRegistro,
                telefonoRegistro,
                documentoRegistro,
                claveRegistro,
                crearCuenta
        );

        EmailField adminCorreo = new EmailField("Correo administrador");
        PasswordField adminClave = new PasswordField("Clave administrador");
        Button entrarAdmin = new Button("Iniciar sesion administrador");
        entrarAdmin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        entrarAdmin.addClickListener(e -> {
            if (ADMIN_CORREO.equals(adminCorreo.getValue()) && ADMIN_CLAVE.equals(adminClave.getValue())) {
                usuarioActual = null;
                LOGGER.info("Inicio de sesion de administrador");
                mostrarPanelAdmin();
            } else {
                LOGGER.warn("Intento fallido de inicio de sesion de administrador: {}", adminCorreo.getValue());
                Notification.show("Credenciales de administrador invalidas");
            }
        });

        VerticalLayout admin = tarjetaLogin(
                "Administrador",
                "Gestiona catalogo, solicitudes, asignaciones, emergencias y clientes.",
                adminCorreo,
                adminClave,
                entrarAdmin
        );

        HorizontalLayout login = new HorizontalLayout(cliente, admin);
        login.setWidthFull();
        login.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        login.setAlignItems(FlexComponent.Alignment.START);
        login.getStyle().set("gap", "22px").set("padding", "10px 28px 34px 28px").set("flex-wrap", "wrap");
        return login;
    }

    private VerticalLayout tarjetaLogin(String titulo, String descripcion, Component... componentes) {
        H2 encabezado = new H2(titulo);
        encabezado.getStyle().set("margin", "0").set("font-size", "24px");
        Paragraph texto = new Paragraph(descripcion);
        texto.getStyle().set("margin", "0 0 8px 0").set("color", "#596579");

        VerticalLayout tarjeta = new VerticalLayout(encabezado, texto);
        tarjeta.add(componentes);
        tarjeta.setWidth("460px");
        tarjeta.setPadding(true);
        tarjeta.setSpacing(true);
        tarjeta.getStyle()
                .set("background", "#ffffff")
                .set("border", "1px solid #dfe7f3")
                .set("border-radius", "8px")
                .set("box-shadow", "0 8px 24px rgba(15, 23, 42, 0.10)");
        return tarjeta;
    }

    private void mostrarPortalUsuario() {
        removeAll();
        add(crearHeader("Portal del cliente", usuarioActual.getNombre()), crearPortalUsuario());
        refrescarDatos();
    }

    private void mostrarPanelAdmin() {
        removeAll();
        add(crearHeader("Panel administrador", "admin@seguridad.com"), crearPanelAdmin());
        refrescarDatos();
    }

    private Component crearHeader(String tituloTexto, String usuarioTexto) {
        H1 titulo = new H1(tituloTexto);
        titulo.getStyle().set("margin", "0").set("font-size", "32px");

        Paragraph subtitulo = new Paragraph("Sesion activa: " + usuarioTexto);
        subtitulo.getStyle().set("margin", "6px 0 0 0").set("color", "#596579");

        VerticalLayout texto = new VerticalLayout(titulo, subtitulo);
        texto.setPadding(false);
        texto.setSpacing(false);

        Button salir = new Button("Cerrar sesion", e -> {
            usuarioActual = null;
            mostrarLogin();
        });
        salir.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(texto, salir);
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
                .set("background", "#ffffff")
                .set("border-bottom", "1px solid #dfe7f3")
                .set("padding", "24px 28px");
        return header;
    }

    private VerticalLayout crearPortalUsuario() {
        Div contenido = new Div();
        contenido.setWidthFull();
        contenido.getStyle().set("padding", "0 28px 28px 28px");

        ComboBox<String> metodoPago = new ComboBox<>("Metodo de pago");
        TextField total = new TextField("Total estimado");
        TextArea personalizacion = new TextArea("Personalizacion del servicio");
        DatePicker fechaServicio = new DatePicker("Fecha del servicio");
        TimePicker horaServicio = new TimePicker("Hora del servicio");
        metodoPago.setItems("Tarjeta", "Transferencia", "Efectivo", "Pago empresarial");
        total.setReadOnly(true);
        total.setValue(moneda.format(0));
        fechaServicio.setMin(LocalDate.now());
        personalizacion.setPlaceholder("Ejemplo: ruta, acompañantes, nivel de riesgo, observaciones");

        usuarioSolicitud.setItemLabelGenerator(Usuario::toString);
        usuarioSolicitud.setReadOnly(true);
        serviciosSolicitud.setItemLabelGenerator(servicio -> servicio.getTipo() + " - " + moneda.format(servicio.getPrecio()));
        serviciosSolicitud.addValueChangeListener(e -> total.setValue(moneda.format(calcularTotal(e.getValue()))));

        Button solicitar = new Button("Solicitar servicio");
        solicitar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        solicitar.addClickListener(e -> {
            Set<Servicio> seleccion = serviciosSolicitud.getValue();
            if (usuarioActual == null || seleccion.isEmpty() || fechaServicio.isEmpty() || horaServicio.isEmpty() || metodoPago.isEmpty()) {
                Notification.show("Seleccione servicios, fecha, hora y metodo de pago");
                return;
            }

            String nombresServicios = seleccion.stream().map(Servicio::getTipo).collect(Collectors.joining(", "));
            SolicitudServicio solicitud = new SolicitudServicio(
                    usuarioActual,
                    nombresServicios,
                    personalizacion.getValue(),
                    fechaServicio.getValue(),
                    horaServicio.getValue(),
                    calcularTotal(seleccion),
                    metodoPago.getValue()
            );
            ejecutarOperacion("Solicitud guardada. Revise el detalle y espere asignacion.", "No fue posible guardar la solicitud", () -> {
                solicitudRepository.save(solicitud);
                serviciosSolicitud.clear();
                personalizacion.clear();
                fechaServicio.clear();
                horaServicio.clear();
                metodoPago.clear();
                total.setValue(moneda.format(0));
            });
        });

        HorizontalLayout seleccion = fila(usuarioSolicitud, serviciosSolicitud, personalizacion);
        HorizontalLayout agendaPago = fila(fechaServicio, horaServicio, metodoPago, total, solicitar);

        solicitudUsuario.setItemLabelGenerator(item -> "#" + item.getId() + " - " + item.getServicios() + " - " + item.getEstado());
        IntegerField calificacion = new IntegerField("Calificacion");
        calificacion.setMin(1);
        calificacion.setMax(5);
        TextArea comentario = new TextArea("Comentario");

        Button pagar = new Button("Pagar");
        pagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pagar.addClickListener(e -> pagarSolicitudUsuario());

        Button emergencia = new Button("Emergencia");
        emergencia.addThemeVariants(ButtonVariant.LUMO_ERROR);
        emergencia.addClickListener(e -> {
            SolicitudServicio item = solicitudUsuario.getValue();
            if (item == null) {
                Notification.show("Seleccione una solicitud");
                return;
            }
            item.setEmergenciaActiva(true);
            item.setEstado("Emergencia activa");
            ejecutarOperacion("Emergencia activada", "No fue posible activar la emergencia", () -> solicitudRepository.save(item));
        });

        Button enviarCalificacion = new Button("Enviar calificacion");
        enviarCalificacion.addClickListener(e -> {
            SolicitudServicio item = solicitudUsuario.getValue();
            if (item == null || calificacion.isEmpty()) {
                Notification.show("Seleccione solicitud y calificacion");
                return;
            }
            item.setCalificacion(calificacion.getValue());
            item.setComentario(comentario.getValue());
            item.setEstado("Servicio calificado");
            ejecutarOperacion("Gracias por calificar el servicio", "No fue posible registrar la calificacion", () -> {
                solicitudRepository.save(item);
                calificacion.clear();
                comentario.clear();
            });
        });

        HorizontalLayout seguimiento = fila(solicitudUsuario, pagar, emergencia, calificacion, comentario, enviarCalificacion);

        contenido.add(
                crearMetricas(),
                panel("1. Seleccionar, personalizar y agendar servicio", seleccion, agendaPago),
                panel("Pago, emergencia y calificacion", seguimiento),
                panel("Mi historial", historialUsuarioGrid)
        );

        VerticalLayout layout = new VerticalLayout(contenido);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
        return layout;
    }

    private VerticalLayout crearPanelAdmin() {
        Div contenido = new Div();
        contenido.setWidthFull();
        contenido.getStyle().set("padding", "0 28px 28px 28px");

        TextField tipo = new TextField("Nombre del servicio");
        NumberField precio = new NumberField("Precio");
        precio.setMin(0);
        precio.setStep(50000);
        Button agregarServicio = new Button("Agregar servicio");
        agregarServicio.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        agregarServicio.addClickListener(e -> {
            if (tipo.isEmpty() || precio.isEmpty()) {
                Notification.show("Servicio y precio son obligatorios");
                return;
            }

            ejecutarOperacion("Servicio publicado", "No fue posible publicar el servicio", () -> {
                servicioRepository.save(new Servicio(tipo.getValue(), precio.getValue()));
                tipo.clear();
                precio.clear();
            });
        });

        TextField escolta = new TextField("Escolta asignado");
        TextField vehiculo = new TextField("Vehiculo asignado");
        TextField monitoreo = new TextField("GPS / estado del servicio");
        solicitudAdmin.setItemLabelGenerator(item -> "#" + item.getId() + " - " + item.getUsuario().getNombre() + " - " + item.getEstado());

        Button asignar = new Button("Asignar recursos");
        asignar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        asignar.addClickListener(e -> {
            SolicitudServicio item = solicitudAdmin.getValue();
            if (item == null || escolta.isEmpty() || vehiculo.isEmpty()) {
                Notification.show("Seleccione solicitud, escolta y vehiculo");
                return;
            }

            item.setEscoltaAsignado(escolta.getValue());
            item.setVehiculoAsignado(vehiculo.getValue());
            item.setMonitoreo(monitoreo.isEmpty() ? "Personal asignado, pendiente de iniciar" : monitoreo.getValue());
            item.setEstado("Escolta y vehiculo asignados");
            ejecutarOperacion("Recursos asignados al servicio", "No fue posible asignar recursos", () -> solicitudRepository.save(item));
        });

        Button iniciarServicio = new Button("Iniciar servicio");
        iniciarServicio.addClickListener(e -> {
            SolicitudServicio item = solicitudAdmin.getValue();
            if (item == null) {
                Notification.show("Seleccione una solicitud");
                return;
            }
            item.setEstado("Servicio iniciado");
            item.setMonitoreo(monitoreo.isEmpty() ? "En ruta - GPS activo" : monitoreo.getValue());
            ejecutarOperacion("Servicio iniciado y monitoreo activo", "No fue posible iniciar el servicio", () -> solicitudRepository.save(item));
        });

        Button finalizarServicio = new Button("Finalizar servicio");
        finalizarServicio.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        finalizarServicio.addClickListener(e -> {
            SolicitudServicio item = solicitudAdmin.getValue();
            if (item == null) {
                Notification.show("Seleccione una solicitud");
                return;
            }
            item.setEstado("Servicio finalizado");
            item.setMonitoreo("Servicio finalizado, solicitar calificacion");
            ejecutarOperacion("Servicio finalizado. El cliente puede calificar.", "No fue posible finalizar el servicio", () -> solicitudRepository.save(item));
        });

        Button cerrarEmergencia = new Button("Cerrar emergencia");
        cerrarEmergencia.addClickListener(e -> {
            SolicitudServicio item = solicitudAdmin.getValue();
            if (item == null) {
                Notification.show("Seleccione una solicitud");
                return;
            }
            item.setEmergenciaActiva(false);
            item.setEstado("Servicio en seguimiento");
            ejecutarOperacion("Emergencia cerrada", "No fue posible cerrar la emergencia", () -> solicitudRepository.save(item));
        });

        contenido.add(
                crearMetricas(),
                panel("Administrar catalogo", fila(tipo, precio, agregarServicio), serviciosGrid),
                panel("Operacion de servicios", fila(solicitudAdmin, escolta, vehiculo, monitoreo, asignar, iniciarServicio, finalizarServicio, cerrarEmergencia), solicitudesAdminGrid),
                panel("Clientes registrados", usuariosGrid)
        );

        VerticalLayout layout = new VerticalLayout(contenido);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
        return layout;
    }

    private HorizontalLayout crearMetricas() {
        HorizontalLayout metricas = new HorizontalLayout(
                metrica("Clientes", usuariosMetric),
                metrica("Solicitudes", solicitudesMetric),
                metrica("Emergencias", emergenciasMetric),
                metrica("Ingresos", ingresosMetric)
        );
        metricas.setWidthFull();
        metricas.setSpacing(true);
        metricas.getStyle().set("margin", "22px 0 8px 0").set("flex-wrap", "wrap");
        return metricas;
    }

    private void configurarGrids() {
        configurarSolicitudesGrid(historialUsuarioGrid);
        configurarSolicitudesGrid(solicitudesAdminGrid);

        usuariosGrid.addColumn(Usuario::getId).setHeader("ID").setAutoWidth(true);
        usuariosGrid.addColumn(Usuario::getNombre).setHeader("Nombre").setAutoWidth(true);
        usuariosGrid.addColumn(Usuario::getCorreo).setHeader("Correo").setAutoWidth(true);
        usuariosGrid.addColumn(Usuario::getTelefono).setHeader("Telefono").setAutoWidth(true);
        usuariosGrid.addColumn(Usuario::getDocumento).setHeader("Documento").setAutoWidth(true);
        usuariosGrid.addColumn(new ComponentRenderer<>(usuario -> {
            Button editar = new Button("Editar", event -> editarCliente(usuario));
            editar.addThemeVariants(ButtonVariant.LUMO_SMALL);
            Button eliminar = new Button("Eliminar", event -> eliminarCliente(usuario));
            eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return filaAcciones(editar, eliminar);
        })).setHeader("Acciones").setAutoWidth(true);

        serviciosGrid.addColumn(Servicio::getId).setHeader("ID").setAutoWidth(true);
        serviciosGrid.addColumn(Servicio::getTipo).setHeader("Servicio").setAutoWidth(true);
        serviciosGrid.addColumn(servicio -> moneda.format(servicio.getPrecio())).setHeader("Precio").setAutoWidth(true);
        serviciosGrid.addColumn(new ComponentRenderer<>(servicio -> {
            Button editar = new Button("Editar", event -> editarServicio(servicio));
            editar.addThemeVariants(ButtonVariant.LUMO_SMALL);
            Button eliminar = new Button("Eliminar", event -> eliminarServicio(servicio));
            eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return filaAcciones(editar, eliminar);
        })).setHeader("Acciones").setAutoWidth(true);
    }

    private void configurarSolicitudesGrid(Grid<SolicitudServicio> grid) {
        grid.addColumn(SolicitudServicio::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(item -> item.getUsuario() == null ? "Sin usuario" : item.getUsuario().getNombre()).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getServicios).setHeader("Servicios").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getPersonalizacion).setHeader("Personalizacion").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getFechaServicio).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getHoraServicio).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(item -> moneda.format(item.getCostoTotal())).setHeader("Total").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getMetodoPago).setHeader("Pago").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getEstadoPago).setHeader("Estado pago").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getComprobantePago).setHeader("Comprobante").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getEscoltaAsignado).setHeader("Escolta").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getVehiculoAsignado).setHeader("Vehiculo").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getMonitoreo).setHeader("Monitoreo").setAutoWidth(true);
        grid.addColumn(item -> item.isEmergenciaActiva() ? "Activa" : "No").setHeader("Emergencia").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getCalificacion).setHeader("Calificacion").setAutoWidth(true);
        grid.addColumn(SolicitudServicio::getEstado).setHeader("Estado").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(solicitud -> {
            Button editar = new Button("Editar", event -> editarSolicitud(solicitud));
            editar.addThemeVariants(ButtonVariant.LUMO_SMALL);
            Button eliminar = new Button("Eliminar", event -> eliminarSolicitud(solicitud));
            eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return filaAcciones(editar, eliminar);
        })).setHeader("Acciones").setAutoWidth(true);
    }

    private void refrescarDatos() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<Servicio> servicios = servicioRepository.findAll();
            List<SolicitudServicio> solicitudes = solicitudRepository.findAll();
            List<SolicitudServicio> solicitudesUsuario = solicitudes;

            if (usuarioActual != null && usuarioActual.getId() != null) {
                solicitudesUsuario = solicitudes.stream()
                        .filter(item -> item.getUsuario() != null && usuarioActual.getId().equals(item.getUsuario().getId()))
                        .collect(Collectors.toList());
                usuarioSolicitud.setItems(List.of(usuarioActual));
                usuarioSolicitud.setValue(usuarioActual);
            } else {
                usuarioSolicitud.setItems(usuarios);
            }

            serviciosSolicitud.setItems(servicios);
            solicitudUsuario.setItems(solicitudesUsuario);
            solicitudAdmin.setItems(solicitudes);
            historialUsuarioGrid.setItems(solicitudesUsuario);
            solicitudesAdminGrid.setItems(solicitudes);
            usuariosGrid.setItems(usuarios);
            serviciosGrid.setItems(servicios);

            usuariosMetric.setText(String.valueOf(usuarios.size()));
            solicitudesMetric.setText(String.valueOf(usuarioActual == null ? solicitudes.size() : solicitudesUsuario.size()));
            emergenciasMetric.setText(String.valueOf((usuarioActual == null ? solicitudes : solicitudesUsuario).stream().filter(SolicitudServicio::isEmergenciaActiva).count()));
            ingresosMetric.setText(moneda.format((usuarioActual == null ? solicitudes : solicitudesUsuario).stream()
                    .filter(item -> "Pagado".equals(item.getEstadoPago()))
                    .mapToDouble(SolicitudServicio::getCostoTotal)
                    .sum()));
            LOGGER.debug("Datos refrescados: {} usuarios, {} servicios, {} solicitudes", usuarios.size(), servicios.size(), solicitudes.size());
        } catch (RuntimeException ex) {
            manejarError("No fue posible cargar los datos", ex);
        }
    }

    private void pagarSolicitudUsuario() {
        SolicitudServicio item = solicitudUsuario.getValue();
        if (item == null) {
            Notification.show("Seleccione una solicitud");
            return;
        }
        item.setEstadoPago("Pagado");
        item.setEstado("Pago aprobado");
        item.setComprobantePago("CP-" + item.getId() + "-" + System.currentTimeMillis());
        ejecutarOperacion("Pago aprobado y comprobante generado", "No fue posible registrar el pago", () -> solicitudRepository.save(item));
    }

    private void eliminarCliente(Usuario usuario) {
        ejecutarOperacion("Cliente eliminado", "No fue posible eliminar el cliente", () -> {
            List<SolicitudServicio> solicitudesCliente = solicitudRepository.findAll().stream()
                    .filter(item -> item.getUsuario() != null && usuario.getId().equals(item.getUsuario().getId()))
                    .collect(Collectors.toList());

            solicitudRepository.deleteAll(solicitudesCliente);
            usuarioRepository.delete(usuario);
        });
    }

    private void editarCliente(Usuario usuario) {
        TextField nombre = new TextField("Nombre completo");
        EmailField correo = new EmailField("Correo");
        TextField telefono = new TextField("Telefono");
        TextField documento = new TextField("Documento");
        PasswordField clave = new PasswordField("Clave");

        nombre.setValue(valorSeguro(usuario.getNombre()));
        correo.setValue(valorSeguro(usuario.getCorreo()));
        telefono.setValue(valorSeguro(usuario.getTelefono()));
        documento.setValue(valorSeguro(usuario.getDocumento()));
        clave.setValue(valorSeguro(usuario.getClave()));

        Dialog dialog = crearDialogo("Editar cliente");
        Button guardar = new Button("Guardar", event -> {
            if (nombre.isEmpty() || correo.isEmpty() || clave.isEmpty()) {
                Notification.show("Nombre, correo y clave son obligatorios");
                return;
            }

            try {
                if (usuarioRepository.findByCorreo(correo.getValue())
                        .filter(existente -> !existente.getId().equals(usuario.getId()))
                        .isPresent()) {
                    Notification.show("Ya existe otro cliente con ese correo");
                    return;
                }
            } catch (RuntimeException ex) {
                manejarError("No fue posible validar el correo", ex);
                return;
            }

            usuario.setNombre(nombre.getValue());
            usuario.setCorreo(correo.getValue());
            usuario.setTelefono(telefono.getValue());
            usuario.setDocumento(documento.getValue());
            usuario.setClave(clave.getValue());
            if (ejecutarOperacion("Cliente actualizado", "No fue posible actualizar el cliente", () -> usuarioRepository.save(usuario))) {
                dialog.close();
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(nombre, correo, telefono, documento, clave, filaAcciones(guardar, new Button("Cancelar", event -> dialog.close()))));
        dialog.open();
    }

    private void editarServicio(Servicio servicio) {
        TextField tipo = new TextField("Nombre del servicio");
        NumberField precio = new NumberField("Precio");
        tipo.setValue(valorSeguro(servicio.getTipo()));
        precio.setValue(servicio.getPrecio());
        precio.setMin(0);

        Dialog dialog = crearDialogo("Editar servicio");
        Button guardar = new Button("Guardar", event -> {
            if (tipo.isEmpty() || precio.isEmpty()) {
                Notification.show("Servicio y precio son obligatorios");
                return;
            }
            servicio.setTipo(tipo.getValue());
            servicio.setPrecio(precio.getValue());
            if (ejecutarOperacion("Servicio actualizado", "No fue posible actualizar el servicio", () -> servicioRepository.save(servicio))) {
                dialog.close();
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(tipo, precio, filaAcciones(guardar, new Button("Cancelar", event -> dialog.close()))));
        dialog.open();
    }

    private void eliminarServicio(Servicio servicio) {
        ejecutarOperacion("Servicio eliminado", "No fue posible eliminar el servicio", () -> servicioRepository.delete(servicio));
    }

    private void editarSolicitud(SolicitudServicio solicitud) {
        TextField servicios = new TextField("Servicios");
        TextArea personalizacion = new TextArea("Personalizacion");
        DatePicker fecha = new DatePicker("Fecha");
        TimePicker hora = new TimePicker("Hora");
        NumberField costo = new NumberField("Costo total");
        ComboBox<String> metodoPago = new ComboBox<>("Metodo de pago");
        ComboBox<String> estadoPago = new ComboBox<>("Estado de pago");
        TextField comprobante = new TextField("Comprobante");
        TextField escolta = new TextField("Escolta");
        TextField vehiculo = new TextField("Vehiculo");
        TextField monitoreo = new TextField("Monitoreo");
        ComboBox<String> emergencia = new ComboBox<>("Emergencia");
        IntegerField calificacion = new IntegerField("Calificacion");
        TextArea comentario = new TextArea("Comentario");
        TextField estado = new TextField("Estado");

        metodoPago.setItems("Tarjeta", "Transferencia", "Efectivo", "Pago empresarial");
        estadoPago.setItems("Pendiente", "Pagado", "Rechazado");
        emergencia.setItems("No", "Activa");
        calificacion.setMin(1);
        calificacion.setMax(5);

        servicios.setValue(valorSeguro(solicitud.getServicios()));
        personalizacion.setValue(valorSeguro(solicitud.getPersonalizacion()));
        fecha.setValue(solicitud.getFechaServicio());
        hora.setValue(solicitud.getHoraServicio());
        costo.setValue(solicitud.getCostoTotal());
        metodoPago.setValue(solicitud.getMetodoPago());
        estadoPago.setValue(solicitud.getEstadoPago());
        comprobante.setValue(valorSeguro(solicitud.getComprobantePago()));
        escolta.setValue(valorSeguro(solicitud.getEscoltaAsignado()));
        vehiculo.setValue(valorSeguro(solicitud.getVehiculoAsignado()));
        monitoreo.setValue(valorSeguro(solicitud.getMonitoreo()));
        emergencia.setValue(solicitud.isEmergenciaActiva() ? "Activa" : "No");
        calificacion.setValue(solicitud.getCalificacion());
        comentario.setValue(valorSeguro(solicitud.getComentario()));
        estado.setValue(valorSeguro(solicitud.getEstado()));

        Dialog dialog = crearDialogo("Editar solicitud");
        Button guardar = new Button("Guardar", event -> {
            if (servicios.isEmpty() || fecha.isEmpty() || hora.isEmpty() || costo.isEmpty() || metodoPago.isEmpty() || estadoPago.isEmpty()) {
                Notification.show("Servicios, agenda, costo y pago son obligatorios");
                return;
            }

            solicitud.setServicios(servicios.getValue());
            solicitud.setPersonalizacion(personalizacion.getValue());
            solicitud.setFechaServicio(fecha.getValue());
            solicitud.setHoraServicio(hora.getValue());
            solicitud.setCostoTotal(costo.getValue());
            solicitud.setMetodoPago(metodoPago.getValue());
            solicitud.setEstadoPago(estadoPago.getValue());
            solicitud.setComprobantePago(comprobante.getValue());
            solicitud.setEscoltaAsignado(escolta.getValue());
            solicitud.setVehiculoAsignado(vehiculo.getValue());
            solicitud.setMonitoreo(monitoreo.getValue());
            solicitud.setEmergenciaActiva("Activa".equals(emergencia.getValue()));
            solicitud.setCalificacion(calificacion.getValue());
            solicitud.setComentario(comentario.getValue());
            solicitud.setEstado(estado.getValue());

            if (ejecutarOperacion("Solicitud actualizada", "No fue posible actualizar la solicitud", () -> solicitudRepository.save(solicitud))) {
                dialog.close();
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(
                servicios,
                personalizacion,
                fila(fecha, hora, costo),
                fila(metodoPago, estadoPago, comprobante),
                fila(escolta, vehiculo, monitoreo),
                fila(emergencia, calificacion, comentario, estado),
                filaAcciones(guardar, new Button("Cancelar", event -> dialog.close()))
        ));
        dialog.open();
    }

    private void eliminarSolicitud(SolicitudServicio solicitud) {
        ejecutarOperacion("Solicitud eliminada", "No fue posible eliminar la solicitud", () -> solicitudRepository.delete(solicitud));
    }

    private boolean ejecutarOperacion(String mensajeExito, String mensajeError, Runnable operacion) {
        try {
            operacion.run();
            refrescarDatos();
            LOGGER.info(mensajeExito);
            Notification.show(mensajeExito);
            return true;
        } catch (RuntimeException ex) {
            manejarError(mensajeError, ex);
            return false;
        }
    }

    private void manejarError(String mensaje, RuntimeException ex) {
        LOGGER.error(mensaje, ex);
        Notification.show(mensaje + ". Revise los logs de la aplicacion.");
    }

    private Dialog crearDialogo(String tituloTexto) {
        Dialog dialog = new Dialog();
        H2 titulo = new H2(tituloTexto);
        titulo.getStyle().set("margin", "0 0 8px 0").set("font-size", "22px");
        dialog.add(titulo);
        dialog.setWidth("720px");
        return dialog;
    }

    private HorizontalLayout filaAcciones(Component... componentes) {
        HorizontalLayout layout = new HorizontalLayout(componentes);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        return layout;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    private double calcularTotal(Set<Servicio> servicios) {
        return servicios.stream().mapToDouble(Servicio::getPrecio).sum();
    }

    private HorizontalLayout fila(Component... componentes) {
        HorizontalLayout layout = new HorizontalLayout(componentes);
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.getStyle().set("gap", "12px").set("flex-wrap", "wrap");
        return layout;
    }

    private VerticalLayout panel(String titulo, Component... componentes) {
        H2 encabezado = new H2(titulo);
        encabezado.getStyle().set("font-size", "20px").set("margin", "0 0 12px 0");

        VerticalLayout layout = new VerticalLayout(encabezado);
        layout.add(componentes);
        layout.setWidthFull();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle()
                .set("background", "#ffffff")
                .set("border", "1px solid #dfe7f3")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 3px rgba(15, 23, 42, 0.08)")
                .set("margin-top", "14px");
        return layout;
    }

    private VerticalLayout metrica(String titulo, Span valor) {
        Span label = new Span(titulo);
        label.getStyle().set("color", "#667085").set("font-size", "13px");
        valor.getStyle().set("font-size", "26px").set("font-weight", "700").set("color", "#172033");

        VerticalLayout layout = new VerticalLayout(label, valor);
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.setWidthFull();
        layout.getStyle()
                .set("background", "#ffffff")
                .set("border", "1px solid #dfe7f3")
                .set("border-radius", "8px");
        return layout;
    }

}
