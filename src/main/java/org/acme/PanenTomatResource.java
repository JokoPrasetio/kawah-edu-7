package org.acme;

import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/hasilPanen")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PanenTomatResource {
    @Inject
    EntityManager em;

    @Inject
    private Mailer mailer;

    @Inject
    private EmailService emailService;

    private final JasperReportGeneratorService jasperReportGeneratorService;

    public PanenTomatResource(DataSource dataSource){
        this.jasperReportGeneratorService = new JasperReportGeneratorService(dataSource);
    }


        @Transactional
        @Scheduled(cron = "0 0 0 L * ?")
        public void generateMonthlyReportandSendEmail() {
            try {
                String uuidToken = UUID.randomUUID().toString();
                String fileName = "report" + "_" + uuidToken + ".pdf";
                String outputFileName = "external_resources/GeneratedReport/" + fileName;
                String jasperReportPath = "external_resources/JasperReport/Simple_Blue.jrxml";
                jasperReportGeneratorService.generatedPdfReport(jasperReportPath, outputFileName);

                // kirim email dengan file laporan terlampir
                String subject = "Laporan Bulanan";
                String body = "Berikut ini adalah laporan bulanan kami";
                String attachmentPath = outputFileName;
                emailService.sendEmailWithAttachment("joko12prasetio@gmail.com", subject, body, attachmentPath);

                System.out.println("Laporan bulanan telah berhasil dibuat dan dikirimkan melalui email.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Gagal membuat dan mengirimkan laporan bulanan.");
            }
        }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<PanenTomat> getAllPanenTomat(){
        return em.createNativeQuery("SELECT * FROM panentomat", PanenTomat.class).getResultList();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addPanenTomat(PanenTomat panenTomat){
        em.persist(panenTomat);
        return Response.ok().build();
    }


    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updatePanenTomat(@PathParam("id") UUID id, PanenTomat updatedPanenTomat){
        PanenTomat panenTomat = em.find(PanenTomat.class, id);
        if(panenTomat == null){
            throw new EntityNotFoundException("Panen tomat with id" + id + "not found");
        }
        panenTomat.setKomoditas(updatedPanenTomat.getKomoditas());
        panenTomat.setTotal(updatedPanenTomat.getTotal());
        panenTomat.setUpdatedAt(updatedPanenTomat.getUpdatedAt());
        em.merge(panenTomat);
        return Response.ok().build();
    }
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deletePanenTomat(@PathParam("id") UUID id){
        PanenTomat panenTomat = em.find(PanenTomat.class, id);
        if (panenTomat == null ){
            throw new EntityNotFoundException("Panen Tomat with id " + id + "not found");
        }
        em.remove(panenTomat);
        return Response.ok().build();
    }
}
