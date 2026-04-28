package com.moonlight.config;

import com.moonlight.resource.AuthResource;
import com.moonlight.resource.OrderResource;
import com.moonlight.resource.ProductResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class AppConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(AuthResource.class);
        classes.add(ProductResource.class);
        classes.add(OrderResource.class);
        classes.add(CorsFilter.class);
        return classes;
    }
}