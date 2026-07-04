// authorization/model/Users.java
package com.example.authorization.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users implements UserDetails {

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

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    // Связь с контрагентами через таблицу user_contractor
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_contractor",
            joinColumns = @JoinColumn(name = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_contractor")
    )
    private List<Contractor> contractors = new ArrayList<>();

    // ✅ СВЯЗЬ С INSTITUTION (ManyToMany через user_institution)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_institution",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "institution_id")
    )
    @Builder.Default
    private Set<Institution> institutions = new HashSet<>();

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