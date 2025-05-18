    package com.unicolombo.bienestar.models;

    import jakarta.persistence.*;
    import java.time.Instant;

    @Entity
    public class RefreshToken {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true)
        private String token;

        @OneToOne
        @JoinColumn(name = "usuario_id", referencedColumnName = "id")
        private Usuario usuario;

        @Column(nullable = false)
        private Instant expiryDate;

        public Long getId() {
            return id;
        }

        public String getToken() {
            return token;
        }

        public Usuario getUsuario() {
            return usuario;
        }

        public Instant getExpiryDate() {
            return expiryDate;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setUsuario(Usuario usuario) {
            this.usuario = usuario;
        }

        public void setExpiryDate(Instant expiryDate) {
            this.expiryDate = expiryDate;
        }
    }
