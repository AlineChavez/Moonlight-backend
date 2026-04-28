package com.moonlight.resource;

import com.moonlight.model.Order;
import com.moonlight.model.OrderItem;
import com.moonlight.repository.OrderRepository;
import com.moonlight.security.JwtUtil;
import com.moonlight.service.OrderService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService orderService = new OrderService(new OrderRepository());
    private final JwtUtil jwtUtil = new JwtUtil();

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token requerido");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token)) {
            throw new RuntimeException("Token inválido");
        }
        return jwtUtil.getUserId(token);
    }

    @POST
    public Response create(Map<String, Object> body,
                           @HeaderParam("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);

            List<Map<String, Object>> rawItems = (List<Map<String, Object>>) body.get("items");
            if (rawItems == null || rawItems.isEmpty()) {
                return Response.status(400)
                        .entity(Map.of("message", "El pedido debe tener al menos un producto"))
                        .build();
            }

            List<OrderItem> items = rawItems.stream().map(i -> {
                OrderItem item = new OrderItem();
                item.setProductId(Long.parseLong(i.get("id").toString()));
                item.setProductName(i.get("name").toString());
                item.setSize(i.get("size").toString());
                item.setQuantity(Integer.parseInt(i.get("quantity").toString()));
                item.setPrice(Double.parseDouble(i.get("price").toString()));
                return item;
            }).toList();

            Order order = orderService.create(userId, items);
            return Response.status(201).entity(order).build();

        } catch (RuntimeException e) {
            return Response.status(400)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/my")
    public Response getMyOrders(@HeaderParam("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            List<Order> orders = orderService.getMyOrders(userId);
            return Response.ok(orders).build();
        } catch (RuntimeException e) {
            return Response.status(401)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id,
                            @HeaderParam("Authorization") String authHeader) {
        try {
            getUserIdFromToken(authHeader);
            Order order = orderService.getById(id);
            return Response.ok(order).build();
        } catch (RuntimeException e) {
            return Response.status(404)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
}