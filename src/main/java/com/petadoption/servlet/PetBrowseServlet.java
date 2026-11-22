package com.petadoption.servlet;

import com.petadoption.exception.AppException;
import com.petadoption.model.Pet;
import com.petadoption.service.PetService;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Public pet catalogue: search list (/pets) and detail page (/pets?id=5). */
@WebServlet(name = "PetBrowseServlet", urlPatterns = "/pets")
public class PetBrowseServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient PetService petService = new PetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!param(req, "id").isEmpty()) {
                req.setAttribute("pet", petService.getPublicPet(intParam(req, "id")));
                forward(req, resp, "pet-detail.jsp");
                return;
            }
            String q = param(req, "q");
            String species = param(req, "species");
            List<Pet> pets = petService.search(q, species);
            req.setAttribute("pets", pets);
            req.setAttribute("speciesList", petService.approvedSpecies());
            req.setAttribute("q", q);
            req.setAttribute("species", species);
            forward(req, resp, "pets.jsp");
        } catch (AppException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, "/pets");
        }
    }
}
