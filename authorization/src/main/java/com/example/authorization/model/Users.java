package com.example.authorization.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;  // ✅ ВАЖНО: добавьте этот импорт!

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users implements UserDetails {  // ✅ Теперь implements работает

    @Id
    @UuidGenerator
    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "surname", nullable = false, length = 255)
    private String surname;

    @Column(name = "patronymic", length = 255)
    private String patronymic;

    @Column(name = "phone", nullable = false, length = 255)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "login", nullable = false, length = 255, unique = true)
    private String login;

    @Column(name = "hesh_password", nullable = false, length = 255)
    private String heshPassword;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "Users_role_id_fkey"))
    private Role role;

    // ===== Реализация методов UserDetails =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null || role.getNameRole() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role.getNameRole()));
    }

    @Override
    public String getPassword() {
        return heshPassword;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}