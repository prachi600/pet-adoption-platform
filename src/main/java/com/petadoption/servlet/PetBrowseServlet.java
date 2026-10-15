package com.petadoption.servlet;

import com.petadoption.dao.PetDao;
import com.petadoption.exception.AppException;
import com.petadoption.model.Pet;

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
    private final transient PetDao petDao = new PetDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String id = param(req, "id");
            if (!id.isEmpty()) {
                Pet pet = petDao.findById(intParam(req, "id"));
                if (pet == null || !(pet.getStatus().name().equals("APPROVED") || pet.getStatus().name().equals("ADOPTED"))) {
                    flash(req, "error", "That pet could not be found.");
                    redirect(req, resp, "/pets");
                    return;
                }
                req.setAttribute("pet", pet);
                forward(req, resp, "pet-detail.jsp");
                return;
            }
            String q = param(req, "q");
            String species = param(req, "species");
            List<Pet> pets = petDao.search(q, species);
            req.setAttribute("pets", pets);
            req.setAttribute("speciesList", petDao.findApprovedSpecies());
            req.setAttribute("q", q);
            req.setAttribute("species", species);
            forward(req, resp, "pets.jsp");
        } catch (AppException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, "/pets");
        }
    }
}
