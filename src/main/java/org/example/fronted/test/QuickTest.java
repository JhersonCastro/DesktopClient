package org.example.fronted.test;

import org.example.fronted.api.AuthApi;

public class QuickTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Probando API completa...");

        AuthApi auth = new AuthApi();

        String email = "ana.gomez@unicauca.edu.co";
        String password = "Segura!2025";

        System.out.println("1. Probando login...");
        auth.login(email, password)
                .subscribe(
                        success -> {
                            if (success) {
                                System.out.println("Login exitoso");

                                System.out.println("\n2. Probando obtener usuario actual...");
                                auth.getCurrentUser()
                                        .subscribe(
                                                user -> {
                                                    System.out.println("Usuario obtenido: " + user);

                                                    System.out.println("\n3. Probando logout...");
                                                    auth.logout()
                                                            .subscribe(
                                                                    logoutSuccess -> {
                                                                        System.out.println("Logout exitoso");
                                                                        System.out.println("\nTodas las pruebas completadas exitosamente");
                                                                    },
                                                                    logoutError -> {
                                                                        System.err.println("Error en logout: " + logoutError.getMessage());
                                                                    }
                                                            );
                                                },
                                                error -> {
                                                    System.err.println("Error obteniendo usuario: " + error.getMessage());
                                                }
                                        );
                            } else {
                                System.out.println("Login fallido");
                            }
                        },
                        error -> {
                            System.err.println("Error en login: " + error.getMessage());
                        }
                );

        Thread.sleep(5000);
    }
}