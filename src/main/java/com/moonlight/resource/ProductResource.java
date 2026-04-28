package com.moonlight.resource;

import com.moonlight.model.Product;
import com.moonlight.repository.ProductRepository;
import com.moonlight.service.ProductService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductService productService = new ProductService(new ProductRepository());

    @GET
    public Response getAll(@QueryParam("category") String category) {
        try {
            List<Product> products = productService.getAll(category);
            return Response.ok(products).build();
        } catch (RuntimeException e) {
            return Response.status(500)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        try {
            Product product = productService.getById(id);
            return Response.ok(product).build();
        } catch (RuntimeException e) {
            return Response.status(404)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/featured")
    public Response getFeatured() {
        try {
            List<Product> products = productService.getFeatured();
            return Response.ok(products).build();
        } catch (RuntimeException e) {
            return Response.status(500)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/categories")
    public Response getCategories() {
        try {
            List<String> categories = productService.getCategories();
            return Response.ok(categories).build();
        } catch (RuntimeException e) {
            return Response.status(500)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @POST
    public Response create(Product product) {
        try {
            Product saved = productService.save(product);
            return Response.status(201).entity(saved).build();
        } catch (RuntimeException e) {
            return Response.status(400)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
}