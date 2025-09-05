package com.example.bankcards.entity.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Пользователь.
 * Замена стандартному UserDetails.
 * Ролей не может быть много, поэтому поставил на них "жадную" загрузку, это не скажется на производительности,
 * но гарантированно избавит от проблем преждевременно закрытой сессии.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /**
     * Возвращает коллекцию ролей и прав пользователя (грантов).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // добавляем роли
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(addPrefixIfMissing(role.getName(), "ROLE_")))
                .collect(Collectors.toList());
    }

    private String addPrefixIfMissing(String value, String prefix) {
        return value.startsWith(prefix) ? value : prefix + value;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Методы для правильного добавления/удаления ролей пользователю.
     */
    public void addRole(Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }


    /**
     * Конвертация множества ролей в строку.
     */
    public String getRolesList() {
        return roles.stream().map(Role::getName).toList().toString();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + getRolesList() +
                '}';
    }
}
