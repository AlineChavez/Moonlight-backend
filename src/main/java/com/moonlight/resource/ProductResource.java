package com.moonlight.resource;

import com.moonlight.model.Product;
import com.moonlight.repository.JpaProductRepository;
import com.moonlight.security.AuthContext;
import com.moonlight.security.JwtUtil;
import com.moonlight.service.ProductService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductService productService = new ProductService(new JpaProductRepository());
    private final AuthContext authContext = new AuthContext(new JwtUtil());

    @GET
    public Response getAll(@QueryParam("category") String category) {
        List<Product> products = productService.getAll(category);
        return Response.ok(products).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Product product = productService.getById(id);
        return Response.ok(product).build();
    }

    @GET
    @Path("/featured")
    public Response getFeatured() {
        List<Product> products = productService.getFeatured();
        return Response.ok(products).build();
    }

    @GET
    @Path("/categories")
    public Response getCategories() {
        List<String> categories = productService.getCategories();
        return Response.ok(categories).build();
    }

    @POST
    public Response create(Product product, @HeaderParam("Authorization") String authHeader) {
        authContext.requireAdmin(authHeader);
        Product saved = productService.save(product);
        return Response.status(201).entity(saved).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Product product,
                           @HeaderParam("Authorization") String authHeader) {
        authContext.requireAdmin(authHeader);
        Product updated = productService.update(id, product);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id, @HeaderParam("Authorization") String authHeader) {
        authContext.requireAdmin(authHeader);
        productService.delete(id);
        return Response.noContent().build();
    }
}
