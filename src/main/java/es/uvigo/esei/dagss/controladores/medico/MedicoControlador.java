/*
 Proyecto Java EE, DAGSS-2013
 */
package es.uvigo.esei.dagss.controladores.medico;

import es.uvigo.esei.dagss.controladores.autenticacion.AutenticacionControlador;
import es.uvigo.esei.dagss.controladores.administrador.GestionCitasControlador;
import es.uvigo.esei.dagss.controladores.prescripcion.PrescripcionControlador;
import es.uvigo.esei.dagss.dominio.daos.CitaDAO;
import es.uvigo.esei.dagss.dominio.daos.MedicoDAO;
import es.uvigo.esei.dagss.dominio.entidades.Cita;
import es.uvigo.esei.dagss.dominio.entidades.Medico;
import es.uvigo.esei.dagss.dominio.entidades.TipoUsuario;
import es.uvigo.esei.dagss.dominio.entidades.EstadoCita;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author ribadas
 */

@Named(value = "medicoControlador")
@SessionScoped
public class MedicoControlador implements Serializable {

    private Medico medicoActual;
    private String dni;
    private String numeroColegiado;
    private String password;
    private List<Cita> listCitasMedico;

    @Inject
    private AutenticacionControlador autenticacionControlador;
    
    @Inject
    private GestionCitasControlador gestionCitasControlador;

    @Inject
    private PrescripcionControlador prescripcionControlador;
    
    @EJB
    private MedicoDAO medicoDAO;

    /**
     * Creates a new instance of AdministradorControlador
     */
    public MedicoControlador() {
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumeroColegiado() {
        return numeroColegiado;
    }
    
    public List<Cita> getListCitasMedico(){
        return this.listCitasMedico;
    }

    public void setNumeroColegiado(String numeroColegiado) {
        this.numeroColegiado = numeroColegiado;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Medico getMedicoActual() {
        return medicoActual;
    }

    public void setMedicoActual(Medico medicoActual) {
        this.medicoActual = medicoActual;
    }

    private boolean parametrosAccesoInvalidos() {
        return (((dni == null) && (numeroColegiado == null)) || (password == null));
    }

    private Medico recuperarDatosMedico() {
        Medico medico = null;
        if (dni != null) {
            medico = medicoDAO.buscarPorDNI(dni);
        }
        if ((medico == null) && (numeroColegiado != null)) {
            medico = medicoDAO.buscarPorNumeroColegiado(numeroColegiado);
        }
        return medico;
    }

    public String doLogin() {
        String destino = null;
        if (parametrosAccesoInvalidos()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No se ha indicado un DNI ó un número de colegiado o una contraseña", ""));
        } else {
            Medico medico = recuperarDatosMedico();
            if (medico == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe ningún médico con los datos indicados", ""));
            } else {
                if (autenticacionControlador.autenticarUsuario(medico.getId(), password,
                        TipoUsuario.MEDICO.getEtiqueta().toLowerCase())) {
                    medicoActual = medico;
                    destino = "privado/index";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }
            }
        }
        return destino;
    }

    //Acciones
    /**
     * Acción de Mostrar una cita
     * 
     * @param cita Cita a mostrar
     * @return Formulario de Atención al paciente
     */
    public String doShowCita(Cita cita) {
        gestionCitasControlador.setCitaActual(cita);
        prescripcionControlador.setPrescripcionesPaciente(cita.getPaciente());
        prescripcionControlador.setMedicoActual(medicoActual);
        prescripcionControlador.setPacienteActual(cita.getPaciente());
        return "/medico/privado/atencionPaciente/atencionPaciente";
    }
    
    /**
     * Acción de mostrar / ocultar botón de Mostrar cita.
     * Se muestra el botón si la cita está en estado PLANIFICADA
     * 
     * @param cita Cita para resolver la acción
     * @return True si el estado de la cita es PLANIFICADA, FALSE en caso contrario
     */
    public boolean doEnableButtonShowCita(Cita cita) {
        return (cita.getEstado()==EstadoCita.PLANIFICADA);
    }
    /**
     * Lista las citas que tiene el medico loggeado en el día actual.
     * 
     * @return List<Citas> lista de citas
     */
    public String dolistarCitasMedico() {
        this.listCitasMedico = medicoDAO.buscarCitasPorMedico(medicoActual);
        return "/medico/privado/agenda/listadoCitas";   
    }
    
}
